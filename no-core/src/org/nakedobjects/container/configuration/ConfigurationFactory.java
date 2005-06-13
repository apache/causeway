package org.nakedobjects.container.configuration;



/**
 * Holds the available properties for this Naked Objects session.
 * @deprecated use NakedObjects.getConfiguraton
 */
public final class ConfigurationFactory {
    private static Configuration instance;

    public static void setConfiguration(Configuration instance) {
        ConfigurationFactory.instance = instance;
    }
    
    /**
	 * Expose as a .NET property
	 * @property
 	 */
	public static void set_Configuration(Configuration instance) {
		setConfiguration(instance);
	}

    /**
     * Returns the singleton that is to be used to access the properties.
     * @deprecated use NakedObjects.getConfiguraton
     */
    public final static Configuration getConfiguration() {
        if(instance == null) {
            throw new IllegalStateException("No configuration set up");
        }
        return instance;
    }

	/**
	 * Expose as a .NET property
     * @deprecated use NakedObjects.getConfiguraton
	 */
	public final static Configuration get_Configuration() {
		return getConfiguration();
	}

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects
 * Group is Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */