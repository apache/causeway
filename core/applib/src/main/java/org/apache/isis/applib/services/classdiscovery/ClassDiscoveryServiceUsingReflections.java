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
package org.apache.isis.applib.services.classdiscovery;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import com.google.common.collect.Lists;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.vfs.SystemDir;
import org.reflections.vfs.Vfs;
import org.apache.isis.applib.AbstractService;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;

/**
 * This utility service supports the dynamic discovery of classes from the classpath.  One service that uses this
 * is the <tt>FixtureScripts</tt> domain service.
 *
 * <p>
 * This service has no UI and there is only one implementation (this class) in applib, so it is annotated with
 * {@link org.apache.isis.applib.annotation.DomainService}.  This means that it is automatically registered and
 * available for use; no further configuration is required.
 * </p>
 */
@DomainService(
        nature = NatureOfService.DOMAIN
)
public class ClassDiscoveryServiceUsingReflections
            extends AbstractService 
            implements ClassDiscoveryService2 {


    @Programmatic
    @Override
    public <T> Set<Class<? extends T>> findSubTypesOfClasses(Class<T> type) {
        Vfs.setDefaultURLTypes(getUrlTypes());

        final Reflections reflections = new Reflections(
                ClasspathHelper.forClassLoader(Thread.currentThread().getContextClassLoader()),
                ClasspathHelper.forClass(Object.class),
                new SubTypesScanner(false)
        );
        return reflections.getSubTypesOf(type);
    }

    @Programmatic
    @Override
    public <T> Set<Class<? extends T>> findSubTypesOfClasses(Class<T> type, String packagePrefix) {
        Vfs.setDefaultURLTypes(getUrlTypes());

        final Reflections reflections = new Reflections(
                ClasspathHelper.forClassLoader(Thread.currentThread().getContextClassLoader()),
                ClasspathHelper.forClass(Object.class),
                ClasspathHelper.forPackage(packagePrefix),
                new SubTypesScanner(false)
        );
        return reflections.getSubTypesOf(type);
    }

    // //////////////////////////////////////

    /**
     * Has <tt>public</tt> visibility only so can be reused by other services (including Isis runtime itself).
     */
    public static List<Vfs.UrlType> getUrlTypes() {
        final List<Vfs.UrlType> urlTypes = Lists.newArrayList();
        urlTypes.add(new EmptyIfFileEndingsUrlType(".pom", ".jnilib", "QTJava.zip"));
        urlTypes.add(new JettyConsoleUrlType());
        urlTypes.addAll(Arrays.asList(Vfs.DefaultUrlTypes.values()));

        return urlTypes;
    }


    private static class EmptyIfFileEndingsUrlType implements Vfs.UrlType {

        private final List<String> fileEndings;

        private EmptyIfFileEndingsUrlType(final String... fileEndings) {
            this.fileEndings = Lists.newArrayList(fileEndings);
        }

        public boolean matches(URL url) {
            final String protocol = url.getProtocol();
            final String externalForm = url.toExternalForm();
            if (!protocol.equals("file")) {
                return false;
            }
            for (String fileEnding : fileEndings) {
                if (externalForm.endsWith(fileEnding))
                    return true;
            }
            return false;
        }

        public Vfs.Dir createDir(final URL url) throws Exception {
            return emptyVfsDir(url);
        }

        private static Vfs.Dir emptyVfsDir(final URL url) {
            return new Vfs.Dir() {
                @Override
                public String getPath() {
                    return url.toExternalForm();
                }

                @Override
                public Iterable<Vfs.File> getFiles() {
                    return Collections.emptyList();
                }

                @Override
                public void close() {
                    //
                }
            };
        }
    }


    public static class JettyConsoleUrlType implements Vfs.UrlType {
        public boolean matches(URL url) {
            final String protocol = url.getProtocol();
            final String externalForm = url.toExternalForm();
            final boolean matches = protocol.equals("file") && externalForm.contains("jetty-console") && externalForm.contains("-any-") && externalForm.endsWith("webapp/WEB-INF/classes/");
            return matches;
        }

        public Vfs.Dir createDir(final URL url) throws Exception {
            return new SystemDir(getFile(url));
        }

        /**
         * try to get {@link java.io.File} from url
         *
         * <p>
         *     Copied from {@link org.reflections.vfs.Vfs} (not publicly accessible)
         * </p>
         */
        static java.io.File getFile(URL url) {
            java.io.File file;
            String path;

            try {
                path = url.toURI().getSchemeSpecificPart();
                if ((file = new java.io.File(path)).exists()) return file;
            } catch (URISyntaxException e) {
            }

            try {
                path = URLDecoder.decode(url.getPath(), "UTF-8");
                if (path.contains(".jar!")) path = path.substring(0, path.lastIndexOf(".jar!") + ".jar".length());
                if ((file = new java.io.File(path)).exists()) return file;

            } catch (UnsupportedEncodingException e) {
            }

            try {
                path = url.toExternalForm();
                if (path.startsWith("jar:")) path = path.substring("jar:".length());
                if (path.startsWith("file:")) path = path.substring("file:".length());
                if (path.contains(".jar!")) path = path.substring(0, path.indexOf(".jar!") + ".jar".length());
                if ((file = new java.io.File(path)).exists()) return file;

                path = path.replace("%20", " ");
                if ((file = new java.io.File(path)).exists()) return file;

            } catch (Exception e) {
            }

            return null;
        }
    }

}