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

package it.geosolutions.geobatch.geoserver.shapefile;

import it.geosolutions.geobatch.catalog.impl.BaseService;
import it.geosolutions.geobatch.flow.event.action.ActionService;
import it.geosolutions.geobatch.geoserver.GeoServerActionConfiguration;

import java.io.IOException;
import java.util.EventObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author AlFa
 * @author (r2)Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 */
public class ShapeFileGeoServerService extends BaseService implements ActionService<EventObject, GeoServerActionConfiguration>  {

    private final static Logger LOGGER = LoggerFactory.getLogger(ShapeFileGeoServerService.class);

    public ShapeFileGeoServerService(String id, String name, String description) {
        super(id, name, description);
    }
    
    public ShapeFileAction createAction(final GeoServerActionConfiguration configuration) {
        try {
            return new ShapeFileAction(configuration);
        } catch (IOException e) {
            if (LOGGER.isWarnEnabled())
                LOGGER.warn(e.getLocalizedMessage(), e);
            return null;
        }
    }

    public boolean canCreateAction(final GeoServerActionConfiguration configuration) {
        // data flow configuration must not be null.
        if (configuration == null) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error("Cannot create the ShapeFileAction:  Configuration is null.");
            return false;
        }

        return true;
    }

}
