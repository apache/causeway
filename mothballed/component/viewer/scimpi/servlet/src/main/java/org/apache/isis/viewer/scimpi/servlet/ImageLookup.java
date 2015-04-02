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

package org.apache.isis.viewer.scimpi.servlet;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

/**
 * ImageLookup provides an efficient way of finding the most suitable image to
 * use. It ensures that an image is always available, providing a default image
 * if needed. All requests are cached to improve performance.
 */
// TODO allow for multiple extension types
public class ImageLookup {
    private static final Logger LOG = LoggerFactory.getLogger(ImageLookup.class);
    private static final String UNKNOWN_IMAGE = "Default";
    private static final String[] EXTENSIONS = { "png", "gif", "jpg", "jpeg" };
    private static final Map images = new HashMap();
    private static String imageDirectory;
    // private static String unknownImageFile;
    private static ServletContext context;

    public static void setImageDirectory(final ServletContext context, String imageDirectory) {
        LOG.debug("image directory required for: " + imageDirectory);
        ImageLookup.context = context;
        imageDirectory = (imageDirectory.startsWith("/") ? "" : "/") + imageDirectory + "/";
        final Set resourcePaths = context.getResourcePaths(imageDirectory);
        if (resourcePaths == null || resourcePaths.size() == 0) {
            //throw new IsisException("No image directory found: " + imageDirectory);
            LOG.warn("No image directory found: " + imageDirectory);
        }
        LOG.info("image directory set to: " + imageDirectory);
        ImageLookup.imageDirectory = imageDirectory;
    }

    public static void debug(final DebugString debug) {
        debug.appendTitle("Image Lookup");
        debug.indent();
        final Iterator keys = images.keySet().iterator();
        while (keys.hasNext()) {
            final Object key = keys.next();
            final Object value = images.get(key);
            debug.appendln(key + " -> " + value);
        }
        debug.unindent();
    }

    private static String imageFile(final String imageName, final String contextPath) {
        for (final String element : EXTENSIONS) {
            URL resource;
            try {
                final String imagePath = imageDirectory + imageName + "." + element;
                resource = context.getResource(imagePath);
                if (resource != null) {
                    LOG.debug("image found at " + contextPath + imagePath);
                    return contextPath + imagePath;
                }
                final URL onClasspath = ImageLookup.class.getResource(imagePath);
                if (onClasspath != null) {
                    LOG.debug("image found on classpath " + onClasspath);
                    return contextPath + imagePath;
                }
            } catch (final MalformedURLException ignore) {
            }
        }
        return null;
    }

    private static String findImage(final ObjectSpecification specification, final String contextPath) {
        String path = findImageFor(specification, contextPath);
        if (path == null) {
            path = imageFile(UNKNOWN_IMAGE, contextPath);
        }
        return path;
    }

    private static String findImageFor(final ObjectSpecification specification, final String contextPath) {
        final String name = specification.getShortIdentifier();
        final String fileName = imageFile(name, contextPath);
        if (fileName != null) {
            images.put(name, fileName);
            return fileName;
        } else {
            for (final ObjectSpecification interfaceSpec : specification.interfaces()) {
                final String path = findImageFor(interfaceSpec, contextPath);
                if (path != null) {
                    return path;
                }
            }
            final ObjectSpecification superclass = specification.superclass();
            if (superclass != null) {
                return findImageFor(superclass, contextPath);
            } else {
                return null;
            }
        }
    }

    /**
     * For an object, the icon name from the object is return if it is not null,
     * otherwise the specification is used to look up a suitable image name.
     * 
     * @param contextPath
     * 
     * @see ObjectAdapter#getIconName()
     * @see #imagePath(ObjectSpecification)
     */
    /*
     * public static String imagePath(ObjectAdapter object) { String iconName =
     * object.getIconName(); if (iconName != null) { return imagePath(iconName);
     * } else { return imagePath(object.getSpecification()); } }
     */
    public static String imagePath(final ObjectSpecification specification, final String contextPath) {
        final String name = specification.getShortIdentifier();
        final String imageName = (String) images.get(name);
        if (imageName != null) {
            return imageName;
        } else {
            return findImage(specification, contextPath);
        }
    }

    /*
     * public static String imagePath(String name) { String imageName = (String)
     * images.get(name); if (imageName != null) { return (String) imageName; }
     * else { String fileName = imageFile(name); return fileName == null ?
     * unknownImageFile : fileName; } }
     */

    public static String imagePath(final ObjectAdapter object, final String contextPath) {
        final String name = object.getIconName();
        final String imageName = (String) images.get(name);
        if (imageName != null) {
            return imageName;
        } else {
            final String imageFile = imageFile(name, contextPath);
            if (imageFile != null) {
                return imageFile;
            } else {
                return findImage(object.getSpecification(), contextPath);
            }
        }
    }
}
