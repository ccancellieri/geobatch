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

package it.geosolutions.geobatch.global;

import it.geosolutions.geobatch.catalog.Catalog;
import it.geosolutions.geobatch.settings.GBSettingsCatalog;

/**
 * @author Alessio Fabiani, GeoSolutions
 * 
 */
public abstract class CatalogHolder {
    private static Catalog catalog;
    private static GBSettingsCatalog settingsCatalog;

    public synchronized static Catalog getCatalog() {
        return catalog;
    }

    protected synchronized static void setCatalog(Catalog catalog) {
        CatalogHolder.catalog = catalog;
    }

    public static GBSettingsCatalog getSettingsCatalog() {
        return settingsCatalog;
    }

    protected void setSettingsCatalog(GBSettingsCatalog settingsCatalog) {
        CatalogHolder.settingsCatalog = settingsCatalog;
    }

}
