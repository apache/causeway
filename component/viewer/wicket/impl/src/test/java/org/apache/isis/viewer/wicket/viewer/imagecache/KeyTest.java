package org.apache.isis.viewer.wicket.viewer.imagecache;

import java.util.Iterator;
import java.util.List;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class KeyTest {

    ImageResourceCacheClassPath.Key key;

    public static class SomeSuperclass {

    }

    public static class SomeSubclass extends SomeSuperclass {

    }


    public static class SuperKey extends KeyTest {

        @Test
        public void whenSubclass() throws Exception {

            key = new ImageResourceCacheClassPath.Key(SomeSubclass.class, "foo");
            final ImageResourceCacheClassPath.Key superKey = key.superKey();

            assertEquals(SomeSuperclass.class, superKey.getResourceClass());
            assertEquals("foo", key.getResourceName());
        }

        @Test
        public void whenSuperClass() throws Exception {

            key = new ImageResourceCacheClassPath.Key(SomeSuperclass.class, null);
            final ImageResourceCacheClassPath.Key superKey = key.superKey();

            assertNull(superKey);
        }

    }

    public static class ResourcePaths extends KeyTest {

        @Test
        public void withIconName() throws Exception {
            key = new ImageResourceCacheClassPath.Key(SomeSubclass.class, "foo");

            final List<ImageResourceCacheClassPath.Key> keys = key.resourcePaths("png", "jpg", "jpeg");

            final Iterator<ImageResourceCacheClassPath.Key> iterator = keys.iterator();

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
            key = new ImageResourceCacheClassPath.Key(SomeSubclass.class, null);

            final List<ImageResourceCacheClassPath.Key> keys = key.resourcePaths("png", "jpg", "jpeg");

            final Iterator<ImageResourceCacheClassPath.Key> iterator = keys.iterator();

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
            key = new ImageResourceCacheClassPath.Key(null, "foo");

            final List<ImageResourceCacheClassPath.Key> keys = key.resourcePaths("png", "jpg", "jpeg");

            final Iterator<ImageResourceCacheClassPath.Key> iterator = keys.iterator();

            assertNext(iterator, null, "foo.png");
            assertNext(iterator, null, "foo.jpg");
            assertNext(iterator, null, "foo.jpeg");

            assertNext(iterator, null, "Default.png");
            assertNext(iterator, null, "Default.jpg");
            assertNext(iterator, null, "Default.jpeg");

        }

        private static void assertNext(Iterator<ImageResourceCacheClassPath.Key> iterator, Class<?> resourceClass, String resourceName) {
            assertThat(iterator.hasNext(), is(true));
            final ImageResourceCacheClassPath.Key next = iterator.next();
            assertEquals(resourceClass, next.getResourceClass());
            assertEquals(resourceName, next.getResourceName());
        }
    }

}