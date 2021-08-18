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
package org.apache.isis.commons.internal.resources;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.springframework.lang.Nullable;

import org.apache.isis.commons.internal.base._Bytes;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.context._Context;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Utilities for storing and locating resources.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 * @since 2.0
 */
public final class _Resources {

    // -- CLASS PATH RESOURCE LOADING

    /**
     * Returns the resource with path {@code resourceName} relative to {@code contextClass} as an InputStream.
     * @param contextClass
     * @param resourceName
     * @return An input stream for reading the resource, or null if the resource could not be found.
     */
    @SneakyThrows
    public static @Nullable InputStream load(
            final @NonNull Class<?> contextClass,
            final @NonNull String resourceName) {

        val absoluteResourceName = resolveName(resourceName, contextClass);
        return _Context.getDefaultClassLoader().getResourceAsStream(absoluteResourceName);
    }

    /**
     * Returns the resource with path {@code resourceName} relative to {@code contextClass} as a String
     * conforming to the given {@code charset}.
     * @param contextClass
     * @param resourceName
     * @param charset
     * @return The resource as a String, or null if the resource could not be found.
     * @throws IOException
     */
    public static @Nullable String loadAsString(
            final @NonNull Class<?> contextClass,
            final @NonNull String resourceName,
            final @NonNull Charset charset) throws IOException {

        val inputStream = load(contextClass, resourceName);
        return _Strings.ofBytes(_Bytes.of(inputStream), charset);
    }

    /**
     * Shortcut using Charset UTF-8, see {@link #loadAsString(Class, String, Charset)}
     */
    public static @Nullable String loadAsStringUtf8(
            final @NonNull Class<?> contextClass,
            final @NonNull String resourceName) throws IOException {
        return loadAsString(contextClass, resourceName, StandardCharsets.UTF_8);
    }

    /**
     * @param resourceName
     * @return The resource location as an URL, or null if the resource could not be found.
     */
    public static @Nullable URL getResourceUrl(
            final @NonNull Class<?> contextClass,
            final @NonNull String resourceName) {

        final String absoluteResourceName = resolveName(resourceName, contextClass);

        return _Context.getDefaultClassLoader().getResource(absoluteResourceName);
    }


    // -- LOCAL vs EXTERNAL resource path

    private static final Predicate<String> externalResourcePattern =
            Pattern.compile("^\\w+?://.*$").asPredicate();

    /**
     * Returns whether the {@code resourcePath} is intended local and relative
     * to the web-app's context root.
     * @param resourcePath
     */
    public static boolean isLocalResource(final @NonNull String resourcePath) {
        return !externalResourcePattern.test(resourcePath);
    }

    /**
     * To build a path from chunks {@code 'a' + 'b' -> 'a/b'}, also handling cases eg.
     * {@code 'a/' + '/b' -> 'a/b'}
     *
     * @param extendee
     * @param suffix
     */
    public static String combinePath(final @Nullable String extendee, final @Nullable String suffix) {
        return _Strings.combineWithDelimiter(extendee, suffix, "/");
    }

    // -- HELPER

    /*
     *
     * Adapted copy of JDK 8 Class::resolveName
     */
    private static String resolveName(String name, final Class<?> contextClass) {
        if (name == null) {
            return name;
        }
        if (!name.startsWith("/")) {
            Class<?> c = contextClass;
            while (c.isArray()) {
                c = c.getComponentType();
            }
            String baseName = c.getName();
            int index = baseName.lastIndexOf('.');
            if (index != -1) {
                name = baseName.substring(0, index).replace('.', '/')
                        +"/"+name;
            }
        } else {
            name = name.substring(1);
        }
        return name;
    }




}
