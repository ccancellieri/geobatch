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

package it.geosolutions.geobatch.configuration.flow.file;

import java.io.File;

import it.geosolutions.geobatch.catalog.file.FileBasedCatalogImpl;
import it.geosolutions.geobatch.catalog.impl.BaseDescriptableConfiguration;
import it.geosolutions.geobatch.configuration.CatalogConfiguration;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.tools.commons.file.Path;


/**
 * A Conf for the Catalog based on xml marshalled files.
 *
 * @author Simone Giannecchini, GeoSolutions
 * @author Alessio Fabiani, GeoSolutions
 *
 * @todo: take a look to
 * @see ActionConfiguration
 */
public class FileBasedCatalogConfiguration extends BaseDescriptableConfiguration implements CatalogConfiguration
{
    /**
     * workingDirectory: this attribute represents the configuring directory for this flow.
     * It can be relative to the catalog.xml directory or absolute.
     * <P>
     * <B>Attention: the configuring directory should be different from the one containing the configuration files.</B>
     * @uml.property  name="workingDirectory"
     */
    private String workingDirectory;

    // private List<FlowConfiguration> flowConfigurations;

    public FileBasedCatalogConfiguration(String id, String name, String description, boolean dirty)
    {
        super(id, name, description, dirty);
    }

    /**
     * Getter for the workingDirectory
     */
    public String getWorkingDirectory()
    {
        return workingDirectory;
    }

    /**
     * Setter for the workingDirectory.
     *
     * @param workingDirectory
     */
    public void setWorkingDirectory(String workingDirectory)
    {
        this.workingDirectory = workingDirectory;
    }

    /**
     * Obtaining the Absolute path of the working dir
     *
     * @param working_dir
     *            the relative (or absolute) path to absolutize
     * @note it should be a sub-dir of ...
     * @TODO open a ticket to get getBaseDirectory() into Catalog interface
     */
    public static String getAbsolutePath(String working_dir) /* throws FileNotFoundException */
    {
        FileBasedCatalogImpl c = (FileBasedCatalogImpl) CatalogHolder.getCatalog();
        File fo = Path.findLocation(working_dir, c.getBaseDirectory());
        if (fo != null)
        {
            return fo.toString();
        }
        else
        {
            // TODO LOG throw new FileNotFoundException("Unable to locate the working dir");
            // throw new FileNotFoundException();
            return null;
        }
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "[" + "id:" + getId() +
            ", workingDirectory:" + getWorkingDirectory() + ", name:" +
            getName() + "]";
    }
}
