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

@SuppressWarnings("unused")
public class Annotations_synthesize_on_Parameter_Test {

    @Inherited
    @Target({ ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface DomainObj { // cf @DomainObject
        enum Publishng { // cf Publishing enum
            YES,
            NO,
            NOT_SPECIFIED
        }
        Publishng publishng() default Publishng.NOT_SPECIFIED;
    }

    @DomainObj(publishng = DomainObj.Publishng.YES)
    @Inherited
    @Target({ ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface Published {
    }

    @DomainObj(publishng = DomainObj.Publishng.NO)
    @Inherited
    @Target({ ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface NotPublished {
    }

    @Published
    @Inherited
    @Target({ ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface MetaPublished {
    }

    @Test
    public void direct() throws Exception {

        class SomeDomainObject {
            public void updateName(
                    @DomainObj(publishng = DomainObj.Publishng.YES) final
                    String name
                    ) {}
        }

        var method = SomeDomainObject.class.getMethod("updateName", String.class);
        var nearest = _Annotations.synthesize(method.getParameters()[0], DomainObj.class);

        assertThat(nearest.isPresent(), is(true));
        assertThat(nearest.get().publishng(), is(DomainObj.Publishng.YES));

    }

    @Test
    public void meta() throws Exception {

        class SomeDomainObject {
            public void updateName(
                    @Published final
                    String name
                    ) {}
        }

        var method = SomeDomainObject.class.getMethod("updateName", String.class);
        var nearest = _Annotations.synthesize(method.getParameters()[0], DomainObj.class);

        assertThat(nearest.isPresent(), is(true));
        assertThat(nearest.get().publishng(), is(DomainObj.Publishng.YES));
    }

    @Test
    public void metaMeta() throws Exception {

        class SomeDomainObject {
            public void updateName(
                    @MetaPublished final
                    String name
                    ) {}
        }

        var method = SomeDomainObject.class.getMethod("updateName", String.class);
        var nearest = _Annotations.synthesize(method.getParameters()[0], DomainObj.class);

        assertThat(nearest.isPresent(), is(true));
        assertThat(nearest.get().publishng(), is(DomainObj.Publishng.YES));
    }

    @Test
    public void meta_and_metaMeta() throws Exception {

        class SomeDomainObject {
            public void updateName(
                    @MetaPublished
                    @Published final
                    String name
                    ) {}
        }

        var method = SomeDomainObject.class.getMethod("updateName", String.class);
        var nearest = _Annotations.synthesize(method.getParameters()[0], DomainObj.class);

        assertThat(nearest.isPresent(), is(true));
        assertThat(nearest.get().publishng(), is(DomainObj.Publishng.YES));
    }

    @Test
    public void meta_overrides_metaMeta() throws Exception {

        class SomeDomainObject {
            public void updateName(
                    @MetaPublished
                    @NotPublished final
                    String name
                    ) {}
        }

        var method = SomeDomainObject.class.getMethod("updateName", String.class);
        var nearest = _Annotations.synthesize(method.getParameters()[0], DomainObj.class);

        assertThat(nearest.isPresent(), is(true));
        assertThat(nearest.get().publishng(), is(DomainObj.Publishng.NO));
    }

    @Test
    public void direct_overrides_metaMeta() throws Exception {

        class SomeDomainObject {
            public void updateName(
                    @MetaPublished
                    @Published
                    @DomainObj(publishng = DomainObj.Publishng.NO) final
                    String name
                    ) {}
        }

        var method = SomeDomainObject.class.getMethod("updateName", String.class);
        var nearest = _Annotations.synthesize(method.getParameters()[0], DomainObj.class);

        assertThat(nearest.isPresent(), is(true));
        assertThat(nearest.get().publishng(), is(DomainObj.Publishng.NO));
    }

    @Test
    public void direct_overrides_metaMeta_2() throws Exception {

        class SomeDomainObject {
            public void updateName(
                    @MetaPublished
                    @NotPublished
                    @DomainObj(publishng = DomainObj.Publishng.YES) final
                    String name
                    ) {}
        }

        var method = SomeDomainObject.class.getMethod("updateName", String.class);
        var nearest = _Annotations.synthesize(method.getParameters()[0], DomainObj.class);

        assertThat(nearest.isPresent(), is(true));
        assertThat(nearest.get().publishng(), is(DomainObj.Publishng.YES));
    }

}
