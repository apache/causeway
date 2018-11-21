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

package org.apache.isis.core.commons.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ResourceStreamSourceFileSystem extends ResourceStreamSourceAbstract {

    /**
     * Factory method to guard against nulls
     */
    public static ResourceStreamSource create(final String directory) {
        if(directory == null) {
            return null;
        }
        final File file = new File(directory);
        if(!file.exists()) {
            return null;
        }
        if (!file.isDirectory()) {
            return null;
        }
        try {
            return new ResourceStreamSourceFileSystem(file.getCanonicalPath());
        } catch (IOException e) {
            // shouldn't happen given earlier checks.
            throw new RuntimeException(e);
        }
    }

    private final String directory;

    public ResourceStreamSourceFileSystem(final String directory) {
        this.directory = directory;
    }

    @Override
    protected InputStream doReadResource(final String resourcePath) throws FileNotFoundException {
        final File file = new File(directory, resourcePath);
        return new FileInputStream(file);
    }

    @Override
    public OutputStream writeResource(final String resourcePath) {
        final File file = new File(directory, resourcePath);
        try {
            return new FileOutputStream(file);
        } catch (final FileNotFoundException e) {
            return null;
        }
    }

    @Override
    public String getName() {
        return "file system (directory '" + directory + "')";
    }

}
