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

package org.apache.isis.core.commons.lang;

import java.io.InputStream;
import java.net.URL;

/**
 * Adapted from Ibatis Common, now with some additional guava stuff.
 */
public class ResourceUtil {
    
    private ResourceUtil(){}

    /**
     * Returns the URL, or null if not available.
     */
    public static URL getResourceURL(final String resource) {

        // try thread's classloader
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL url = classLoader.getResource(resource);
        if (url != null) {
            return url;
        }

        // try this class' classloader
        classLoader = ResourceUtil.class.getClassLoader();
        url = classLoader.getResource(resource);
        if (url != null) {
            return url;
        }

        // try system class loader (could return null)
        // wrapping in a try...catch because when running tests by Maven for a
        // non-existing
        // resource, seems to bomb out. Is okay when run from Eclipse. A bit of
        // a puzzle.
        try {
            return ClassLoader.getSystemResource(resource);
        } catch (final NullPointerException ignore) {
            return null;
        }
    }

    public static InputStream getResourceAsStream(final String resource) {

        // try thread's classloader
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream is = classLoader.getResourceAsStream(resource);
        if (is != null) {
            return is;
        }

        // try this class' classloader
        classLoader = ResourceUtil.class.getClassLoader();
        is = classLoader.getResourceAsStream(resource);
        if (is != null) {
            return is;
        }

        // try system class loader (could return null)
        // have wrapped in a try...catch because for same reason as
        // getResourceURL
        try {
            return ClassLoader.getSystemResourceAsStream(resource);
        } catch (final NullPointerException ignore) {
            return null;
        }
    }

}
