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

package it.geosolutions.geobatch.ftp.client.delete;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.geobatch.catalog.impl.BaseService;
import it.geosolutions.geobatch.flow.event.action.ActionService;
import it.geosolutions.geobatch.ftp.client.configuration.FTPActionConfiguration;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class manage the delete service running the FTP delete action.
 * 
 * @author Tobia Di Pisa (tobia.dipisa@geo-solutions.it)
 * 
 */
public class FTPDeleteActionService extends BaseService implements
        ActionService<FileSystemEvent, FTPActionConfiguration> {

    public FTPDeleteActionService(String id, String name, String description) {
        super(id, name, description);
    }

    private final static Logger LOGGER = LoggerFactory.getLogger(FTPDeleteActionService.class.toString());

    /**
     * The FTPDeleteActionServiceb default constructor.
     */
//    public FTPDeleteActionService() {
//        super(true);
//    }

    /**
     * Method to verify if the action creation is available.
     * 
     * @param configuration
     *            The FTP action configuration.
     * @return boolean
     */
    public boolean canCreateAction(FTPActionConfiguration configuration) {
        return true;
    }

    /**
     * Method to create a delete action using the FTP action configuration.
     * 
     * @param configuration
     *            The FTP action configuration.
     * @return The FTPDeleteAction
     */
    public FTPDeleteAction createAction(FTPActionConfiguration configuration) {
        try {
            return new FTPDeleteAction(configuration);
        } catch (IOException e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error(e.getLocalizedMessage(), e);
        }

        return null;
    }
}
