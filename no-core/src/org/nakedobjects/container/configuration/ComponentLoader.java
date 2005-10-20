package org.nakedobjects.container.configuration;

import org.nakedobjects.NakedObjects;

public class ComponentLoader {
    /**
     * Loads a component required by the system, as specfied in the
     * configuration file. If there is no class specified in the configuration
     * file then the default class is used.
     * 
     * @param paramName
     *                   the parameter name to match with the configuration file
     * @param requiredClass
     *                   the type of component that must be specified
     * @return Object the loaded component
     * @throws ConfigurationException
     * @throws ComponentException
     */
    public static Object loadComponent(String paramName, Class requiredClass) throws ConfigurationException, ComponentException {
        return loadComponent(paramName, null, requiredClass, true);
    }

    /**
     * Loads a component required by the system, as specfied in the
     * configuration file. If there is no class specified in the configuration
     * file then the default class is used.
     * 
     * @param paramName
     *                   the parameter name to match with the configuration file
     * @param defaultType
     *                   the name of the class to use for the default component
     * @param requiredClass
     *                   the type of component that must be specified
     * @return Object the loaded component
     * @throws ConfigurationException
     * @throws ComponentException
     */
    public static Object loadComponent(String paramName, Class defaultType, Class requiredClass) throws ConfigurationException,
            ComponentException {
        return loadComponent(paramName, defaultType, requiredClass, true);
    }

    private static Object loadComponent(String paramName, Class defaultType, Class requiredClass, boolean mustLoad)
            throws ConfigurationException, ComponentException {
        String componentName = defaultType == null ? "" : defaultType.getName();
        String className = params().getString(paramName, componentName);

        if (className == null || className.trim().equals("")) {
            if (mustLoad) {
                throw new ConfigurationException("Configuration parameter " + params().referedToAs(paramName) + " must be set");
            } else {
                return null;
            }
        }

        return loadNamedComponent(className, defaultType, requiredClass);
    }

    public static Object loadNamedComponent(String className, Class requiredClass) throws ConfigurationException {
        return loadNamedComponent(className, null, requiredClass);
    }
    
    private static Object loadNamedComponent(String className, Class defaultType, Class requiredClass) throws ConfigurationException {

        Class c = null;

        try {
            c = Class.forName(className);

            if (requiredClass.isAssignableFrom(c)) {
                return c.newInstance();
            } else {
                throw new ConfigurationException("Component class " + className + " must be of the type " + requiredClass);
            }
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException("The component class " + className + " can not be found");
        } catch (InstantiationException e) {
            throw new ConfigurationException("Could not instantiate an object of class " + c.getName() + "; " + e.getMessage());
        } catch (IllegalAccessException e) {
            throw new ConfigurationException("Could not access the class " + c.getName() + "; " + e.getMessage());
        }

    }

    public static Object loadComponentIfSpecified(String paramName, Class requiredClass) throws ConfigurationException,
            ComponentException {
        return loadComponent(paramName, null, requiredClass, false);
    }

    public static Object loadComponentIfSpecified(String paramName, Class defaultType, Class requiredClass)
            throws ConfigurationException, ComponentException {
        return loadComponent(paramName, defaultType, requiredClass, false);
    }

    private static NakedObjectConfiguration params() {
        return NakedObjects.getConfiguration();
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
