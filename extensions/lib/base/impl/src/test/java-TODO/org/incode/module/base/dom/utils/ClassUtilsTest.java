package org.incode.module.base.dom.utils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class ClassUtilsTest {

    public static class Load extends ClassUtilsTest {

        public static class SomeClass {
        }

        public static class SomeSubclass extends SomeClass {
        }

        public static class SomeOtherClass {
        }

        @Rule
        public ExpectedException thrown = ExpectedException.none();

        @Test
        public void doesExist() throws Exception {
            final Class<? extends SomeClass> cls = ClassUtils.load(SomeClass.class.getName(), SomeClass.class);
            assertThat(cls, is(not(nullValue())));
        }

        @Test
        public void doesNotExist() throws Exception {
            thrown.expect(IllegalArgumentException.class);
            thrown.expectMessage("Class 'org.incode.module.base.dom.utils.SomeNonExistentClass' not found");
            final Class<? extends SomeClass> cls = ClassUtils.load("org.incode.module.base.dom.utils.SomeNonExistentClass", SomeClass.class);
            assertThat(cls, is(not(nullValue())));
        }

        @Test
        public void existsAndIsSubclass() throws Exception {
            final Class<? extends SomeClass> cls = ClassUtils.load(SomeSubclass.class.getName(), SomeClass.class);
            assertThat(cls, is(not(nullValue())));
        }

        @Test
        public void existsAndIsNotASubclass() throws Exception {
            thrown.expect(IllegalArgumentException.class);
            thrown.expectMessage("Class 'org.incode.module.base.dom.utils.ClassUtilsTest$Load$SomeOtherClass' not a subclass of org.incode.module.base.dom.utils.ClassUtilsTest$Load$SomeClass");
            final Class<? extends SomeClass> cls = ClassUtils.load(SomeOtherClass.class.getName(), SomeClass.class);
            assertThat(cls, is(not(nullValue())));
        }

    }
}