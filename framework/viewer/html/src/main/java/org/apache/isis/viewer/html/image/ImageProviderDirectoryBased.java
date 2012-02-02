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

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public class ImageProviderDirectoryBased implements ImageProvider {

    private final String UNKNOWN_IMAGE = "Default";
    private final String[] EXTENSIONS = { "png", "gif", "jpg", "jpeg" };
    private final Map images = new HashMap();
    private File imageDirectory;
    private String unknownImageFile;

    public void setImageDirectory(final String imageDirectory) {
        this.imageDirectory = new File(imageDirectory);
        if (!this.imageDirectory.exists()) {
            throw new IsisException("No image directory found: " + imageDirectory);
        }
        unknownImageFile = imageFile(UNKNOWN_IMAGE);
    }

    @Override
    public void debug(final DebugBuilder debug) {
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

    private String imageFile(final String imageName) {
        final String[] files = imageDirectory.list(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                final int dot = name.lastIndexOf('.');
                if (dot > 0) {
                    for (final String element : EXTENSIONS) {
                        if (name.substring(0, dot).equalsIgnoreCase(imageName) && name.substring(dot + 1).equalsIgnoreCase(element)) {
                            return true;
                        }
                    }
                }
                return false;
            }
        });

        return files.length == 0 ? null : files[0];
    }

    private String findImage(final ObjectSpecification specification) {
        final String name = specification.getShortIdentifier();
        final String fileName = imageFile(name);
        if (fileName != null) {
            images.put(name, fileName);
            return fileName;
        } else {
            final ObjectSpecification superclass = specification.superclass();
            if (superclass != null) {
                return findImage(superclass);
            } else {
                return unknownImageFile;
            }
        }
    }

    /**
     * For an object, the icon name from the object is return if it is not null,
     * otherwise the specification is used to look up a suitable image name.
     * 
     * @see ObjectAdapter#getIconName()
     * @see #image(ObjectSpecification)
     */
    @Override
    public String image(final ObjectAdapter object) {
        final String iconName = object.getIconName();
        if (iconName != null) {
            return image(iconName);
        } else {
            return image(object.getSpecification());
        }
    }

    @Override
    public String image(final ObjectSpecification specification) {
        final String name = specification.getShortIdentifier();
        final String imageName = (String) images.get(name);
        if (imageName != null) {
            return imageName;
        } else {
            return findImage(specification);
        }
    }

    @Override
    public String image(final String name) {
        final String imageName = (String) images.get(name);
        if (imageName != null) {
            return imageName;
        } else {
            final String fileName = imageFile(name);
            return fileName == null ? unknownImageFile : fileName;
        }
    }

}
