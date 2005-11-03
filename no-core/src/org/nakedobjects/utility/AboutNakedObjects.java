package org.nakedobjects.utility;

import org.apache.log4j.Logger;


public class AboutNakedObjects {

    public static String getVersion() {
        return "Version " + select("%VERSION%", "2");
    }
    
    public static String getImageName() {
        return select("%IMAGE_FILE%", "logo.jpg");
    }
    
    private static String select(String value, String defaultValue) {
        return value.startsWith("%") && value.endsWith("%") ? defaultValue : value;
    }

    public static String getName() {
        return select("%NAME%", "Naked Objects Framework");
    }
    
    public static String getBuildId() {
        return select("%BUILD_ID%", "(temporary build)");
    }
    
    
    public static String getCopyrightNotice() {
        return select("%COPYRIGHT_NOTICE%", "Copyright Naked Objects Group");
    }
    
    public static void main(String[] args) {
        System.out.println(getName() + " version " + getVersion());
        System.out.println("Build: " + getBuildId());
        System.out.println(getCopyrightNotice());
    }

    public static void logVersion() {
        Logger log = Logger.getLogger("Naked Objects");
        log.info(getName());
        log.info(getVersion());
        log.info(getBuildId());    
    }
}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

The authors can be contacted via www.nakedobjects.org (the
registered address of Naked Objects Group is Kingsway House, 123 Goldworth
Road, Woking GU21 1NR, UK).
*/