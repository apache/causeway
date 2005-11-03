package org.nakedobjects.utility;

import java.awt.Color;
import java.awt.Font;


public interface NakedObjectConfiguration {

    /**
     * Gets the boolean value for the specified name where no value or 'on' will
     * result in true being returned; anything gives false. If no boolean
     * property is specified with this name then false is returned.
     * 
     * @param name
     *                       the property name
     */
    boolean getBoolean(String name);

    /**
     * Gets the boolean value for the specified name. If no property is
     * specified with this name then the specified default boolean value is
     * returned.
     * 
     * @param name
     *                       the property name
     * @param defaultValue
     *                       the value to use as a default
     */
    boolean getBoolean(String name, boolean defaultValue);

    /**
     * Gets the color for the specified name. If no color property is specified
     * with this name then null is returned.
     * 
     * @param name
     *                       the property name
     */
    Color getColor(String name);

    /**
     * Gets the color for the specified name. If no color property is specified
     * with this name then the specified default color is returned.
     * 
     * @param name
     *                       the property name
     * @param defaultValue
     *                       the value to use as a default
     */
    Color getColor(String name, Color defaultValue);

    /**
     * Gets the font for the specified name. If no font property is specified
     * with this name then null is returned.
     * 
     * @param name
     *                       the property name
     */
    Font getFont(String name);

    /**
     * Gets the font for the specified name. If no font property is specified
     * with this name then the specified default font is returned.
     * 
     * @param name
     *                       the property name
     * @param defaultValue
     *                       the color to use as a default
     */
    Font getFont(String name, Font defaultValue);

    /**
     * Gets the number value for the specified name. If no property is specified
     * with this name then 0 is returned.
     * 
     * @param name
     *                       the property name
     */
    int getInteger(String name);

    /**
     * Gets the nimber value for the specified name. If no property is specified
     * with this name then the specified default number value is returned.
     * 
     * @param name
     *                       the property name
     * @param defaultValue
     *                       the value to use as a default
     */
    int getInteger(String name, int defaultValue);

    /**
     * Returns the configuration property with the specified name. If there is
     * no matching property then null is returned.
     */
    String getString(String name);

    String getString(String name, String defaultValue);

    boolean hasProperty(String name);
    
    /**
     * Returns as a String what the named property is refered to as.  For example in a
     * simple properties file the property z might be specified in the file as x.y.z.  
     */
    String referedToAs(String name);

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