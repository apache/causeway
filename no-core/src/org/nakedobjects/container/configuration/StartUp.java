package org.nakedobjects.container.configuration;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.system.AboutNakedObjects;
import org.nakedobjects.utility.StartupException;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


public class StartUp {
    private static final Logger LOG = Logger.getLogger(StartUp.class);
    private static final String CONTAINER = "container";
    private static final String VIEWER = "viewer";

    public static void main(String[] args) {
        LogManager.getRootLogger().setLevel(Level.OFF);
        
        String name = args.length > 0 ? args[0] : "nakedobjects.properties";
        Configuration configuration = new Configuration(new ConfigurationPropertiesLoader(name, true));

        // set up logging immediately
        PropertyConfigurator.configure(configuration.getProperties("log4j"));
        Properties properties = configuration.getProperties("nakedobjects.container");

        AboutNakedObjects.logVersion();

        LOG.debug("Configuring system using " + name);


        try {
            String containerName = configuration.getString(CONTAINER);
            LOG.debug("loading core component " + containerName);
            NakedObjects container = (NakedObjects) loadComponent(containerName, NakedObjects.class);
            setProperty(container, "configuration", configuration);
            setProperties(container, "nakedobjects." + CONTAINER, properties);
            
            
            // TODO need to load viewer, server listener etc
    //        setProperties(container, "nakedobjects." + VIEWER, properties);
      //      loadComponent(containerName, NakedObjects.class);
            
            container.init();
        } catch(StartupException e) {
            LOG.error("Failed to start NOF: " + e.getMessage(), e);
        }
    }

    private static void setProperties(Object object, String prefix, Properties properties) {
        LOG.debug("looking for properties starting with " + prefix);
        Enumeration e = properties.propertyNames();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            if (key.startsWith(prefix) && key.length() > prefix.length() && key.substring(prefix.length() + 1).indexOf('.') == -1) {
                LOG.debug("  property " + key + " (of prefix " + prefix + ")");
                String className = properties.getProperty(key).trim();
                
                Object value;
                if(className.equalsIgnoreCase("true")) {
                   value = Boolean.valueOf(true);
                } else if (className.equalsIgnoreCase("false")) {
                    value = Boolean.valueOf(false);
                } else {
                    value = load(className, key, properties);
                }
                setProperty(object, key, value);
            }
        }
    }

    private static void setProperty(Object object, String fieldName, Object value) {
        String field = fieldName.substring(fieldName.lastIndexOf(".") + 1);
        field = Character.toUpperCase(field.charAt(0)) + field.substring(1);
        LOG.debug("    setting " + field + " on " + object);

        Class c = object.getClass();
        LOG.debug("    getting set method for " + field);
        Method setter = null;
        try {
            PropertyDescriptor property = new PropertyDescriptor(field, c, null, "set" + field);
            setter = property.getWriteMethod();
            
            //setter = c.getMethod("set" + field, new Class[] { value.getClass() });
            setter.invoke(object, new Object[] { value });
            LOG.debug("  set " + field + " with " +  value.getClass());
        } catch (SecurityException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
         } catch (IllegalArgumentException e) {
             throw new StartupException(e.getMessage() + ": can't invoke " + setter.getName() + " with instance of " + value.getClass().getName());
        } catch (IllegalAccessException e) {
            throw new StartupException(e.getMessage() + ": can't access " + setter.getName());
        } catch (InvocationTargetException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IntrospectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static Object load(String className, String prefix, Properties properties) {
        LOG.debug("loading component " + className + " for " + prefix);
        Object object = loadComponent(className);
        setProperties(object, prefix, properties);
        return object;
    }

    private static Object loadComponent(String className) {
        return loadNamedComponent(className, null, null);
    }

    private static Object loadComponent(String className, Class requiredClass) {
        return loadNamedComponent(className, null, requiredClass);
    }

    private static Object loadNamedComponent(String className, Class defaultType, Class requiredClass) {

        Class c = null;

        try {
            c = Class.forName(className);

            if (requiredClass == null || requiredClass.isAssignableFrom(c)) {
                return c.newInstance();
            } else {
                throw new StartupException("Component class " + className + " must be of the type " + requiredClass);
            }
        } catch (ClassNotFoundException e) {
            throw new StartupException("The component class " + className + " can not be found");
        } catch (InstantiationException e) {
            throw new StartupException("Could not instantiate an object of class " + c.getName() + "; " + e.getMessage());
        } catch (IllegalAccessException e) {
            throw new StartupException("Could not access the class " + c.getName() + "; " + e.getMessage());
        }

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