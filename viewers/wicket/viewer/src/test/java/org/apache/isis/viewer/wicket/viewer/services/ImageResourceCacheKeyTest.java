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
package org.apache.isis.viewer.wicket.viewer.services;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;

public class ImageResourceCacheKeyTest {

    ImageResourceCacheClassPath.ImageResourceCacheKey key;

    public static class SomeSuperclass {
    }

    public static class SomeSubclass extends SomeSuperclass {
    }

    public static class SomeOtherContextClass extends SomeSuperclass {
    }


    public static class SuperKey extends ImageResourceCacheKeyTest {

        @Test
        public void whenSubclass() throws Exception {

            key = new ImageResourceCacheClassPath.ImageResourceCacheKey(SomeSubclass.class, "foo");
            final ImageResourceCacheClassPath.ImageResourceCacheKey superKey = key.superKey();

            assertEquals(SomeSuperclass.class, superKey.getResourceClass());
            assertEquals("foo", key.getResourceName());
        }

        @Test
        public void whenSuperClass() throws Exception {

            key = new ImageResourceCacheClassPath.ImageResourceCacheKey(SomeSuperclass.class, null);
            final ImageResourceCacheClassPath.ImageResourceCacheKey superKey = key.superKey();

            assertNull(superKey);
        }

    }

    public static class ResourcePaths extends ImageResourceCacheKeyTest {

        @Test
        public void withIconName() throws Exception {
            key = new ImageResourceCacheClassPath.ImageResourceCacheKey(SomeSubclass.class, "foo");

            final List<ImageResourceCacheClassPath.ImageResourceCacheKey> keys = key.resourcePaths("png", "jpg", "jpeg");

            final Iterator<ImageResourceCacheClassPath.ImageResourceCacheKey> iterator = keys.iterator();

            assertNext(iterator, SomeSubclass.class, "SomeSubclass-foo.png");
            assertNext(iterator, SomeSubclass.class, "SomeSubclass-foo.jpg");
            assertNext(iterator, SomeSubclass.class, "SomeSubclass-foo.jpeg");

            assertNext(iterator, SomeSubclass.class, "foo.png");
            assertNext(iterator, SomeSubclass.class, "foo.jpg");
            assertNext(iterator, SomeSubclass.class, "foo.jpeg");

            assertNext(iterator, SomeSuperclass.class, "SomeSuperclass-foo.png");
            assertNext(iterator, SomeSuperclass.class, "SomeSuperclass-foo.jpg");
            assertNext(iterator, SomeSuperclass.class, "SomeSuperclass-foo.jpeg");

            assertNext(iterator, SomeSuperclass.class, "foo.png");
            assertNext(iterator, SomeSuperclass.class, "foo.jpg");
            assertNext(iterator, SomeSuperclass.class, "foo.jpeg");

            assertNext(iterator, null, "SomeSubclass-foo.png");
            assertNext(iterator, null, "SomeSubclass-foo.jpg");
            assertNext(iterator, null, "SomeSubclass-foo.jpeg");

            assertNext(iterator, null, "SomeSuperclass-foo.png");
            assertNext(iterator, null, "SomeSuperclass-foo.jpg");
            assertNext(iterator, null, "SomeSuperclass-foo.jpeg");

            assertNext(iterator, null, "foo.png");
            assertNext(iterator, null, "foo.jpg");
            assertNext(iterator, null, "foo.jpeg");

            assertNext(iterator, SomeSubclass.class, "SomeSubclass.png");
            assertNext(iterator, SomeSubclass.class, "SomeSubclass.jpg");
            assertNext(iterator, SomeSubclass.class, "SomeSubclass.jpeg");

            assertNext(iterator, SomeSuperclass.class, "SomeSuperclass.png");
            assertNext(iterator, SomeSuperclass.class, "SomeSuperclass.jpg");
            assertNext(iterator, SomeSuperclass.class, "SomeSuperclass.jpeg");

            assertNext(iterator, null, "SomeSubclass.png");
            assertNext(iterator, null, "SomeSubclass.jpg");
            assertNext(iterator, null, "SomeSubclass.jpeg");

            assertNext(iterator, null, "SomeSuperclass.png");
            assertNext(iterator, null, "SomeSuperclass.jpg");
            assertNext(iterator, null, "SomeSuperclass.jpeg");

            assertNext(iterator, null, "Default.png");
            assertNext(iterator, null, "Default.jpg");
            assertNext(iterator, null, "Default.jpeg");

        }

        @Test
        public void withoutIconName() throws Exception {
            key = new ImageResourceCacheClassPath.ImageResourceCacheKey(SomeSubclass.class, null);

            final List<ImageResourceCacheClassPath.ImageResourceCacheKey> keys = key.resourcePaths("png", "jpg", "jpeg");

            final Iterator<ImageResourceCacheClassPath.ImageResourceCacheKey> iterator = keys.iterator();

            assertNext(iterator, SomeSubclass.class, "SomeSubclass.png");
            assertNext(iterator, SomeSubclass.class, "SomeSubclass.jpg");
            assertNext(iterator, SomeSubclass.class, "SomeSubclass.jpeg");

            assertNext(iterator, SomeSuperclass.class, "SomeSuperclass.png");
            assertNext(iterator, SomeSuperclass.class, "SomeSuperclass.jpg");
            assertNext(iterator, SomeSuperclass.class, "SomeSuperclass.jpeg");

            assertNext(iterator, null, "SomeSubclass.png");
            assertNext(iterator, null, "SomeSubclass.jpg");
            assertNext(iterator, null, "SomeSubclass.jpeg");

            assertNext(iterator, null, "SomeSuperclass.png");
            assertNext(iterator, null, "SomeSuperclass.jpg");
            assertNext(iterator, null, "SomeSuperclass.jpeg");

            assertNext(iterator, null, "Default.png");
            assertNext(iterator, null, "Default.jpg");
            assertNext(iterator, null, "Default.jpeg");

        }

        @Test
        public void withoutClass() throws Exception {
            key = new ImageResourceCacheClassPath.ImageResourceCacheKey(null, "foo");

            final List<ImageResourceCacheClassPath.ImageResourceCacheKey> keys = key.resourcePaths("png", "jpg", "jpeg");

            final Iterator<ImageResourceCacheClassPath.ImageResourceCacheKey> iterator = keys.iterator();

            assertNext(iterator, null, "foo.png");
            assertNext(iterator, null, "foo.jpg");
            assertNext(iterator, null, "foo.jpeg");

            assertNext(iterator, null, "Default.png");
            assertNext(iterator, null, "Default.jpg");
            assertNext(iterator, null, "Default.jpeg");
        }

        /**
         * This what we see for repositories (using <code></code>@DomainService(repositoryFor=...))
         */
        @Test
        public void withoutClassButIconNameSpecifyingADifferentContextClassAndClassName() throws Exception {
            key = new ImageResourceCacheClassPath.ImageResourceCacheKey(SomeOtherContextClass.class, SomeSuperclass.class.getName());

            final List<ImageResourceCacheClassPath.ImageResourceCacheKey> keys = key.resourcePaths("png", "jpg", "jpeg");

            final Iterator<ImageResourceCacheClassPath.ImageResourceCacheKey> iterator = keys.iterator();

            assertNext(iterator, SomeSuperclass.class, "SomeSuperclass.png");
            assertNext(iterator, SomeSuperclass.class, "SomeSuperclass.jpg");
            assertNext(iterator, SomeSuperclass.class, "SomeSuperclass.jpeg");

            assertNext(iterator, null, "Default.png");
            assertNext(iterator, null, "Default.jpg");
            assertNext(iterator, null, "Default.jpeg");

        }

        @Test
        public void withoutClassButIconNameSpecifyingAClassName() throws Exception {
            key = new ImageResourceCacheClassPath.ImageResourceCacheKey(null, SomeSuperclass.class.getName());

            final List<ImageResourceCacheClassPath.ImageResourceCacheKey> keys = key.resourcePaths("png", "jpg", "jpeg");

            final Iterator<ImageResourceCacheClassPath.ImageResourceCacheKey> iterator = keys.iterator();

            assertNext(iterator, SomeSuperclass.class, "SomeSuperclass.png");
            assertNext(iterator, SomeSuperclass.class, "SomeSuperclass.jpg");
            assertNext(iterator, SomeSuperclass.class, "SomeSuperclass.jpeg");

            assertNext(iterator, null, "Default.png");
            assertNext(iterator, null, "Default.jpg");
            assertNext(iterator, null, "Default.jpeg");

        }

        private static void assertNext(Iterator<ImageResourceCacheClassPath.ImageResourceCacheKey> iterator, Class<?> resourceClass, String resourceName) {
            assertThat(iterator.hasNext(), is(true));
            final ImageResourceCacheClassPath.ImageResourceCacheKey next = iterator.next();
            assertEquals(resourceClass, next.getResourceClass());
            assertEquals(resourceName, next.getResourceName());
        }
    }

}