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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.causeway.applib.services.appfeat.ApplicationFeatureId;
import org.apache.causeway.applib.services.appfeat.ApplicationMemberSort;

import lombok.val;

class ApplicationFeatureTest {

    public static class GetContents_and_AddToContents extends ApplicationFeatureTest {

        private static ApplicationFeatureDefault newApplicationFeature(final ApplicationFeatureId featId) {
            return new ApplicationFeatureDefault(featId);
        }

        @Test
        public void givenPackage_whenAddPackageAndClass() throws Exception {
            val applicationFeature = newApplicationFeature(ApplicationFeatureId.newNamespace("com.mycompany"));
            val packageFeatureId = ApplicationFeatureId.newNamespace("com.mycompany.flob");
            val classFeatureId = ApplicationFeatureId.newType("com.mycompany.Bar");

            applicationFeature.addToContents(packageFeatureId);
            applicationFeature.addToContents(classFeatureId);

            assertThat(applicationFeature.getContents().size(), is(2));
            assertThat(applicationFeature.getContents(), containsInAnyOrder(packageFeatureId, classFeatureId));
        }

        @Test
        public void givenPackage_whenAddMember() throws Exception {
            val applicationFeature = newApplicationFeature(ApplicationFeatureId.newNamespace("com.mycompany"));
            val memberFeatureId = ApplicationFeatureId.newMember("com.mycompany.Bar", "foo");

            assertThrows(IllegalStateException.class, ()->
                applicationFeature.addToContents(memberFeatureId));
        }

        @Test
        public void givenClass() throws Exception {
            val applicationFeature = newApplicationFeature(ApplicationFeatureId.newType("com.mycompany.Bar"));
            val classFeatureId = ApplicationFeatureId.newType("com.mycompany.flob.Bar");
            assertThrows(IllegalStateException.class, ()->
                applicationFeature.addToContents(classFeatureId));
        }

        @Test
        public void givenMember() throws Exception {
            val applicationFeature = newApplicationFeature(ApplicationFeatureId.newMember("com.mycompany.Bar", "foo"));
            val classFeatureId = ApplicationFeatureId.newType("com.mycompany.flob.Bar");
            assertThrows(IllegalStateException.class, ()->
                applicationFeature.addToContents(classFeatureId));
        }

    }

    public static class GetMembers_and_AddToMembers extends ApplicationFeatureTest {

        private static ApplicationFeatureDefault newApplicationFeature(final ApplicationFeatureId featId) {
            return new ApplicationFeatureDefault(featId);
        }

        @Test
        public void givenPackage() throws Exception {
            val applicationFeature = newApplicationFeature(ApplicationFeatureId.newNamespace("com.mycompany"));
            val memberFeatureId = ApplicationFeatureId.newMember("com.mycompany.Bar", "foo");

            assertThrows(IllegalStateException.class, ()->
                applicationFeature.addToMembers(memberFeatureId, ApplicationMemberSort.PROPERTY));
        }

        @Test
        public void givenClass_whenAddMember() throws Exception {

            val applicationFeature = newApplicationFeature(ApplicationFeatureId.newType("com.mycompany.Bar"));
            val memberFeatureId = ApplicationFeatureId.newMember("com.mycompany.Bar", "foo");
            val memberFeatureId2 = ApplicationFeatureId.newMember("com.mycompany.Bar", "boz");

            applicationFeature.addToMembers(memberFeatureId, ApplicationMemberSort.PROPERTY);
            applicationFeature.addToMembers(memberFeatureId2, ApplicationMemberSort.PROPERTY);

            assertThat(applicationFeature.getProperties().size(), is(2));
            assertThat(applicationFeature.getProperties(), containsInAnyOrder(memberFeatureId, memberFeatureId2));
        }

        @Test
        public void givenClass_whenAddPackage() throws Exception {

            val applicationFeature = newApplicationFeature(ApplicationFeatureId.newType("com.mycompany.Bar"));
            val packageFeatureId = ApplicationFeatureId.newNamespace("com.mycompany");

            assertThrows(IllegalStateException.class, ()->
                applicationFeature.addToMembers(packageFeatureId, ApplicationMemberSort.PROPERTY));
        }

        @Test
        public void givenClass_whenAddClass() throws Exception {

            val applicationFeature = newApplicationFeature(ApplicationFeatureId.newType("com.mycompany.Bar"));
            val classFeatureId = ApplicationFeatureId.newType("com.mycompany.Bop");

            assertThrows(IllegalStateException.class, ()->
                applicationFeature.addToMembers(classFeatureId, ApplicationMemberSort.PROPERTY));
        }

        @Test
        public void givenMember() throws Exception {

            val applicationFeature = newApplicationFeature(ApplicationFeatureId.newMember("com.mycompany.Bar", "foo"));
            val classFeatureId = ApplicationFeatureId.newType("com.mycompany.flob.Bar");

            assertThrows(IllegalStateException.class, ()->
                applicationFeature.addToMembers(classFeatureId, ApplicationMemberSort.PROPERTY));
        }
    }


    public static class MethodsTest extends ApplicationFeatureTest {

        private static ApplicationFeatureDefault newApplicationFeature(final ApplicationFeatureId featId) {
            return new ApplicationFeatureDefault(featId);
        }

        @Test
        public void getFullyQualifiedName() throws Exception {
            val input = newApplicationFeature(ApplicationFeatureId.newMember("com.mycompany.Foo#bar"));
            assertThat(input.getFullyQualifiedName(), is("com.mycompany.Foo#bar"));
        }
    }



}