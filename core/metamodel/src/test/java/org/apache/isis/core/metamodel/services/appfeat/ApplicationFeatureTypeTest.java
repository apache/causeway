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
package org.apache.isis.core.metamodel.services.appfeat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureSort;

import lombok.val;

public class ApplicationFeatureTypeTest {

    public static class HideClassName extends ApplicationFeatureTypeTest {
        @Test
        public void all() throws Exception {
            assertThat(ApplicationFeatureSort.NAMESPACE.hideClassName(), is(true));
            assertThat(ApplicationFeatureSort.TYPE.hideClassName(), is(false));
            assertThat(ApplicationFeatureSort.MEMBER.hideClassName(), is(false));
        }
    }

    public static class HideMemberName extends ApplicationFeatureTypeTest {

        @Test
        public void all() throws Exception {
            assertThat(ApplicationFeatureSort.NAMESPACE.hideMember(), is(true));
            assertThat(ApplicationFeatureSort.TYPE.hideMember(), is(true));
            assertThat(ApplicationFeatureSort.MEMBER.hideMember(), is(false));
        }
    }

    public static class Init extends ApplicationFeatureTypeTest {

        @Rule
        public ExpectedException expectedException = ExpectedException.none();

        @Test
        public void givenPackage() throws Exception {

            val applicationFeatureId = ApplicationFeatureId.createNamespace("com.mycompany"); 

            assertThat(applicationFeatureId.getNamespace(), is("com.mycompany"));
            assertThat(applicationFeatureId.getTypeSimpleName(), is(nullValue()));
            assertThat(applicationFeatureId.getMemberName(), is(nullValue()));

        }
        @Test
        public void givenClass() throws Exception {

            val applicationFeatureId = ApplicationFeatureId.createType("com.mycompany.Bar");

            assertThat(applicationFeatureId.getNamespace(), is("com.mycompany"));
            assertThat(applicationFeatureId.getTypeSimpleName(), is("Bar"));
            assertThat(applicationFeatureId.getMemberName(), is(nullValue()));

        }
        @Test
        public void givenMember() throws Exception {

            val applicationFeatureId = ApplicationFeatureId.createMember("com.mycompany.Bar#foo");
            
            assertThat(applicationFeatureId.getNamespace(), is("com.mycompany"));
            assertThat(applicationFeatureId.getTypeSimpleName(), is("Bar"));
            assertThat(applicationFeatureId.getMemberName(), is("foo"));
        }
        @Test
        public void givenMemberMalformed() throws Exception {

            expectedException.expect(IllegalArgumentException.class);
            ApplicationFeatureId
                    .createMember("com.mycompany.BarISMISSINGTHEHASHSYMBOL");
        }
    }

    public static class EnsurePackage extends ApplicationFeatureTypeTest {

        @Rule
        public ExpectedException expectedException = ExpectedException.none();

        @Test
        public void whenPackage() throws Exception {
            ApplicationFeatureSort.ensurePackage(new ApplicationFeatureId(ApplicationFeatureSort.NAMESPACE, "xxx"));
        }
        @Test
        public void whenClass() throws Exception {
            expectedException.expect(IllegalStateException.class);
            ApplicationFeatureSort.ensurePackage(new ApplicationFeatureId(ApplicationFeatureSort.TYPE, "xxx"));
        }
        @Test
        public void whenMember() throws Exception {
            expectedException.expect(IllegalStateException.class);
            ApplicationFeatureSort.ensurePackage(new ApplicationFeatureId(ApplicationFeatureSort.MEMBER, "xxx#x"));
        }
    }

    public static class EnsurePackageOrClass extends ApplicationFeatureTypeTest {

        @Rule
        public ExpectedException expectedException = ExpectedException.none();

        @Test
        public void whenPackage() throws Exception {
            ApplicationFeatureSort.ensurePackageOrClass(new ApplicationFeatureId(ApplicationFeatureSort.NAMESPACE, "xxx"));
        }
        @Test
        public void whenClass() throws Exception {
            ApplicationFeatureSort.ensurePackageOrClass(new ApplicationFeatureId(ApplicationFeatureSort.TYPE, "xxx"));
        }
        @Test
        public void whenMember() throws Exception {
            expectedException.expect(IllegalStateException.class);
            ApplicationFeatureSort.ensurePackageOrClass(new ApplicationFeatureId(ApplicationFeatureSort.MEMBER, "xxx#x"));
        }

    }

    public static class EnsureClass extends ApplicationFeatureTypeTest {
        @Rule
        public ExpectedException expectedException = ExpectedException.none();

        @Test
        public void whenPackage() throws Exception {
            expectedException.expect(IllegalStateException.class);
            ApplicationFeatureSort.ensureClass(new ApplicationFeatureId(ApplicationFeatureSort.NAMESPACE, "xxx"));
        }
        @Test
        public void whenClass() throws Exception {
            ApplicationFeatureSort.ensureClass(new ApplicationFeatureId(ApplicationFeatureSort.TYPE, "xxx"));
        }
        @Test
        public void whenMember() throws Exception {
            expectedException.expect(IllegalStateException.class);
            ApplicationFeatureSort.ensureClass(new ApplicationFeatureId(ApplicationFeatureSort.MEMBER, "xxx#x"));
        }

    }

    public static class EnsureMember extends ApplicationFeatureTypeTest {
        @Rule
        public ExpectedException expectedException = ExpectedException.none();

        @Test
        public void whenPackage() throws Exception {
            expectedException.expect(IllegalStateException.class);
            ApplicationFeatureSort.ensureMember(new ApplicationFeatureId(ApplicationFeatureSort.NAMESPACE, "xxx"));
        }
        @Test
        public void whenClass() throws Exception {
            expectedException.expect(IllegalStateException.class);
            ApplicationFeatureSort.ensureMember(new ApplicationFeatureId(ApplicationFeatureSort.TYPE, "xxx"));
        }
        @Test
        public void whenMember() throws Exception {
            ApplicationFeatureSort.ensureMember(new ApplicationFeatureId(ApplicationFeatureSort.MEMBER, "xxx#x"));
        }
    }

    public static class ToString extends ApplicationFeatureTypeTest {

        @Test
        public void happyCase() throws Exception {
            assertThat(ApplicationFeatureSort.NAMESPACE.toString(), is("NAMESPACE"));
        }
    }

}