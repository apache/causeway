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


package org.apache.isis.extensions.html.image;

import java.io.File;
import java.io.FilenameFilter;

import org.apache.isis.commons.exceptions.IsisException;

public class ImageProviderDirectoryBasedReworked extends ImageProviderAbstract {

    private File imageDirectory;
    
    public void setImageDirectory(final String imageDirectory) {
        this.imageDirectory = new File(imageDirectory);
        if (!this.imageDirectory.exists()) {
            throw new IsisException("No image directory found: " + imageDirectory);
        }
    }

    protected String findImage(final String imageName, final String[] extensions) {
        final String[] files = imageDirectory.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                int dot = name.lastIndexOf('.');
                if (dot > 0) {
                    String nameWithoutExtension = name.substring(0, dot);
                    String nameExtension = name.substring(dot + 1);
                    for (int i = 0; i < extensions.length; i++) {
                        if (nameWithoutExtension.equalsIgnoreCase(imageName) && 
                            nameExtension.equalsIgnoreCase(extensions[i])) {
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


