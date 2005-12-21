package org.nakedobjects.system;

import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.NakedObjectsComponent;
import org.nakedobjects.utility.AboutNakedObjects;
import org.nakedobjects.utility.configuration.PropertiesConfiguration;
import org.nakedobjects.utility.configuration.PropertiesFileLoader;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


public class StartUp {
    private final static Logger LOG = Logger.getLogger(StartUp.class);
    private final Hashtable components = new Hashtable();

    public static void main(String[] args) {
        String configurationFile = args.length > 0 ? args[0] : "nakedobjects.properties";
        StartUp startup = new StartUp();
        startup.start(configurationFile);
    }

    protected void start(String name) {
        LogManager.getRootLogger().setLevel(Level.OFF);
        
        // TODO the configuration should be used in the repository
        PropertiesConfiguration configuration = new PropertiesConfiguration(new PropertiesFileLoader(name, true));

        // set up logging immediately
        PropertyConfigurator.configure(configuration.getProperties("log4j"));
        AboutNakedObjects.logVersion();
        

        LOG.debug("Configuring system using " + name);
        String componentNames = configuration.getString("nakedobjects.components");
        StringTokenizer st = new StringTokenizer(componentNames, ",;/:");
        Properties properties = configuration.getProperties("nakedobjects");
        Vector components = new Vector();
        while (st.hasMoreTokens()) {
            String componentName = ((String) st.nextToken()).trim();

            String componentClass = properties.getProperty("nakedobjects." + componentName);
            LOG.debug("loading core component " + componentName + ": " + componentClass);
            if (componentClass == null) {
                throw new StartupException("No component specified for nakedobjects." + componentName);
            }
            NakedObjectsComponent component = (NakedObjectsComponent) loadComponent(componentClass, NakedObjectsComponent.class);
            if(component instanceof NakedObjects) {
                ((NakedObjects) component).setConfiguration(configuration);
            }
            setProperties(component, "nakedobjects." + componentName, properties);

            component.init();
//            components.addElement(component);
        }

        for(Enumeration e = components.elements(); e.hasMoreElements();) {
            ((NakedObjectsComponent) e.nextElement()).init();
        }
        
        /*
        Vector components = new Vector();
        Enumeration keys = properties.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String component = key.substring("nakedobjects.components".length(), key.indexOf('.', "nakedobjects.components".length() + 1));
            if(!components.contains(component)) {
                components.addElement(component);
            }
        }
        
        try {
            
            Enumeration e = components.elements();
            while (e.hasMoreElements()) {
                String component = (String) e.nextElement();
                
                String containerName = configuration.getString("install." + component);
                LOG.debug("loading core component " + containerName);
                NakedObjects container = (NakedObjects) loadComponent(containerName, NakedObjects.class);
                setProperties(container, "nakedobjects." + "install." + component, properties);
                
            }
            
            
//            container.init();
            
        } catch(StartupException e) {
            LOG.error("Failed to start NOF: " + e.getMessage(), e);
        }
        */
        
        
        
        /*
        Properties properties = configuration.getProperties("nakedobjects.install.container");

        AboutNakedObjects.logVersion();

        LOG.debug("Configuring system using " + name);


        try {
            String containerName = configuration.getString("install." + CONTAINER);
            LOG.debug("loading core component " + containerName);
            NakedObjects container = (NakedObjects) loadComponent(containerName, NakedObjects.class);
            setProperty(container, "configuration", configuration);
            setProperties(container, "nakedobjects." + "install." + CONTAINER, properties);
            
            
            // TODO need to load viewer, server listener etc
    //        setProperties(container, "nakedobjects." + VIEWER, properties);
      //      loadComponent(containerName, NakedObjects.class);
            
            container.init();
            
        } catch(StartupException e) {
            LOG.error("Failed to start NOF: " + e.getMessage(), e);
        }
        */
    }

    private void setProperties(Object object, String prefix, Properties properties) {
        LOG.debug("looking for properties starting with " + prefix);
        Enumeration e = properties.propertyNames();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            if (key.startsWith(prefix) && key.length() > prefix.length() && key.substring(prefix.length() + 1).indexOf('.') == -1) {
                LOG.debug("  property " + key + " (of prefix " + prefix + ")");
                String className = properties.getProperty(key).trim();
                
                Object value;
                if(className.indexOf(',') > 0) {
                    StringTokenizer st = new StringTokenizer(className, ",");
                    Object[] elements = new Object[st.countTokens()];
                    int i = 0;
                    while (st.hasMoreTokens()) {
                        String cls = st.nextToken();
                        elements[i++] =  load(cls, key, properties);
                    }
                    value = elements;
                } else {
                    if(className.equalsIgnoreCase("true")) {
                        value = Boolean.TRUE;
                    } else if (className.equalsIgnoreCase("false")) {
                        value = Boolean.FALSE;
                    } else {
                        value = load(className, key, properties);
                    }
                }
                setProperty(object, key, value);
                
            }
        }
    }

    private void setProperty(Object object, String fieldName, Object value) {
        String field = fieldName.substring(fieldName.lastIndexOf(".") + 1);
        field = Character.toUpperCase(field.charAt(0)) + field.substring(1);
        LOG.debug("    setting " + field + " on " + object);

        Class c = object.getClass();
        LOG.debug("    getting set method for " + field);
        Method setter = null;
        try {
            PropertyDescriptor property = new PropertyDescriptor(field, c, null, "set" + field);
            setter = property.getWriteMethod();
            Class cls = setter.getParameterTypes()[0];
            if(cls.isArray()) {
                int length = Array.getLength(value);
                Object[] array = (Object[]) Array.newInstance(cls.getComponentType(), length);
                System.arraycopy(value,0, array, 0, length);
                //array[0] = value;
                setter.invoke(object, new Object[] { array });
                
            } else {
                setter.invoke(object, new Object[] { value });
            }
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

    private Object load(String className, String name, Properties properties) {
        if(className.startsWith("ref:")) {
            String referencedName = className.substring(4);
            if(components.containsKey(referencedName)) {
                return components.get(referencedName);
            } else {
                throw new StartupException("Could not reference the object names " + referencedName);
            }
        }
        LOG.debug("loading component " + className + " for " + name);
        Object object = loadComponent(className);
        components.put(name, object);
        setProperties(object, name, properties);
        return object;
    }

/*
    public static Object loadComponent(String name, PropertiesConfiguration configuration) {
        String className = configuration.getString(name);
        
        LOG.debug("loading component " + className + " for " + name);
        Object object = loadComponent(className);
        setProperties(object, name, properties);
        return object;
    }
   */ 
    public static Object loadComponent(String className) {
        return loadNamedComponent(className, null, null);
    }

    public static Object loadComponent(String className, Class requiredClass) {
        return loadNamedComponent(className, null, requiredClass);
    }

    public static Object loadNamedComponent(String className, Class defaultType, Class requiredClass) {
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