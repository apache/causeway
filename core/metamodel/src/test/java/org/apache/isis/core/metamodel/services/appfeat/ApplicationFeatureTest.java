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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.applib.services.appfeat.ApplicationMemberSort;

import lombok.val;

public class ApplicationFeatureTest {

    public static class GetContents_and_AddToContents extends ApplicationFeatureTest {

        private static ApplicationFeatureDefault newApplicationFeature(ApplicationFeatureId featId) {
            return new ApplicationFeatureDefault(featId);
        }
        
        @Rule
        public ExpectedException expectedException = ExpectedException.none();

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

            expectedException.expect(IllegalStateException.class);

            val applicationFeature = newApplicationFeature(ApplicationFeatureId.newNamespace("com.mycompany"));
            val memberFeatureId = ApplicationFeatureId.newMember("com.mycompany.Bar", "foo");

            applicationFeature.addToContents(memberFeatureId);
        }

        @Test
        public void givenClass() throws Exception {

            expectedException.expect(IllegalStateException.class);

            val applicationFeature = newApplicationFeature(ApplicationFeatureId.newType("com.mycompany.Bar"));
            val classFeatureId = ApplicationFeatureId.newType("com.mycompany.flob.Bar");

            applicationFeature.addToContents(classFeatureId);
        }

        @Test
        public void givenMember() throws Exception {

            expectedException.expect(IllegalStateException.class);

            val applicationFeature = newApplicationFeature(ApplicationFeatureId.newMember("com.mycompany.Bar", "foo"));
            val classFeatureId = ApplicationFeatureId.newType("com.mycompany.flob.Bar");

            applicationFeature.addToContents(classFeatureId);
        }

    }

    public static class GetMembers_and_AddToMembers extends ApplicationFeatureTest {

        private static ApplicationFeatureDefault newApplicationFeature(ApplicationFeatureId featId) {
            return new ApplicationFeatureDefault(featId);
        }
        
        @Rule
        public ExpectedException expectedException = ExpectedException.none();

        @Test
        public void givenPackage() throws Exception {

            expectedException.expect(IllegalStateException.class);

            val applicationFeature = newApplicationFeature(ApplicationFeatureId.newNamespace("com.mycompany"));
            val memberFeatureId = ApplicationFeatureId.newMember("com.mycompany.Bar", "foo");

            applicationFeature.addToMembers(memberFeatureId, ApplicationMemberSort.PROPERTY);
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

            expectedException.expect(IllegalStateException.class);

            val applicationFeature = newApplicationFeature(ApplicationFeatureId.newType("com.mycompany.Bar"));
            val packageFeatureId = ApplicationFeatureId.newNamespace("com.mycompany");

            applicationFeature.addToMembers(packageFeatureId, ApplicationMemberSort.PROPERTY);
        }

        @Test
        public void givenClass_whenAddClass() throws Exception {

            expectedException.expect(IllegalStateException.class);

            val applicationFeature = newApplicationFeature(ApplicationFeatureId.newType("com.mycompany.Bar"));
            val classFeatureId = ApplicationFeatureId.newType("com.mycompany.Bop");

            applicationFeature.addToMembers(classFeatureId, ApplicationMemberSort.PROPERTY);
        }

        @Test
        public void givenMember() throws Exception {

            expectedException.expect(IllegalStateException.class);

            val applicationFeature = newApplicationFeature(ApplicationFeatureId.newMember("com.mycompany.Bar", "foo"));
            val classFeatureId = ApplicationFeatureId.newType("com.mycompany.flob.Bar");

            applicationFeature.addToMembers(classFeatureId, ApplicationMemberSort.PROPERTY);
        }
    }


    public static class MethodsTest extends ApplicationFeatureTest {

        private static ApplicationFeatureDefault newApplicationFeature(ApplicationFeatureId featId) {
            return new ApplicationFeatureDefault(featId);
        }
        
        @Test
        public void getFullyQualifiedName() throws Exception {
            val input = newApplicationFeature(ApplicationFeatureId.newMember("com.mycompany.Foo#bar"));
            assertThat(input.getFullyQualifiedName(), is("com.mycompany.Foo#bar"));
        }
    }



}