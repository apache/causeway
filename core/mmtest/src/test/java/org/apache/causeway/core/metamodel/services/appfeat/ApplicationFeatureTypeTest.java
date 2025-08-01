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
package org.apache.causeway.core.metamodel.services.appfeat;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.causeway.applib.services.appfeat.ApplicationFeatureId;
import org.apache.causeway.applib.services.appfeat.ApplicationFeatureSort;

class ApplicationFeatureTypeTest {

    public static class HideClassName extends ApplicationFeatureTypeTest {
        @Test
        public void all() throws Exception {
            assertThat(ApplicationFeatureSort.NAMESPACE.isNamespace(), is(true));
            assertThat(ApplicationFeatureSort.TYPE.isNamespace(), is(false));
            assertThat(ApplicationFeatureSort.MEMBER.isNamespace(), is(false));
        }
    }

    public static class HideMemberName extends ApplicationFeatureTypeTest {

        @Test
        public void all() throws Exception {
            assertThat(!ApplicationFeatureSort.NAMESPACE.isMember(), is(true));
            assertThat(!ApplicationFeatureSort.TYPE.isMember(), is(true));
            assertThat(!ApplicationFeatureSort.MEMBER.isMember(), is(false));
        }
    }

    public static class Init extends ApplicationFeatureTypeTest {

        @Test
        public void givenPackage() throws Exception {

            var applicationFeatureId = ApplicationFeatureId.newNamespace("com.mycompany");

            assertThat(applicationFeatureId.getNamespace(), is("com.mycompany"));
            assertThat(applicationFeatureId.getTypeSimpleName(), is(nullValue()));
            assertThat(applicationFeatureId.getLogicalMemberName(), is(nullValue()));

        }
        @Test
        public void givenClass() throws Exception {

            var applicationFeatureId = ApplicationFeatureId.newType("com.mycompany.Bar");

            assertThat(applicationFeatureId.getNamespace(), is("com.mycompany"));
            assertThat(applicationFeatureId.getTypeSimpleName(), is("Bar"));
            assertThat(applicationFeatureId.getLogicalMemberName(), is(nullValue()));

        }
        @Test
        public void givenMember() throws Exception {

            var applicationFeatureId = ApplicationFeatureId.newMember("com.mycompany.Bar#foo");

            assertThat(applicationFeatureId.getNamespace(), is("com.mycompany"));
            assertThat(applicationFeatureId.getTypeSimpleName(), is("Bar"));
            assertThat(applicationFeatureId.getLogicalMemberName(), is("foo"));
        }
        @Test
        public void givenMemberMalformed() throws Exception {
            assertThrows(IllegalArgumentException.class, ()->
                ApplicationFeatureId
                        .newMember("com.mycompany.BarISMISSINGTHEHASHSYMBOL"));
        }
    }

    public static class EnsurePackage extends ApplicationFeatureTypeTest {

        @Test
        public void whenPackage() throws Exception {
            _Asserts.assertIsNamespace(ApplicationFeatureId.newFeature(ApplicationFeatureSort.NAMESPACE, "xxx"));
        }
        @Test
        public void whenClass() throws Exception {
            assertThrows(IllegalStateException.class, ()->
                _Asserts.assertIsNamespace(ApplicationFeatureId.newFeature(ApplicationFeatureSort.TYPE, "x.xxx")));
        }
        @Test
        public void whenMember() throws Exception {
            assertThrows(IllegalStateException.class, ()->
                _Asserts.assertIsNamespace(ApplicationFeatureId.newFeature(ApplicationFeatureSort.MEMBER, "x.xxx#x")));
        }
    }

    public static class EnsurePackageOrClass extends ApplicationFeatureTypeTest {

        @Test
        public void whenPackage() throws Exception {
            _Asserts.assertIsNamespaceOrType(ApplicationFeatureId.newFeature(ApplicationFeatureSort.NAMESPACE, "xxx"));
        }
        @Test
        public void whenClass() throws Exception {
            _Asserts.assertIsNamespaceOrType(ApplicationFeatureId.newFeature(ApplicationFeatureSort.TYPE, "x.xxx"));
        }
        @Test
        public void whenMember() throws Exception {
            assertThrows(IllegalStateException.class, ()->
                _Asserts.assertIsNamespaceOrType(ApplicationFeatureId.newFeature(ApplicationFeatureSort.MEMBER, "x.xxx#x")));
        }

    }

    public static class EnsureClass extends ApplicationFeatureTypeTest {

        @Test
        public void whenPackage() throws Exception {
            assertThrows(IllegalStateException.class, ()->
                _Asserts.assertIsType(ApplicationFeatureId.newFeature(ApplicationFeatureSort.NAMESPACE, "xxx")));
        }
        @Test
        public void whenClass() throws Exception {
            _Asserts.assertIsType(ApplicationFeatureId.newFeature(ApplicationFeatureSort.TYPE, "x.xxx"));
        }
        @Test
        public void whenMember() throws Exception {
            assertThrows(IllegalStateException.class, ()->
                _Asserts.assertIsType(ApplicationFeatureId.newFeature(ApplicationFeatureSort.MEMBER, "x.xxx#x")));
        }

    }

    public static class EnsureMember extends ApplicationFeatureTypeTest {

        @Test
        public void whenPackage() throws Exception {
            assertThrows(IllegalStateException.class, ()->
                _Asserts.assertIsMember(ApplicationFeatureId.newFeature(ApplicationFeatureSort.NAMESPACE, "xxx")));
        }
        @Test
        public void whenClass() throws Exception {
            assertThrows(IllegalStateException.class, ()->
                _Asserts.assertIsMember(ApplicationFeatureId.newFeature(ApplicationFeatureSort.TYPE, "x.xxx")));
        }
        @Test
        public void whenMember() throws Exception {
            _Asserts.assertIsMember(ApplicationFeatureId.newFeature(ApplicationFeatureSort.MEMBER, "x.xxx#x"));
        }
    }

    public static class ToString extends ApplicationFeatureTypeTest {

        @Test
        public void happyCase() throws Exception {
            assertThat(ApplicationFeatureSort.NAMESPACE.toString(), is("NAMESPACE"));
        }
    }

}