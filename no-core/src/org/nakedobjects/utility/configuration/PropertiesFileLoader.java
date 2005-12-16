package org.nakedobjects.utility.configuration;

import org.nakedobjects.utility.NakedObjectRuntimeException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;


public class PropertiesFileLoader implements ConfigurationLoader {
    private Properties properties;

    public PropertiesFileLoader(String pathname, boolean ensureFileLoads) {
        properties = new Properties();
        try {
	        FileInputStream in;
            File file = new File(pathname);
            in = new FileInputStream(file);
            properties.load(in);
        } catch (FileNotFoundException e) {
            if(ensureFileLoads) {
                throw new NakedObjectRuntimeException("Could not find configuration file: " + pathname, e);
            }
        } catch (IOException e) {
            throw new NakedObjectRuntimeException("Could not load configuration file: " + pathname, e);
        }
    }

    public Properties getProperties() {
        return properties;
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