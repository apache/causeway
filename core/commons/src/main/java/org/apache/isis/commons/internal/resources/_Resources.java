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

import javax.annotation.Nullable;

import org.apache.isis.commons.internal.base._Bytes;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.context._Context;

import static org.apache.isis.commons.internal.base._With.ifPresentElseThrow;
import static org.apache.isis.commons.internal.base._With.requires;

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
    public static InputStream load(Class<?> contextClass, String resourceName) {

        requires(contextClass, "contextClass");
        requires(resourceName, "resourceName");

        final String absoluteResourceName = resolveName(resourceName, contextClass);

        return _Context.getDefaultClassLoader()
                .getResourceAsStream(absoluteResourceName);
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
    public static String loadAsString(Class<?> contextClass, String resourceName, Charset charset) throws IOException {
        final InputStream is = load(contextClass, resourceName);
        return _Strings.ofBytes(_Bytes.of(is), charset);
    }
    
    /**
     * Shortcut using Charset UTF-8, see {@link #loadAsString(Class, String, Charset)} 
     */
    public static String loadAsStringUtf8(Class<?> contextClass, String resourceName) throws IOException {
        return loadAsString(contextClass, resourceName, StandardCharsets.UTF_8);
    }

    /**
     * @param resourceName
     * @return The resource location as an URL, or null if the resource could not be found.
     */
    public static URL getResourceUrl(Class<?> contextClass, String resourceName) {
        requires(resourceName, "resourceName");
        final String absoluteResourceName = resolveName(resourceName, contextClass);
        return _Context.getDefaultClassLoader().getResource(absoluteResourceName);
    }

    // -- CONTEXT PATH RESOURCE

    /**
     * @return context-path resource (if any) as stored previously by {@link #putContextPathIfPresent(String)}
     */
    public final static String getContextPathIfAny() {
        final _Resources_ContextPath resource = _Context.getIfAny(_Resources_ContextPath.class);
        return resource!=null ? resource.getContextPath() : null;
    }

    /**
     * Stores the {@code contextPath} as an application scoped resource-object.
     * If {@code contextPath} is null or an empty String, no path-resource object is stored.
     * @param contextPath
     * @throws IllegalArgumentException if an non-empty contextPath evaluates to being
     * equivalent to the root-path '/'
     */
    public final static void putContextPathIfPresent(String contextPath) {
        if(!_Strings.isEmpty(contextPath)) {
            _Context.put(_Resources_ContextPath.class, new _Resources_ContextPath(contextPath), false);
        }
    }

    public final static String prependContextPathIfPresent(String path) {

        if(path==null) {
            return null;
        }

        final String contextPath = getContextPathIfAny();

        if(contextPath==null) {
            return path;
        }

        if(!path.startsWith("/")) {
            return contextPath + "/" + path;
        } else {
            return "/" + contextPath + path;
        }
    }
    
    public static String prependContextPathIfRequired(String url) {
        if(url==null) {
            return null; 
        }
        if(isLocalResource(url)) {
            return prependContextPathIfPresent(url);
        }
        return url;
    }

    // -- RESTFUL PATH RESOURCE

    /**
     * @return restful-path resource (if any) as stored previously by {@link #putRestfulPath(String)}
     */
    public final static String getRestfulPathIfAny() {
        final _Resources_RestfulPath resource = _Context.getIfAny(_Resources_RestfulPath.class);
        return resource!=null ? resource.getRestfulPath() : null;
    }

    /**
     * 
     * @return restful-path resource as stored previously by {@link #putRestfulPath(String)}
     *  or throws if resource not found
     * @throws NullPointerException if resource not found
     */
    public final static String getRestfulPathOrThrow() {
        return ifPresentElseThrow(getRestfulPathIfAny(), 
                ()->new NullPointerException(
                        "Could not find BasePath for the REST Service "
                                + "config value on the context."));
    }

    /**
     * Stores the {@code restfulPath} as an application scoped resource-object.
     * @param restfulPath
     * @throws IllegalArgumentException if the restfulPath is empty or is the root-path.
     */
    public final static void putRestfulPath(String restfulPath) {
        _Context.put(_Resources_RestfulPath.class, new _Resources_RestfulPath(restfulPath), false);
    }

    // -- LOCAL vs EXTERNAL resource path

    private static final Predicate<String> externalResourcePattern = 
            Pattern.compile("^\\w+?://.*$").asPredicate(); 

    /**
     * Returns whether the {@code resourcePath} is intended local and relative 
     * to the web-app's context root. 
     * @param resourcePath
     */
    public static boolean isLocalResource(String resourcePath) {
        requires(resourcePath, "resourcePath");
        return !externalResourcePattern.test(resourcePath);
    }

    /**
     * To build a path from chunks {@code 'a' + 'b' -> 'a/b'}, also handling cases eg.
     * {@code 'a/' + '/b' -> 'a/b'}
     * 
     * @param extendee
     * @param suffix
     * @return
     */
    public static String combinePath(@Nullable String extendee, @Nullable String suffix) {
        return _Strings.combineWithDelimiter(extendee, suffix, "/");
    }

    // -- HELPER

    /*
     *
     * Adapted copy of JDK 8 Class::resolveName
     */
    private static String resolveName(String name, Class<?> contextClass) {
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
