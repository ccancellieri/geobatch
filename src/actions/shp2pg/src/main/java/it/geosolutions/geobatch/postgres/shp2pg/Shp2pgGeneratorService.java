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
package it.geosolutions.geobatch.postgres.shp2pg;

import it.geosolutions.geobatch.actions.tools.configuration.Path;
import it.geosolutions.geobatch.catalog.impl.BaseService;
import it.geosolutions.geobatch.flow.event.action.ActionService;

import java.util.EventObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Andrea Di Nora - andrea.dinora@sinergis.it
 * 
 */
public class Shp2pgGeneratorService extends BaseService implements
        ActionService<EventObject, Shp2pgConfiguration> {

    public Shp2pgGeneratorService(String id, String name, String description) {
        super(id, name, description);
    }

    private final static Logger LOGGER = LoggerFactory.getLogger(Shp2pgGeneratorService.class);

    public Shp2pgAction createAction(Shp2pgConfiguration configuration) {
        try {
            return new Shp2pgAction(configuration);
        } catch (Exception e) {
            if (LOGGER.isInfoEnabled())
                LOGGER.info(e.getLocalizedMessage(), e);
            return null;
        }
    }

    public boolean canCreateAction(Shp2pgConfiguration configuration) {
        try {
            // absolutize working dir
            String wd = Path.getAbsolutePath(configuration.getWorkingDirectory());
            if (wd != null) {
                configuration.setWorkingDirectory(wd);
                return true;
            } else {
                if (LOGGER.isWarnEnabled())
                    LOGGER
                            .warn("Shp2pgGeneratorService::canCreateAction(): "
                                    + "unable to create action, it's not possible to get an absolute working dir.");
            }
        } catch (Throwable e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error(e.getLocalizedMessage(), e);
        }
        return false;
    }

}
