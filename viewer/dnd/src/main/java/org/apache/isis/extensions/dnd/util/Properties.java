/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */


package org.apache.isis.extensions.dnd.util;

import java.util.StringTokenizer;

import org.apache.isis.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.config.ConfigurationConstants;
import org.apache.isis.core.metamodel.config.ConfigurationException;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.extensions.dnd.drawing.Location;
import org.apache.isis.extensions.dnd.drawing.Size;
import org.apache.isis.extensions.dnd.view.ViewSpecification;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.userprofile.Options;


public class Properties {

    public static final String PROPERTY_BASE = ConfigurationConstants.ROOT + "viewer.dnd.";

    public static Size getSize(final String name, final Size defaultSize) {
        String initialSize = optionFor(name);
        if (initialSize == null) {
              initialSize = IsisContext.getConfiguration().getString(name);
        }
        if (initialSize != null) {
            final StringTokenizer st = new StringTokenizer(initialSize, "x");
            if (st.countTokens() == 2) {
                int width = 0;
                int height = 0;
                width = Integer.valueOf(st.nextToken().trim()).intValue();
                height = Integer.valueOf(st.nextToken().trim()).intValue();
                return new Size(width, height);
            } else {
                throw new ConfigurationException("Size not specified correctly in " + name + ": " + initialSize);
            }
        }
        return defaultSize;
    }
    
    public static void saveSizeOption(final String name, final Size size) {
        String value = size.getWidth() + "x" + size.getHeight();
        IsisContext.getUserProfile().getOptions().addOption(name, value);
    }

    public static Location getLocation(final String name, final Location defaultLocation) {
        String initialLocation = optionFor(name);
        if (initialLocation == null) {
            initialLocation = IsisContext.getConfiguration().getString(name);
        }
        if (initialLocation != null) {
            final StringTokenizer st = new StringTokenizer(initialLocation, ",");
            if (st.countTokens() == 2) {
                int x = 0;
                int y = 0;
                x = Integer.valueOf(st.nextToken().trim()).intValue();
                y = Integer.valueOf(st.nextToken().trim()).intValue();
                return new Location(x, y);
            } else {
                throw new ConfigurationException("Location not specified correctly in " + name + ": " + initialLocation);
            }
        }
        return defaultLocation;
    }

    private static String optionFor(final String name) {
        return IsisContext.inSession() ? IsisContext.getUserProfile().getOptions().getString(name) : null;
    }
    
    public static void saveLocationOption(final String name, final Location location) {
        String value = location.getX() + "," + location.getY();
        IsisContext.getUserProfile().getOptions().addOption(name, value);
    }

    public static String getString(String name) {
        String value = optionFor(PROPERTY_BASE +name);
        if (value == null) {
            value = IsisContext.getConfiguration().getString(PROPERTY_BASE + name);
        }
        return value;
    }

    public static void setStringOption(String name, String value) {
        IsisContext.getUserProfile().getOptions().addOption(PROPERTY_BASE +name, value);
    }

    public static Options getOptions(String name) {
        return IsisContext.getUserProfile().getOptions().getOptions(name);
    }

    public static String getDefaultIconViewOptions() {
        return getString("view.icon-default");
    }
    
    public static String getDefaultObjectViewOptions() {
        return getString("view.object-default");
    }
    
    public static String getDefaultCollectionViewOptions() {
        return getString("view.collection-default");
    }
    
    public static Options getViewConfigurationOptions(ViewSpecification specification) {
        Options settingsOptions = getOptions("views.configuration");
        String specificationName = specification.getName();
        return settingsOptions.getOptions(specificationName);
    }

    public static Options getDefaultViewOptions(ObjectSpecification specification) {
        Options settingsOptions = getOptions("views.type-default");
        String name;
        if (specification.isCollection()) {
            name = "collection:" + specification.getFacet(TypeOfFacet.class).valueSpec().getFullName();
        } else {
            name = specification.getFullName();
        }
        Options viewOptions = settingsOptions.getOptions(name);
        return viewOptions;
    }

    public static Options getUserViewSpecificationOptions(String specificationName) {
        Options settingsOptions = getOptions("views.user-defined");
        return settingsOptions.getOptions(specificationName);
    }

    public static Object loadClass(Options viewOptions, String name) {
        String specificationName = viewOptions.getString(name);
        if (specificationName != null) {
            try {
                Class<?> specificationClass = Class.forName(specificationName);
                return specificationClass.newInstance();
            } catch (ClassNotFoundException e) {
                throw new ViewerException(e);
            } catch (InstantiationException e) {
                throw new ViewerException(e);
            } catch (IllegalAccessException e) {
                throw new ViewerException(e);
            }
        }
        return null;
    }

}

