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

package org.apache.isis.viewer.html.image;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public abstract class ImageProviderAbstract implements ImageProvider {

    private final String DEFAULT_IMAGE = "Default";

    /**
     * The extensions we'll search for.
     */
    private final String[] EXTENSIONS = { "png", "gif", "jpg", "jpeg" };

    private final Map<String, String> images = new HashMap<String, String>();

    @Override
    public final String image(final ObjectAdapter object) {

        if (object == null) {
            return image((String) null);
        }

        final String iconName = object.getIconName();
        if (iconName != null) {
            return image(iconName);
        } else {
            return image(object.getSpecification());
        }
    }

    @Override
    public final String image(final ObjectSpecification specification) {

        if (specification == null) {
            return image((String) null);
        }

        final String specShortName = specification.getShortIdentifier();
        final String imageName = image(specShortName);
        if (imageName != null) {
            return imageName;
        }

        // search up the hierarchy
        return image(specification.superclass());
    }

    @Override
    public String image(final String name) {

        if (name == null) {
            return findImage(DEFAULT_IMAGE, EXTENSIONS);
        }

        // look up from cache
        String imageName = images.get(name);
        if (imageName != null) {
            return imageName;
        }

        // delegate to subclass to see if can find the image.
        imageName = findImage(name, EXTENSIONS);

        if (imageName != null) {
            // cache and return
            images.put(name, imageName);
            return imageName;
        }

        // ie loop round to return the default.
        return image((String) null);
    }

    @Override
    public final void debug(final DebugBuilder debug) {
        debug.appendTitle("Image Lookup");
        debug.indent();
        final Iterator<String> keys = images.keySet().iterator();
        while (keys.hasNext()) {
            final Object key = keys.next();
            final Object value = images.get(key);
            debug.appendln(key + " -> " + value);
        }
        debug.unindent();
    }

    /**
     * Hook method for subclass to actually return the image, else <tt>null</tt>
     * .
     * 
     * @param className
     *            - the short name of the class to search for.
     * @param extensions
     *            - the extensions to search for.
     */
    protected abstract String findImage(final String className, String[] extensions);

}
