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
package org.apache.isis.subdomains.base.applib.utils;

/**
 * @since 2.0 {@index}
 */
public final class ClassUtils {

    private ClassUtils(){}

    @SuppressWarnings("unchecked")
    public static <T> Class<? extends T> load(final String clsName, final Class<T> cls)  {
        Class<?> clsx;
        try {
            clsx = Thread.currentThread().getContextClassLoader().loadClass(clsName);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Class '" + clsName + "' not found");
        }
        if (!cls.isAssignableFrom(clsx)) {
            throw new IllegalArgumentException("Class '" + clsName + "' not a subclass of " + cls.getName());
        }
        return (Class<? extends T>) clsx;
    }

}
