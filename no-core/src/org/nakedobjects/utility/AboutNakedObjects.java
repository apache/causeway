package org.nakedobjects.utility;

import org.apache.log4j.Logger;


public class AboutNakedObjects {
    private static String applicationCopyrightNotice;
    private static String applicationName;
    private static String applicationVersion;

    public static String getApplicationCopyrightNotice() {
        return applicationCopyrightNotice;
    }

    public static String getApplicationName() {
        return applicationName;
    }

    public static String getApplicationVersion() {
        return applicationVersion;
    }

    public static String getFrameworkBuild() {
        return select("%BUILD_ID%", "000000");
    }

    public static String getFrameworkCopyrightNotice() {
        return select("%COPYRIGHT_NOTICE%", "Copyright Naked Objects Group");
    }

    public static String getFrameworkName() {
        return select("%NAME%", "Naked Objects Framework");
    }

    public static String getImageName() {
        return select("%IMAGE_FILE%", "logo.jpg");
    }

    public static String getFrameworkVersion() {
        return "Version " + select("%VERSION%", "2");
    }

    public static void logVersion() {
        Logger log = Logger.getLogger("Naked Objects");
        log.info(getFrameworkName());
        log.info(getFrameworkVersion() + getFrameworkBuild());
        if(getApplicationName() != null) {
        log.info(getApplicationName());
        }
        if(getApplicationVersion() != null) {
            log.info(getApplicationVersion());
        }
    }

    public static void main(String[] args) {
        System.out.println(getFrameworkName() + " version " + getFrameworkVersion());
        System.out.println("Build: " + getFrameworkBuild());
        System.out.println(getFrameworkCopyrightNotice());
    }

    private static String select(String value, String defaultValue) {
        return value.startsWith("%") && value.endsWith("%") ? defaultValue : value;
    }

    public static void setApplicationCopyrightNotice(String applicationCopyrightNotice) {
        AboutNakedObjects.applicationCopyrightNotice = applicationCopyrightNotice;
    }

    public static void setApplicationName(String applicationName) {
        AboutNakedObjects.applicationName = applicationName;
    }

    public static void setApplicationVersion(String applicationVersion) {
        AboutNakedObjects.applicationVersion = applicationVersion;
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is
 * Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */