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
package org.apache.causeway.core.metamodel.facets;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.causeway.commons.internal.reflection._Annotations;

import lombok.val;

public class Annotations_synthesizeElseOnClass_Test {


    @Inherited
    @Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface InteracAs { // cf @InteractAs
        String usrName() default "";  // cf @InteractAs#userName
    }

    @InteracAs(usrName = "sven")
    @Inherited
    @Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface InteracdAsSven {
    }

    @InteracAs(usrName = "joe")
    @Inherited
    @Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface InteracdAsJoe {
    }

    @InteracdAsJoe
    @Inherited
    @Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface MetaInteractAsJoe {
    }


    @Test
    public void on_method() throws Exception {

        class SomeTestCase {
            @InteracAs(usrName = "sven")
            public void test() {}
        }

        val method = SomeTestCase.class.getMethod("test");
        val nearest = _Annotations.synthesizeConsideringClass(method, InteracAs.class);

        assertThat(nearest.isPresent(), is(true));
        assertThat(nearest.get().usrName(), is("sven"));
    }

    @Test
    public void meta() throws Exception {

        class SomeTestCase {
            @InteracdAsJoe
            public void test() {}
        }

        val method = SomeTestCase.class.getMethod("test");
        val nearest = _Annotations.synthesizeConsideringClass(method, InteracAs.class);

        assertThat(nearest.isPresent(), is(true));
        assertThat(nearest.get().usrName(), is("joe"));
    }

    @Test
    public void metaMeta() throws Exception {

        class SomeTestCase {
            @MetaInteractAsJoe
            public void test() {}
        }

        val method = SomeTestCase.class.getMethod("test");
        val nearest = _Annotations.synthesizeConsideringClass(method, InteracAs.class);

        assertThat(nearest.isPresent(), is(true));
        assertThat(nearest.get().usrName(), is("joe"));
    }

    @Test
    public void meta_and_metaMeta() throws Exception {

        class SomeTestCase {
            @MetaInteractAsJoe
            @InteracdAsJoe
            public void test() {}
        }

        val method = SomeTestCase.class.getMethod("test");
        val nearest = _Annotations.synthesizeConsideringClass(method, InteracAs.class);

        assertThat(nearest.isPresent(), is(true));
        assertThat(nearest.get().usrName(), is("joe"));
    }

    @Test
    public void meta_overrides_metaMeta() throws Exception {

        class SomeTestCase {
            @MetaInteractAsJoe
            @InteracdAsSven
            public void test() {}
        }

        val method = SomeTestCase.class.getMethod("test");
        val nearest = _Annotations.synthesizeConsideringClass(method, InteracAs.class);

        assertThat(nearest.isPresent(), is(true));
        assertThat(nearest.get().usrName(), is("sven"));
    }

    @Test
    public void direct_overrides_metaMeta() throws Exception {

        class SomeTestCase {
            @MetaInteractAsJoe
            @InteracdAsSven
            @InteracAs(usrName = "bill")
            public void test() {}
        }

        val method = SomeTestCase.class.getMethod("test");
        val nearest = _Annotations.synthesizeConsideringClass(method, InteracAs.class);

        assertThat(nearest.isPresent(), is(true));
        assertThat(nearest.get().usrName(), is("bill"));
    }


    @Test
    public void from_class() throws Exception {

        @InteracAs(usrName = "bill")
        @SuppressWarnings("unused")
        class SomeTestCase {
            public void test() {}
        }

        val method = SomeTestCase.class.getMethod("test");
        val nearest = _Annotations.synthesizeConsideringClass(method, InteracAs.class);

        assertThat(nearest.isPresent(), is(true));
        assertThat(nearest.get().usrName(), is("bill"));
    }

    @Test
    public void from_superclass() throws Exception {

        @InteracAs(usrName = "bill")
        class SomeSuperTestCase {
        }
        @SuppressWarnings("unused")
        class SomeTestCase extends SomeSuperTestCase {
            public void test() {}
        }

        val method = SomeTestCase.class.getMethod("test");
        val nearest = _Annotations.synthesizeConsideringClass(method, InteracAs.class);

        assertThat(nearest.isPresent(), is(true));
        assertThat(nearest.get().usrName(), is("bill"));
    }

    @Test
    public void class_overrides_from_superclass() throws Exception {

        @InteracAs(usrName = "bill")
        class SomeSuperTestCase {
        }

        @InteracAs(usrName = "fred")
        @SuppressWarnings("unused")
        class SomeTestCase extends SomeSuperTestCase {
            public void test() {}
        }

        val method = SomeTestCase.class.getMethod("test");
        val nearest = _Annotations.synthesizeConsideringClass(method, InteracAs.class);

        assertThat(nearest.isPresent(), is(true));
        assertThat(nearest.get().usrName(), is("fred"));
    }

    @Test
    public void method_overrides_class() throws Exception {

        @InteracAs(usrName = "fred")
        class SomeTestCase  {
            @InteracAs(usrName = "bill")
            public void test() {}
        }

        val method = SomeTestCase.class.getMethod("test");
        val nearest = _Annotations.synthesizeConsideringClass(method, InteracAs.class);

        assertThat(nearest.isPresent(), is(true));
        assertThat(nearest.get().usrName(), is("bill"));
    }

    @Test
    public void meta_on_method_overrides_class() throws Exception {

        @InteracAs(usrName = "fred")
        class SomeTestCase  {
            @InteracdAsJoe
            public void test() {}
        }

        val method = SomeTestCase.class.getMethod("test");
        val nearest = _Annotations.synthesizeConsideringClass(method, InteracAs.class);

        assertThat(nearest.isPresent(), is(true));
        assertThat(nearest.get().usrName(), is("joe"));
    }

    @Test
    public void local_class_overrides_method_on_superclass() throws Exception {

        class SomeSuperClass  {
            @InteracAs(usrName = "bill")
            public void test() {}
        }

        @InteracdAsSven
        class SomeTestCase extends SomeSuperClass {
            @Override
            public void test() {
                super.test();
            }
        }

        val method = SomeTestCase.class.getMethod("test");
        val nearest = _Annotations.synthesizeConsideringClass(method, InteracAs.class);

        assertThat(nearest.isPresent(), is(true));
        assertThat(nearest.get().usrName(), is("sven"));
    }

    @Test
    public void meta_on_local_class_overrides_method_on_superclass() throws Exception {

        class SomeSuperClass  {
            @InteracAs(usrName = "bill")
            public void test() {}
        }

        @MetaInteractAsJoe
        class SomeTestCase extends SomeSuperClass {
            @Override
            public void test() {
                super.test();
            }
        }

        val method = SomeTestCase.class.getMethod("test");
        val nearest = _Annotations.synthesizeConsideringClass(method, InteracAs.class);

        assertThat(nearest.isPresent(), is(true));
        assertThat(nearest.get().usrName(), is("joe"));
    }

}
