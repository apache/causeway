package org.nakedobjects.container.configuration;

import org.nakedobjects.object.NakedObjectRuntimeException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;


public class ConfigurationPropertiesLoader {

    private Properties properties;

    public ConfigurationPropertiesLoader(String pathname, boolean ensureFileLoads) {
        properties = new Properties();
        try {
	        FileInputStream in;
            in = new FileInputStream(pathname);
            properties.load(in);
        } catch (FileNotFoundException e) {
            if(ensureFileLoads) {
                throw new NakedObjectRuntimeException("Could not find configuration file", e);
            }
        } catch (IOException e) {
            throw new NakedObjectRuntimeException("Could not load configuration file", e);
        }
    }

    public static Properties loadProperties(URL resource, boolean mustLoadFile) throws ConfigurationException {
        InputStream in;

        try {
            URLConnection connection = resource.openConnection();
            in = connection.getInputStream();
        } catch (FileNotFoundException e) {
            if (mustLoadFile) {
                throw new ConfigurationException("Could not find configuration resource: " + resource);
            } else {
                return new Properties();
            }
        } catch (IOException e) {
            throw new ConfigurationException("Error reading configuration resource: " + resource, e);
        }

        try {
            Properties p = new Properties();
            p.load(in);

            return p;
        } catch (IOException e) {
            throw new ConfigurationException("Error reading properties" + e.getMessage());
        } finally {
            try {
                in.close();
            } catch (IOException ignore) {}
        }
    }

    public static Properties loadProperties(String resource, boolean mustLoadFile) throws ConfigurationException {

        try {
            URL url = ConfigurationFactory.class.getResource(resource);
            if (url == null) {
                if (mustLoadFile) {
                    throw new ConfigurationException("Configuration resource not found: " + resource);
                } else {
                    return new Properties();
                }
            }
            return loadProperties(url, mustLoadFile);

        } catch (ConfigurationException e) {
            try {
                // try as if it is a file name
                URL url = new URL("file:///" + new File(resource).getAbsolutePath());

                return loadProperties(url, mustLoadFile);
            } catch (SecurityException ee) {
                throw new ConfigurationException("Access not granted to configuration resource: " + resource);
            } catch (MalformedURLException ee) {
                throw new ConfigurationException("Configuration resource not found: " + resource);
            }
        }
    }

    public Properties getProperties() {
        return properties;
    }

/*    public Configuration(String file, boolean mustLoadFile) throws ConfigurationException {
        Properties p = loadProperties(file, mustLoadFile);
        load(p);
    }
    */
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