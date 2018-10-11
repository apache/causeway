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
package org.apache.isis.core.metamodel.services;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import org.apache.isis.applib.annotation.DomainService;

public class ServiceUtil_Test {

    @DomainService(objectType = "foo.SomeServiceAnnotated")
    public static class SomeServiceAnnotated {}


    @DomainService()
    public static class SomeServiceWithId {

        public String getId() {
            return "bar.SomeServiceWithId";
        }
    }


    @DomainService(objectType = "bop.SomeServiceAnnotated")
    public static class SomeServiceAnnotatedAndWithId {

        public String getId() {
            return "bop.SomeServiceWithId";
        }
    }

    @DomainService()
    public static class SomeServiceWithoutAnnotationOrId {

    }

    @Test
    public void annotated() throws Exception {

        assertThat(
                ServiceUtil.idOfPojo(new SomeServiceAnnotated()),
                is(equalTo("foo.SomeServiceAnnotated")));

        assertThat(
                ServiceUtil.getExplicitIdOfType(SomeServiceAnnotated.class).orElse(null),
                is(equalTo("foo.SomeServiceAnnotated")));
    }

    @Test
    public void id() throws Exception {

        assertThat(
                ServiceUtil.idOfPojo(new SomeServiceWithId()),
                is(equalTo("bar.SomeServiceWithId")));

        assertThat(
                ServiceUtil.getExplicitIdOfType(SomeServiceWithId.class).orElse(null),
                is(equalTo("bar.SomeServiceWithId")));
    }

    @Test
    public void annotated_precedence_over_id() throws Exception {

        assertThat(
                ServiceUtil.idOfPojo(new SomeServiceAnnotatedAndWithId()),
                is(equalTo("bop.SomeServiceAnnotated")));

        assertThat(
                ServiceUtil.getExplicitIdOfType(SomeServiceAnnotatedAndWithId.class).orElse(null),
                is(equalTo("bop.SomeServiceAnnotated")));
    }

    @Test
    public void fallback_to_fqcn_for_obj_but_to_null_for_service() throws Exception {
        assertThat(
                ServiceUtil.idOfPojo(new SomeServiceWithoutAnnotationOrId()),
                is(equalTo("org.apache.isis.core.metamodel.services.ServiceUtil_Test$SomeServiceWithoutAnnotationOrId")));
        assertThat(
                ServiceUtil.getExplicitIdOfType(SomeServiceWithoutAnnotationOrId.class).orElse(null),
                is(nullValue()));
    }

}