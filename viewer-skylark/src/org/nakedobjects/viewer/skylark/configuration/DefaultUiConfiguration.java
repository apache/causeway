package org.nakedobjects.viewer.skylark.configuration;

import org.nakedobjects.container.configuration.Configuration;
import org.nakedobjects.container.configuration.ConfigurationFactory;
import org.nakedobjects.viewer.skylark.Viewer;


public class DefaultUiConfiguration implements org.nakedobjects.viewer.skylark.GuiConfiguration {
    private final static int CLASS_ICON_SIZE = 34;
    private final static String CLASS_ICON_SIZE_PARAM = Viewer.PROPERTY_BASE + "class-icon-size";
    private static final Configuration configurationFile;
    private static final String FALLBACK_IMAGE = "Unknown.gif";
    private final static String FALLBACK_PARAM = Viewer.PROPERTY_BASE + "fallback-image";
    private final static String IMAGE_DIRECTORY = "images/";
    private final static String IMAGE_DIRECTORY_PARAM = Viewer.PROPERTY_BASE + "image-directory";
    public static final String PROPERTY_BASE = "viewer.skylark.";
//   private static final String SPECIFICATION_BASE = PROPERTY_BASE + "specification.";
    static {
        configurationFile = ConfigurationFactory.getConfiguration();
    }

    public boolean alsoLoadImageAsFiles() {
        return configurationFile.getBoolean(Viewer.PROPERTY_BASE + "load-images-from-files", true);
    }

    public int classIconSize() {
        return configurationFile.getInteger(CLASS_ICON_SIZE_PARAM, CLASS_ICON_SIZE);
    }

    public String fallbackIconImage() {
        return configurationFile.getString(FALLBACK_PARAM, FALLBACK_IMAGE);
    }

    public String imageDirectory() {
        return configurationFile.getString(IMAGE_DIRECTORY_PARAM, IMAGE_DIRECTORY);
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */