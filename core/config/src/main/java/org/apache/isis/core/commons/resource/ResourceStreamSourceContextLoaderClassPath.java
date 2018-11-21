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

import java.io.InputStream;

import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.resources._Resources;

/**
 * Loads the properties from the ContextClassLoader.
 *
 * <p>
 * If this class is on the system class path, then the class loader obtained
 * from this.getClassLoader() won't be able to load resources from the
 * application class path.
 */
public class ResourceStreamSourceContextLoaderClassPath extends ResourceStreamSourceAbstract {

    public static ResourceStreamSourceContextLoaderClassPath create() {
        return create("");
    }

    public static ResourceStreamSourceContextLoaderClassPath create(final String prefix) {
        return new ResourceStreamSourceContextLoaderClassPath(prefix);
    }

    private final String prefix;

    private ResourceStreamSourceContextLoaderClassPath(final String prefix) {
        this.prefix = prefix;
    }

    @Override
    protected InputStream doReadResource(final String resourcePath) {
        final ClassLoader classLoader = _Context.getDefaultClassLoader();
        final String path = _Resources.combinePath(prefix, resourcePath);
        return classLoader.getResourceAsStream(path);
    }

    @Override
    public String getName() {
        return "context loader classpath";
    }

}
