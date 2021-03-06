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
package it.geosolutions.geobatch.services.jmx;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.catalog.Catalog;
import it.geosolutions.geobatch.catalog.file.DataDirHandler;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.configuration.event.consumer.file.FileBasedEventConsumerConfiguration;
import it.geosolutions.geobatch.configuration.flow.file.FileBasedFlowConfiguration;
import it.geosolutions.geobatch.flow.event.IProgressListener;
import it.geosolutions.geobatch.flow.event.action.ActionService;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.flow.event.consumer.EventConsumer;
import it.geosolutions.geobatch.flow.event.consumer.file.FileBasedEventConsumer;
import it.geosolutions.geobatch.flow.file.FileBasedFlowManager;
import it.geosolutions.geobatch.global.CatalogHolder;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * 
 * GeoBatch JMX service which supports:
 * <ul>
 * <li>
 * - creating JMX flow on the fly</li>
 * <li>
 * - creating and starting consumers with externally configured action</li>
 * <li>
 * - get status of JMX consumer instances</li>
 * <li>
 * - dispose JMX consumer instances</li>
 * <li>
 * - get status of JMX consumer instance</li>
 * </ul>
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 */
@ManagedResource(objectName = "bean:name=JMXServiceManager", description = "JMX Service Manager to start/monitor/dispose GeoBatch action", log = true, logFile = "jmx.log", persistName = "JMXServiceManager")
public class JMXServiceManager implements ServiceManager {
	private final static Logger LOGGER = LoggerFactory
			.getLogger(JMXServiceManager.class);

	public final static String FLOW_MANAGER_ID = "JMX_FLOW_MANAGER";

	private static Catalog catalog;

	private static FileBasedFlowManager flowManager;

	private FileBasedFlowConfiguration flowManagerConfig;

	private static File configDirFile;

	@Resource(name = "dataDirHandler")
	private DataDirHandler dataDirHandler;

	@Resource(type = org.springframework.context.ApplicationContext.class)
	private ApplicationContext context;

	@PostConstruct
	private void initialize() throws Exception {
		catalog = CatalogHolder.getCatalog();

		flowManager = catalog.getResource(FLOW_MANAGER_ID,
				it.geosolutions.geobatch.flow.file.FileBasedFlowManager.class);
		if (flowManager == null) {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("The flow id \'"
						+ FLOW_MANAGER_ID
						+ "\' does not exists into catalog... -> going to create it");
			}
			flowManagerConfig = new FileBasedFlowConfiguration(FLOW_MANAGER_ID,
					FLOW_MANAGER_ID, "Auto generated " + FLOW_MANAGER_ID, null,
					null);

			configDirFile = flowManager.initConfigDir(flowManagerConfig,
					dataDirHandler.getBaseConfigDirectory());
			if (!configDirFile.exists()) {
				if (!configDirFile.mkdir()) {
					throw new IllegalArgumentException(
							"Unable to automatically create the "
									+ FLOW_MANAGER_ID
									+ " working dir into: "
									+ configDirFile.getAbsolutePath()
											.toString());
				}
			}
			flowManagerConfig.setOverrideConfigDir(configDirFile);

			// keep consumer until disposeAction is called
			flowManagerConfig.setKeepConsumers(true);
			// flowManagerConfig.setMaximumPoolSize(flowManagerConfig.getMaximumPoolSize());
			// // TODO create a spec param

			flowManager = new FileBasedFlowManager(flowManagerConfig,
					dataDirHandler);

			catalog.add(flowManager);
			// TODO persistence (throws NullPointerException)
			// catalog.save(parent);
			// parent.persist();
		} else {
			flowManagerConfig = flowManager.getConfiguration();
			configDirFile = FileBasedFlowManager.initConfigDir(
					flowManagerConfig, dataDirHandler.getBaseConfigDirectory());
		}

	}

	@Override
	@org.springframework.jmx.export.annotation.ManagedOperation(description = "disposeAction - used to dispose the consumer instance from the consumer registry")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "uuid", description = "The uuid of the consumer") })
	public void disposeConsumer(final String uuid) throws Exception {
		flowManager.disposeConsumer(uuid);
	}

	/**
	 * returns the status of the selected consumer
	 * 
	 * @param uuid
	 * @return {@link ConsumerStatus}
	 */
	@Override
	@org.springframework.jmx.export.annotation.ManagedOperation(description = "get the status of the selected consumer")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "uuid", description = "The uuid of the consumer") })
	public ConsumerStatus getStatus(String uuid) {
		return ConsumerStatus.getStatus(flowManager.getStatus(uuid));
	}

	@Override
    @org.springframework.jmx.export.annotation.ManagedOperation(description = "createConsumer - used to get a new consumer")
    public String createConsumer(java.util.Map<String, String> config) throws Exception {
    	
        final String serviceId = config.remove(ConsumerManager.SERVICE_ID_KEY);
        if (serviceId == null || serviceId.isEmpty())
            throw new IllegalArgumentException(
                                               "Unable to locate the key "
                                                   + ConsumerManager.SERVICE_ID_KEY
                                                   + " matching the serviceId action in the passed paramether table");
        
        final ActionService service = (ActionService)context.getBean(serviceId);
        final Class serviceClass = service.getClass();

        ActionConfiguration actionConfig = null;
        for (Method method : serviceClass.getMethods()) {
            if (method.getName().equals("canCreateAction")) {
                final Class[] classes = method.getParameterTypes();

                Constructor constructor;

                // BeanUtils.instantiate(clazz)
                try {
                    constructor = classes[0].getConstructor(new Class[] {});
                    actionConfig = (ActionConfiguration)constructor.newInstance();
                } catch (NoSuchMethodException e) {
                    constructor = classes[0].getConstructor(new Class[] {String.class, String.class,
                                                                         String.class});
                    actionConfig = (ActionConfiguration)constructor.newInstance(serviceId, serviceId,
                                                                                serviceId);
                }
                actionConfig.setServiceID(serviceId);
                final Set<String> keys = config.keySet();
                final Iterator<String> it = keys.iterator();
                while (it.hasNext()) {
                    String key = it.next();
                    try {
                        smartCopy(actionConfig, key, config.get(key));
                    } catch (Exception e) {
                        if (LOGGER.isErrorEnabled())
                            LOGGER.error(e.getLocalizedMessage(), e);
                        // TODO something else?
                    }
                }

                if (actionConfig != null)
                    break;
            }
        }
        if (actionConfig == null)
            throw new IllegalArgumentException("Unable to locate the configuration");

        final FileBasedEventConsumerConfiguration consumerConfig = new FileBasedEventConsumerConfiguration("JMX_Consumer_id");

        final List<ActionConfiguration> actions = new ArrayList<ActionConfiguration>();
        actions.add(actionConfig);

        consumerConfig.setActions(actions);
        
        // TODO may we want to remove only when getStatus is remotely called???
        // consumerConfig.setKeepContextDir(true);

        // if you want to move the input you may call the action move!
        consumerConfig.setPreserveInput(true);

        File configDir=FileBasedFlowManager.initConfigDir(flowManager.getConfiguration(), dataDirHandler.getBaseConfigDirectory());
        File tempDir=FileBasedFlowManager.initTempDir(flowManager.getConfiguration(), dataDirHandler.getBaseTempDirectory());
        final FileBasedEventConsumer consumer = new FileBasedEventConsumer(consumerConfig, configDir, tempDir);
        
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("INIT injecting consumer to the parent flow. UUID: " + consumer.getId());
        }

        // Add to the consumer map
        if (!flowManager.addConsumer(consumer)) {
            consumer.dispose();
            throw new IllegalStateException("Unable to add another consumer, consumer queue is full. "
                            + "Please dispose some completed consumer before submit a new one.");
        }
        
        final Iterator<BaseAction<FileSystemEvent>> ait=consumer.getActions().iterator();
		while (ait.hasNext()){
	        ait.next().addListener(new JMXCumulatorListener());	
		}
        
//		NOT USEFUL messages from the consumer
//		consumer.addListener(new JMXProgressListener());
        
        return consumer.getId();
    }

    /**
     * create the configured action on the remote GeoBatch server through the
     * JMX connection
     * 
     * @param config A map containing the list of needed parameters, inputs and
     *            outputs used by the action
     * @throws Exception if:
     *             <ul>
     *             <li>the passed map is null</li>
     *             <li>the passed map doesn't contains needed keys</li>
     *             <li>the connection is lost</li>
     *             </ul>
     */
	@Override
	@org.springframework.jmx.export.annotation.ManagedOperation(description = "runConsumer - used to run a consumer")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "jmxConsumer", description = "A map containing the list of needed parameters, inputs and outputs used by the action") })
	public void runConsumer(String uuid, Serializable event) throws Exception {

		if (uuid==null || event==null){
			throw new IllegalArgumentException("Unable to run using null arguments: uuid="+uuid+" event="+event);
		}
		
		EventConsumer consumer = getConsumer(uuid);

		// ///////// SET INPUTS
		if (event instanceof File)
			consumer.consume(new FileSystemEvent(File.class.cast(event),FileSystemEventType.FILE_ADDED));
		else if (event instanceof String)
			consumer.consume(new FileSystemEvent(new File(event.toString()),FileSystemEventType.FILE_ADDED));
		else
			throw new IllegalArgumentException("Unable to use the incoming event: bad type ->"+event.getClass());
		// ///////// RUN CONSUMER
		// execute
		flowManager.getExecutor().submit(consumer);
	}
	
	@Override
	@org.springframework.jmx.export.annotation.ManagedOperation(description = "getListeners - used to run a consumer")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "uuid", description = "A map containing the list of needed parameters, inputs and outputs used by the action") })
	public Collection<JMXProgressListener> getListeners(String uuid, Class<? extends JMXProgressListener> type) {

		EventConsumer consumer = getConsumer(uuid);

		final List<JMXProgressListener> al=get(consumer.getListeners(JMXCumulatorListener.class));
		
		if (consumer instanceof FileBasedEventConsumer){
			final FileBasedEventConsumer fbConsumer=FileBasedEventConsumer.class.cast(consumer);

			final Iterator<BaseAction<FileSystemEvent>> ait=fbConsumer.getActions().iterator();
			
			while (ait.hasNext()){
				al.addAll(get(ait.next().getListeners(JMXCumulatorListener.class)));
			}
		}
		return al;
	}
	
	private static List<JMXProgressListener> get(Collection<IProgressListener> coll){
		final List<JMXProgressListener> al=new ArrayList<JMXProgressListener>();
		if (coll==null){
			return al;
		}
		final Iterator<IProgressListener> cit=coll.iterator();
		while (cit.hasNext()){
			IProgressListener pl=cit.next();
			if (pl instanceof JMXCumulatorListener){
				al.add(JMXCumulatorListener.class.cast(pl));
			}
		}
		return al;
	}

	public void setDataDirHandler(DataDirHandler dataDirHandler) {
		this.dataDirHandler = dataDirHandler;
	}

	private EventConsumer getConsumer(String uuid)
			throws IllegalArgumentException {
		final EventConsumer consumer = flowManager.getConsumer(uuid);
		if (consumer == null)
			throw new IllegalArgumentException(
					"Unable to get a consumer using uuid: " + uuid);
		return consumer;
	}

	private static <T> void smartCopy(final T bean, final String propertyName,
			final Object value) throws Exception {
		// special cases
		PropertyDescriptor pd = PropertyUtils.getPropertyDescriptor(bean,
				propertyName);
		// return null if there is no such descriptor
		if (pd == null) {
			return;
		}

		Class type = pd.getPropertyType();

		Object valueTo = value;
		if (type.isAssignableFrom(value.getClass())) {
			// try using setter
			if (pd.getWriteMethod() != null) {
				PropertyUtils.setProperty(bean, propertyName, valueTo);
				return;
			} else {
				// T interface doesn't declare setter method for this property
				// lets use getter methods to get the property reference
				Object property = PropertyUtils.getProperty(bean, propertyName);
				if (property == null) {
					if (LOGGER.isErrorEnabled())
						LOGGER.error("Skipping unwritable property "
								+ propertyName);
				} else {
					if (Collection.class.isAssignableFrom(type)) {
						((Collection) property).addAll((Collection) value);
					} else if (Map.class.isAssignableFrom(type)) {
						((Map) property).putAll((Map) value);
					}
				}
			}
		} else if (Collection.class.isAssignableFrom(type)) {
			valueTo = adaptCollection(value);

		} else if (Map.class.isAssignableFrom(type)) {

			valueTo = adaptMap(value);

		} else {

			// fail
			// try quick way
			try {
				BeanUtils.copyProperty(bean, propertyName, valueTo);
				return;
			} catch (Exception e) {
				if (LOGGER.isWarnEnabled())
					LOGGER.warn("Error using ");
			}
		}

		//
		// boolean status = true;
		// if (!status) {
		// if (LOGGER.isErrorEnabled())
		// LOGGER.error("Skipping unwritable property " + propertyName
		// + " unable to find the adapter for type " + pd.getPropertyType());
		// }
	}

	private static Map adaptMap(Object value) {
		Map<Object, Object> liveMap = new HashMap<Object, Object>();
		// value should be a list of key=value string ';' separated
		String[] listString = ((String) value).split(";");
		for (String kvString : listString) {
			String kv[] = kvString.split("=");
			liveMap.put(kv[0], kv[1]);
		}
		return liveMap;
	}

	private static Collection adaptCollection(Object value) {

		Collection<Object> liveCollection = new ArrayList<Object>();
		// value should be a list of string ',' separated
		String[] listString = ((String) value).split(",");
		for (String s : listString) {
			liveCollection.add(s);
		}
		return liveCollection;
	}

	/*
	 * TODO make smart adapter extensible
	 * 
	 * public interface TypeAdapter<F, T> { public boolean adapt(Class<F>
	 * fromType, Class<T> toType, Object property, Object value); };
	 * 
	 * private final class StringToCollectionAdapter implements
	 * TypeAdapter<String, Map> {
	 * 
	 * @Override public boolean adapt(Class<String> fromType, Class<Map> toType,
	 * Object property, Object value) {
	 * 
	 * // check type of property to apply new value if
	 * (Map.class.isAssignableFrom(toType)) {
	 * 
	 * final Map<Object, Object> liveMap; if (property != null) { liveMap =
	 * (Map<Object, Object>)property; liveMap.clear(); } else { return false; }
	 * if (fromType.isAssignableFrom(value.getClass())) { // value should be a
	 * list of key=value string ';' separated String[] listString =
	 * ((String)value).split(";"); for (String kvString : listString) { String
	 * kv[] = kvString.split("="); liveMap.put(kv[0], kv[1]); } return true; } }
	 * 
	 * return false; } }
	 */
}
