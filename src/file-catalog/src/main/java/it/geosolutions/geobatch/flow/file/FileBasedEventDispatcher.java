/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.geo-solutions.it/
 *  Copyright (C) 2007-2012 GeoSolutions S.A.S.
 *  http://www.geo-solutions.it
 *
 *  GPLv3 + Classpath exception
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.geosolutions.geobatch.flow.file;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.geobatch.configuration.event.consumer.file.FileBasedEventConsumerConfiguration;
import it.geosolutions.geobatch.configuration.flow.file.FileBasedFlowConfiguration;
import it.geosolutions.geobatch.flow.event.consumer.BaseEventConsumer;
import it.geosolutions.geobatch.flow.event.consumer.EventConsumer;
import it.geosolutions.geobatch.flow.event.consumer.EventConsumerStatus;
import it.geosolutions.geobatch.flow.event.consumer.file.FileBasedEventConsumer;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fetch events and feed them to Consumers.
 * 
 * <P>
 * For every incoming event, existing consumers are checked if they are waiting
 * for it. <BR>
 * If the new event is not consumed by any existing consumer, a new consumer
 * will be created.
 * 
 * @author AlFa
 * @author Emanuele Tajariol, GeoSolutions
 */
/* package private */class FileBasedEventDispatcher extends Thread {
	private static final Logger LOGGER = LoggerFactory.getLogger(FileBasedEventDispatcher.class);

	private final BlockingQueue<FileSystemEvent> eventMailBox;

	private final FileBasedFlowManager flowManager;

	// ----------------------------------------------- PUBLIC METHODS
	/**
	 * Default Constructor
	 */
	public FileBasedEventDispatcher(FileBasedFlowManager fm,
			BlockingQueue<FileSystemEvent> eventMailBox) {
		super(new StringBuilder(
				"FileBasedEventDispatcher: EventDispatcherThread-").append(
				fm.getId()).toString());

		this.eventMailBox = eventMailBox;
		this.flowManager = fm;

		setDaemon(true); // shut me down when parent shutdown
		// reset interrupted flag
		interrupted();
	}

	/**
	 * Shutdown the dispatcher.
	 */
	public void shutdown() {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Shutting down the dispatcher ... NOW!");
		}
		interrupt();
	}

	// ----------------------------------------------- UTILITY METHODS

	/**
     *
     */
	public void run() {
		try {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Ready to dispatch Events to flow "
						+ flowManager.getId() 
                        + "(" + flowManager.getName() + ")");
			}

			while (!isInterrupted()) {

				// waiting for a new event
                
				final FileSystemEvent event;
				try {
					event = eventMailBox.take(); // blocking call
				} catch (InterruptedException e) {
					this.interrupt();

					return;
				}

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Processing incoming event " + event);
				}

				// //
				// is there any BaseEventConsumer waiting for this particular
				// event?
				// //
				boolean eventServed = false;

				for (String consumerId : flowManager.getEventConsumersId()) {
					
					EventConsumer consumer=flowManager.getConsumer(consumerId);
					
					if (LOGGER.isTraceEnabled()) {
						LOGGER.trace("Checking consumer " + consumer + " for "+ event);
					}

					if (consumer.getStatus() == EventConsumerStatus.EXECUTING) {
						if (consumer.consume(event)) {
							// //
							// we have found an Event BaseEventConsumer waiting
							// for this event, if
							// we have changed state we remove it from the list
							// //
							if (LOGGER.isTraceEnabled()) {
								LOGGER.trace(event + " was the last needed event for " + consumer);
							}

							// are we executing? If we are, let's trigger a
							// thread!
							flowManager.execute(consumer);
							// event served
							eventServed = true;

							break;
						}
					} else if (LOGGER.isTraceEnabled()) {
						LOGGER.trace(event + " was consumed by " + consumer);
					}
				}

				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace(event + (eventServed ? "" : " not") + " served");
				}

				if (!eventServed) {
                    // //
                    // if no EventConsumer is found, we need to create a new one
                    // //
                    final FileBasedFlowConfiguration flowCfg = flowManager.getConfiguration();
                    final FileBasedEventConsumerConfiguration consumerCfg = ((FileBasedEventConsumerConfiguration)flowCfg.getEventConsumerConfiguration()).clone();
		    final BaseEventConsumer brandNewConsumer = new FileBasedEventConsumer(consumerCfg, flowManager.getFlowConfigDir(), flowManager.getFlowTempDir());
                    brandNewConsumer.setFlowName(flowManager.getName());

					if (brandNewConsumer.consume(event)) {
						// //
						// We just created a brand new BaseEventConsumer which
						// can handle this event.
						// If it needs some other events to complete, we'll put
						// it in the EventConsumers waiting list.
						// //
						eventServed = flowManager.addConsumer(brandNewConsumer);
						if ( ! eventServed ) {
							if (LOGGER.isWarnEnabled()) {
								LOGGER.warn("No consumer could serve " + event
										+ " (neither " + brandNewConsumer + " could)");
							}
							flowManager.disposeConsumer(brandNewConsumer);
                        } else {
							if (brandNewConsumer.getStatus() != EventConsumerStatus.EXECUTING) {
								if (LOGGER.isDebugEnabled()) {
									LOGGER.debug(brandNewConsumer + " created on event " + event);
								} 
							} else {
								if (LOGGER.isDebugEnabled()) {
									LOGGER.debug(event + " was the only needed event for " + brandNewConsumer);
								}
	                            flowManager.execute(brandNewConsumer);
							}
                        }
					}
				}
			}
		} catch (InterruptedException e) { // may be thrown by the "stop" button
			// on web interface
			LOGGER.error(e.getLocalizedMessage(), e);
		} catch (IOException e) {
			LOGGER.error(e.getLocalizedMessage(), e);
		}

	}
}
