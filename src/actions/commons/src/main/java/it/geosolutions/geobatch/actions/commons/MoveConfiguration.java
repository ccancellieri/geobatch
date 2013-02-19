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
package it.geosolutions.geobatch.actions.commons;

import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

/**
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 */
@Configuration("MoveConfiguration")
@Scope("prototype")
public class MoveConfiguration extends ActionConfiguration {

	public MoveConfiguration() {
        super(null, null, null);
    }
	
    public MoveConfiguration(String id, String name, String description) {
        super(id, name, description);
    }

    private File destination;

    private int timeout = 30; // seconds

    /**
     * @return the timeout
     */
    public final int getTimeout() {
        return timeout;
    }

    /**
     * @param timeout the timeout to set
     */
    public final void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * @return the destination
     */
    public final File getDestination() {
        return destination;
    }

    /**
     * @param destination the destination to set
     */
    public final void setDestination(File destination) {
        this.destination = destination;
    }

}
