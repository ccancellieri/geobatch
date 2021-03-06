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

package it.geosolutions.geobatch.octave.actions;

import it.geosolutions.geobatch.octave.OctaveCommand;
import it.geosolutions.geobatch.octave.OctaveEnv;
import it.geosolutions.geobatch.octave.OctaveExecutableSheet;
import it.geosolutions.geobatch.octave.OctaveFunctionFile;
import it.geosolutions.geobatch.octave.OctaveFunctionSheet;
import it.geosolutions.geobatch.octave.SerializableOctaveFile;
import it.geosolutions.geobatch.octave.SerializableOctaveObject;
import it.geosolutions.geobatch.octave.SerializableOctaveString;
import it.geosolutions.geobatch.registry.AliasRegistrar;
import it.geosolutions.geobatch.registry.AliasRegistry;

/**
 * Register XStream aliases for the relevant services we ship in this class.
 * 
 * @author Carlo Cancellieri <carlo.cancellieri@geo-solutions.it>
 */
public class OctaveAliasRegistrar extends AliasRegistrar {

    public OctaveAliasRegistrar(AliasRegistry registry) {
        LOGGER.info(getClass().getSimpleName() + ": registering alias.");
        registry.putAlias(
                "OctaveConfiguration",OctaveActionConfiguration.class);
        
        registry.putAlias("octave",OctaveEnv.class);
        registry.putAlias("sheet",OctaveExecutableSheet.class);
        registry.putAlias("sheet",OctaveFunctionSheet.class);
        registry.putAlias("OctaveCommand",OctaveCommand.class);
        registry.putAlias("OctaveFunction",OctaveFunctionFile.class);
        registry.putAlias("OctaveFile",SerializableOctaveFile.class);
        registry.putAlias("OctaveString",SerializableOctaveString.class);
        registry.putAlias("var",SerializableOctaveObject.class);
    }
}
