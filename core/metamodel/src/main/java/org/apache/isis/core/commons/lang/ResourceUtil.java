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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.isis.commons.internal.context._Context;

public class ResourceUtil {

    private ResourceUtil(){}

    public static InputStream getResourceAsStream(final String resource) {

        // try Isis's classloader
        ClassLoader classLoader = _Context.getDefaultClassLoader();
        InputStream is = classLoader.getResourceAsStream(resource);
        if (is != null) {
            return is;
        }

        // try thread's classloader
        classLoader = Thread.currentThread().getContextClassLoader();
        is = classLoader.getResourceAsStream(resource);
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
    
    /**
     * @param request
     * @return real-path resource from file-system, if any
     */
    public static InputStream getResourceAsStream(final HttpServletRequest request) {
        final String realPath = request.getSession().getServletContext().getRealPath(request.getServletPath());
        if (realPath == null) {
            return null;
        }
        try {
            return new FileInputStream(realPath);
        } catch (final FileNotFoundException e) {
            return null;
        }
    }

}
