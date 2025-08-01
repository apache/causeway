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

import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.commons.internal.reflection._Annotations;

@SuppressWarnings("unused")
public class Annotations_synthesize_on_Field_Test {

    @Property(executionPublishing = Publishing.ENABLED)
    @Inherited
    @Target({ ElementType.FIELD, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface Published {
    }

    @Property(executionPublishing = Publishing.DISABLED)
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
            @Property(executionPublishing = Publishing.ENABLED)
            private String name;

            public String getName() {
                return name;
            }
        }

        var field = SomeDomainObject.class.getDeclaredField("name");
        var nearestF = _Annotations.synthesize(field, Property.class);

        assertThat(nearestF.isPresent(), is(true));
        assertThat(nearestF.get().executionPublishing(), is(Publishing.ENABLED));

        var method = SomeDomainObject.class.getMethod("getName");
        var nearestM = _Annotations.synthesize(method, Property.class);

        assertThat(nearestM.isPresent(), is(true));
        assertThat(nearestM.get().executionPublishing(), is(Publishing.ENABLED));
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

        var method = SomeDomainObject.class.getMethod("getName");
        var nearestM = _Annotations.synthesize(method, Property.class);

        assertThat(nearestM.isPresent(), is(true));
        assertThat(nearestM.get().executionPublishing(), is(Publishing.ENABLED));
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

        var method = SomeDomainObject.class.getMethod("getName");
        var nearestM = _Annotations.synthesize(method, Property.class);

        assertThat(nearestM.isPresent(), is(true));
        assertThat(nearestM.get().executionPublishing(), is(Publishing.ENABLED));
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

        var method = SomeDomainObject.class.getMethod("getName");
        var nearestM = _Annotations.synthesize(method, Property.class);

        assertThat(nearestM.isPresent(), is(true));
        assertThat(nearestM.get().executionPublishing(), is(Publishing.ENABLED));
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

        var method = SomeDomainObject.class.getMethod("getName");
        var nearestM = _Annotations.synthesize(method, Property.class);

        assertThat(nearestM.isPresent(), is(true));
        assertThat(nearestM.get().executionPublishing(), is(Publishing.DISABLED));
    }

    @Test
    public void direct_overrides_metaMeta() throws Exception {

        class SomeDomainObject {
            @MetaPublished
            @Published
            @Property(executionPublishing = Publishing.DISABLED)
            private String name;
            public String getName() {
                return name;
            }
        }

        var method = SomeDomainObject.class.getMethod("getName");
        var nearestM = _Annotations.synthesize(method, Property.class);

        assertThat(nearestM.isPresent(), is(true));
        assertThat(nearestM.get().executionPublishing(), is(Publishing.DISABLED));
    }

    @Test
    public void direct_overrides_metaMeta_2() throws Exception {

        class SomeDomainObject {
            @MetaPublished
            @NotPublished
            @Property(executionPublishing = Publishing.ENABLED)
            private String name;
            public String getName() {
                return name;
            }
        }

        var method = SomeDomainObject.class.getMethod("getName");
        var nearestM = _Annotations.synthesize(method, Property.class);

        assertThat(nearestM.isPresent(), is(true));
        assertThat(nearestM.get().executionPublishing(), is(Publishing.ENABLED));
    }

}
