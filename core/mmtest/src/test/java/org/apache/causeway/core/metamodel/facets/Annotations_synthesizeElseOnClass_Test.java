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
    void on_method() throws Exception {

        class SomeTestCase {
            @InteracAs(usrName = "sven")
            public void action() {}
        }

        var method = SomeTestCase.class.getMethod("action");
        var nearest = _Annotations.synthesizeConsideringClass(method, InteracAs.class);

        assertThat(nearest.isPresent(), is(true));
        assertThat(nearest.get().usrName(), is("sven"));
    }

    @Test
    void meta() throws Exception {

        class SomeTestCase {
            @InteracdAsJoe
            public void action() {}
        }

        var method = SomeTestCase.class.getMethod("action");
        var nearest = _Annotations.synthesizeConsideringClass(method, InteracAs.class);

        assertThat(nearest.isPresent(), is(true));
        assertThat(nearest.get().usrName(), is("joe"));
    }

    @Test
    void metaMeta() throws Exception {

        class SomeTestCase {
            @MetaInteractAsJoe
            public void action() {}
        }

        var method = SomeTestCase.class.getMethod("action");
        var nearest = _Annotations.synthesizeConsideringClass(method, InteracAs.class);

        assertThat(nearest.isPresent(), is(true));
        assertThat(nearest.get().usrName(), is("joe"));
    }

    @Test
    void meta_and_metaMeta() throws Exception {

        class SomeTestCase {
            @MetaInteractAsJoe
            @InteracdAsJoe
            public void action() {}
        }

        var method = SomeTestCase.class.getMethod("action");
        var nearest = _Annotations.synthesizeConsideringClass(method, InteracAs.class);

        assertThat(nearest.isPresent(), is(true));
        assertThat(nearest.get().usrName(), is("joe"));
    }

    @Test
    void meta_overrides_metaMeta() throws Exception {

        class SomeTestCase {
            @MetaInteractAsJoe
            @InteracdAsSven
            public void action() {}
        }

        var method = SomeTestCase.class.getMethod("action");
        var nearest = _Annotations.synthesizeConsideringClass(method, InteracAs.class);

        assertThat(nearest.isPresent(), is(true));
        assertThat(nearest.get().usrName(), is("sven"));
    }

    @Test
    void direct_overrides_metaMeta() throws Exception {

        class SomeTestCase {
            @MetaInteractAsJoe
            @InteracdAsSven
            @InteracAs(usrName = "bill")
            public void action() {}
        }

        var method = SomeTestCase.class.getMethod("action");
        var nearest = _Annotations.synthesizeConsideringClass(method, InteracAs.class);

        assertThat(nearest.isPresent(), is(true));
        assertThat(nearest.get().usrName(), is("bill"));
    }

    @Test
    void from_class() throws Exception {

        @InteracAs(usrName = "bill")
        @SuppressWarnings("unused")
        class SomeTestCase {
            public void action() {}
        }

        var method = SomeTestCase.class.getMethod("action");
        var nearest = _Annotations.synthesizeConsideringClass(method, InteracAs.class);

        assertThat(nearest.isPresent(), is(true));
        assertThat(nearest.get().usrName(), is("bill"));
    }

    @Test
    void from_superclass() throws Exception {

        @InteracAs(usrName = "bill")
        class SomeSuperTestCase {
        }
        @SuppressWarnings("unused")
        class SomeTestCase extends SomeSuperTestCase {
            public void action() {}
        }

        var method = SomeTestCase.class.getMethod("action");
        var nearest = _Annotations.synthesizeConsideringClass(method, InteracAs.class);

        assertThat(nearest.isPresent(), is(true));
        assertThat(nearest.get().usrName(), is("bill"));
    }

    @Test
    void class_overrides_from_superclass() throws Exception {

        @InteracAs(usrName = "bill")
        class SomeSuperTestCase {
        }

        @InteracAs(usrName = "fred")
        @SuppressWarnings("unused")
        class SomeTestCase extends SomeSuperTestCase {
            public void action() {}
        }

        var method = SomeTestCase.class.getMethod("action");
        var nearest = _Annotations.synthesizeConsideringClass(method, InteracAs.class);

        assertThat(nearest.isPresent(), is(true));
        assertThat(nearest.get().usrName(), is("fred"));
    }

    @Test
    void method_overrides_class() throws Exception {

        @InteracAs(usrName = "fred")
        class SomeTestCase  {
            @InteracAs(usrName = "bill")
            public void action() {}
        }

        var method = SomeTestCase.class.getMethod("action");
        var nearest = _Annotations.synthesizeConsideringClass(method, InteracAs.class);

        assertThat(nearest.isPresent(), is(true));
        assertThat(nearest.get().usrName(), is("bill"));
    }

    @Test
    void meta_on_method_overrides_class() throws Exception {

        @InteracAs(usrName = "fred")
        class SomeTestCase  {
            @InteracdAsJoe
            public void action() {}
        }

        var method = SomeTestCase.class.getMethod("action");
        var nearest = _Annotations.synthesizeConsideringClass(method, InteracAs.class);

        assertThat(nearest.isPresent(), is(true));
        assertThat(nearest.get().usrName(), is("joe"));
    }

    @Test
    void local_class_overrides_method_on_superclass() throws Exception {

        class SomeSuperClass  {
            @InteracAs(usrName = "bill")
            public void action() {}
        }

        @InteracdAsSven
        class SomeTestCase extends SomeSuperClass {
            @Override
            public void action() {
                super.action();
            }
        }

        var method = SomeTestCase.class.getMethod("action");
        var nearest = _Annotations.synthesizeConsideringClass(method, InteracAs.class);

        assertThat(nearest.isPresent(), is(true));
        assertThat(nearest.get().usrName(), is("sven"));
    }

    @Test
    void meta_on_local_class_overrides_method_on_superclass() throws Exception {

        class SomeSuperClass  {
            @InteracAs(usrName = "bill")
            public void action() {}
        }

        @MetaInteractAsJoe
        class SomeTestCase extends SomeSuperClass {
            @Override
            public void action() {
                super.action();
            }
        }

        var method = SomeTestCase.class.getMethod("action");
        var nearest = _Annotations.synthesizeConsideringClass(method, InteracAs.class);

        assertThat(nearest.isPresent(), is(true));
        assertThat(nearest.get().usrName(), is("joe"));
    }

}
