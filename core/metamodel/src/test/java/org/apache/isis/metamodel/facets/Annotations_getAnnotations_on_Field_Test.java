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
package org.apache.isis.metamodel.facets;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.Publishing;

import static org.hamcrest.CoreMatchers.is;

@SuppressWarnings("unused")
public class Annotations_getAnnotations_on_Field_Test {


    @Property(publishing = Publishing.ENABLED)
    @Inherited
    @Target({ ElementType.FIELD, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface Published {
    }

    @Property(publishing = Publishing.DISABLED)
    @Inherited
    @Target({ ElementType.FIELD, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface NotPublished {
    }

    @Published
    @Inherited
    @Target({ ElementType.FIELD, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface MetaPublished {
    }

    @Test
    public void direct() throws Exception {

        class SomeDomainObject {
            @Property(publishing = Publishing.ENABLED)
            private String name;

            public String getName() {
                return name;
            }
        }

        Method method = SomeDomainObject.class.getMethod("getName");
        final List<Property> annotations = Annotations.getAnnotations(method, Property.class);

        Assert.assertThat(annotations.size(), is(1));

        Assert.assertThat(annotations.get(0).publishing(), is(Publishing.ENABLED));
    }

    @Test
    public void meta() throws Exception {

        class SomeDomainObject {
            @Published
            private String name;
            public String getName() {
                return name;
            }
        }

        Method method = SomeDomainObject.class.getMethod("getName");
        final List<Property> annotations = Annotations.getAnnotations(method, Property.class);

        Assert.assertThat(annotations.size(), is(1));

        Assert.assertThat(annotations.get(0).publishing(), is(Publishing.ENABLED));
    }

    @Test
    public void metaMeta() throws Exception {

        class SomeDomainObject {
            @MetaPublished
            private String name;
            public String getName() {
                return name;
            }
        }

        Method method = SomeDomainObject.class.getMethod("getName");
        final List<Property> annotations = Annotations.getAnnotations(method, Property.class);

        Assert.assertThat(annotations.size(), is(1));

        Assert.assertThat(annotations.get(0).publishing(), is(Publishing.ENABLED));
    }

    @Test
    public void meta_and_metaMeta() throws Exception {

        class SomeDomainObject {
            @MetaPublished
            @Published
            private String name;
            public String getName() {
                return name;
            }
        }

        Method method = SomeDomainObject.class.getMethod("getName");
        final List<Property> annotations = Annotations.getAnnotations(method, Property.class);

        Assert.assertThat(annotations.size(), is(2));

        Assert.assertThat(annotations.get(0).publishing(), is(Publishing.ENABLED));
        Assert.assertThat(annotations.get(1).publishing(), is(Publishing.ENABLED));
    }

    @Test
    public void meta_overrides_metaMeta() throws Exception {

        class SomeDomainObject {
            @MetaPublished
            @NotPublished
            private String name;
            public String getName() {
                return name;
            }
        }

        Method method = SomeDomainObject.class.getMethod("getName");
        final List<Property> annotations = Annotations.getAnnotations(method, Property.class);

        Assert.assertThat(annotations.size(), is(2));

        Assert.assertThat(annotations.get(0).publishing(), is(Publishing.DISABLED));
        Assert.assertThat(annotations.get(1).publishing(), is(Publishing.ENABLED));
    }

    @Test
    public void direct_overrides_metaMeta() throws Exception {

        class SomeDomainObject {
            @MetaPublished
            @Published
            @Property(publishing = Publishing.DISABLED)
            private String name;
            public String getName() {
                return name;
            }
        }

        Method method = SomeDomainObject.class.getMethod("getName");
        final List<Property> annotations = Annotations.getAnnotations(method, Property.class);

        Assert.assertThat(annotations.size(), is(3));

        Assert.assertThat(annotations.get(0).publishing(), is(Publishing.DISABLED));
        Assert.assertThat(annotations.get(1).publishing(), is(Publishing.ENABLED));
        Assert.assertThat(annotations.get(2).publishing(), is(Publishing.ENABLED));
    }


    @Test
    public void direct_overrides_metaMeta_2() throws Exception {

        class SomeDomainObject {
            @MetaPublished
            @NotPublished
            @Property(publishing = Publishing.ENABLED)
            private String name;
            public String getName() {
                return name;
            }
        }

        Method method = SomeDomainObject.class.getMethod("getName");
        final List<Property> annotations = Annotations.getAnnotations(method, Property.class);

        Assert.assertThat(annotations.size(), is(3));

        Assert.assertThat(annotations.get(0).publishing(), is(Publishing.ENABLED));
        Assert.assertThat(annotations.get(1).publishing(), is(Publishing.DISABLED));
        Assert.assertThat(annotations.get(2).publishing(), is(Publishing.ENABLED));
    }

}