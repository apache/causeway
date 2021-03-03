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
import org.apache.isis.applib.services.appfeat.ApplicationFeatureType;

import lombok.val;

public class ApplicationFeatureTypeTest {

    public static class HideClassName extends ApplicationFeatureTypeTest {
        @Test
        public void all() throws Exception {
            assertThat(ApplicationFeatureType.PACKAGE.hideClassName(), is(true));
            assertThat(ApplicationFeatureType.CLASS.hideClassName(), is(false));
            assertThat(ApplicationFeatureType.MEMBER.hideClassName(), is(false));
        }
    }

    public static class HideMemberName extends ApplicationFeatureTypeTest {

        @Test
        public void all() throws Exception {
            assertThat(ApplicationFeatureType.PACKAGE.hideMember(), is(true));
            assertThat(ApplicationFeatureType.CLASS.hideMember(), is(true));
            assertThat(ApplicationFeatureType.MEMBER.hideMember(), is(false));
        }
    }

    public static class Init extends ApplicationFeatureTypeTest {

        @Rule
        public ExpectedException expectedException = ExpectedException.none();

        @Test
        public void givenPackage() throws Exception {

            val applicationFeatureId = ApplicationFeatureId.createPackage("com.mycompany"); 

            assertThat(applicationFeatureId.getNamespace(), is("com.mycompany"));
            assertThat(applicationFeatureId.getTypeSimpleName(), is(nullValue()));
            assertThat(applicationFeatureId.getMemberName(), is(nullValue()));

        }
        @Test
        public void givenClass() throws Exception {

            val applicationFeatureId = ApplicationFeatureId.createClass("com.mycompany.Bar");

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
            ApplicationFeatureType.ensurePackage(new ApplicationFeatureId(ApplicationFeatureType.PACKAGE, "xxx"));
        }
        @Test
        public void whenClass() throws Exception {
            expectedException.expect(IllegalStateException.class);
            ApplicationFeatureType.ensurePackage(new ApplicationFeatureId(ApplicationFeatureType.CLASS, "xxx"));
        }
        @Test
        public void whenMember() throws Exception {
            expectedException.expect(IllegalStateException.class);
            ApplicationFeatureType.ensurePackage(new ApplicationFeatureId(ApplicationFeatureType.MEMBER, "xxx#x"));
        }
    }

    public static class EnsurePackageOrClass extends ApplicationFeatureTypeTest {

        @Rule
        public ExpectedException expectedException = ExpectedException.none();

        @Test
        public void whenPackage() throws Exception {
            ApplicationFeatureType.ensurePackageOrClass(new ApplicationFeatureId(ApplicationFeatureType.PACKAGE, "xxx"));
        }
        @Test
        public void whenClass() throws Exception {
            ApplicationFeatureType.ensurePackageOrClass(new ApplicationFeatureId(ApplicationFeatureType.CLASS, "xxx"));
        }
        @Test
        public void whenMember() throws Exception {
            expectedException.expect(IllegalStateException.class);
            ApplicationFeatureType.ensurePackageOrClass(new ApplicationFeatureId(ApplicationFeatureType.MEMBER, "xxx#x"));
        }

    }

    public static class EnsureClass extends ApplicationFeatureTypeTest {
        @Rule
        public ExpectedException expectedException = ExpectedException.none();

        @Test
        public void whenPackage() throws Exception {
            expectedException.expect(IllegalStateException.class);
            ApplicationFeatureType.ensureClass(new ApplicationFeatureId(ApplicationFeatureType.PACKAGE, "xxx"));
        }
        @Test
        public void whenClass() throws Exception {
            ApplicationFeatureType.ensureClass(new ApplicationFeatureId(ApplicationFeatureType.CLASS, "xxx"));
        }
        @Test
        public void whenMember() throws Exception {
            expectedException.expect(IllegalStateException.class);
            ApplicationFeatureType.ensureClass(new ApplicationFeatureId(ApplicationFeatureType.MEMBER, "xxx#x"));
        }

    }

    public static class EnsureMember extends ApplicationFeatureTypeTest {
        @Rule
        public ExpectedException expectedException = ExpectedException.none();

        @Test
        public void whenPackage() throws Exception {
            expectedException.expect(IllegalStateException.class);
            ApplicationFeatureType.ensureMember(new ApplicationFeatureId(ApplicationFeatureType.PACKAGE, "xxx"));
        }
        @Test
        public void whenClass() throws Exception {
            expectedException.expect(IllegalStateException.class);
            ApplicationFeatureType.ensureMember(new ApplicationFeatureId(ApplicationFeatureType.CLASS, "xxx"));
        }
        @Test
        public void whenMember() throws Exception {
            ApplicationFeatureType.ensureMember(new ApplicationFeatureId(ApplicationFeatureType.MEMBER, "xxx#x"));
        }
    }

    public static class ToString extends ApplicationFeatureTypeTest {

        @Test
        public void happyCase() throws Exception {
            assertThat(ApplicationFeatureType.PACKAGE.toString(), is("PACKAGE"));
        }
    }

}