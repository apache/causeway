package org.apache.isis.viewer.wicket.viewer.services;

import java.io.File;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

public class TranslationsResolverWicketTest {

    public static class NewFile extends TranslationsResolverWicketTest {

        @Before
        public void setUp() throws Exception {
            assumeThat(System.getProperty("os.name").startsWith("Windows"), is(true));
        }

        @Test
        public void simple() throws Exception {
            final File file = TranslationsResolverWicket.newFile("c:/foo", "bar");
            final String absolutePath = file.getAbsolutePath();
            assertThat(absolutePath, is("c:\\foo\\bar"));
        }

        @Test
        public void nestedChild() throws Exception {
            final File file = TranslationsResolverWicket.newFile("c:/foo", "bar/baz");
            final String absolutePath = file.getAbsolutePath();
            assertThat(absolutePath, is("c:\\foo\\bar\\baz"));
        }

    }


}