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

import org.apache.isis.core.commons.exceptions.IsisException;

public class ImageProviderDirectoryBasedReworked extends ImageProviderAbstract {

    private File imageDirectory;

    public void setImageDirectory(final String imageDirectory) {
        this.imageDirectory = new File(imageDirectory);
        if (!this.imageDirectory.exists()) {
            throw new IsisException("No image directory found: " + imageDirectory);
        }
    }

    @Override
    protected String findImage(final String imageName, final String[] extensions) {
        final String[] files = imageDirectory.list(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                final int dot = name.lastIndexOf('.');
                if (dot > 0) {
                    final String nameWithoutExtension = name.substring(0, dot);
                    final String nameExtension = name.substring(dot + 1);
                    for (final String extension : extensions) {
                        if (nameWithoutExtension.equalsIgnoreCase(imageName) && nameExtension.equalsIgnoreCase(extension)) {
                            return true;
                        }
                    }
                }
                return false;
            }
        });

        return files.length == 0 ? null : files[0];
    }

}
