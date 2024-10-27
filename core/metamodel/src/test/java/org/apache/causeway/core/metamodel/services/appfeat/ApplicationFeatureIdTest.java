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

import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.causeway.applib.services.appfeat.ApplicationFeatureId;
import org.apache.causeway.applib.services.appfeat.ApplicationFeatureSort;

class ApplicationFeatureIdTest {

    @Nested
    class BasicsTest {

        @Test
        void title_happyCase() throws Exception {
            // when
            var applicationFeatureId = ApplicationFeatureId.newMember("com.mycompany.Bar#foo");
            // then
            assertThat(applicationFeatureId.title(), is("com.mycompany.Bar#foo"));
        }

        @Test
        void newPackage() throws Exception {
            // when
            var applicationFeatureId = ApplicationFeatureId.newNamespace("com.mycompany");
            // then
            assertThat(applicationFeatureId.getSort(), is(ApplicationFeatureSort.NAMESPACE));
            assertThat(applicationFeatureId.getNamespace(), is("com.mycompany"));
            assertThat(applicationFeatureId.getTypeSimpleName(), is(nullValue()));
            assertThat(applicationFeatureId.getLogicalMemberName(), is(nullValue()));
        }

        @Test
        void newClass() throws Exception {
            // when
            var applicationFeatureId = ApplicationFeatureId.newType("com.mycompany.Bar");
            // then
            assertThat(applicationFeatureId.getSort(), is(ApplicationFeatureSort.TYPE));
            assertThat(applicationFeatureId.getNamespace(), is("com.mycompany"));
            assertThat(applicationFeatureId.getTypeSimpleName(), is("Bar"));
            assertThat(applicationFeatureId.getLogicalMemberName(), is(nullValue()));
        }
    }

    @Nested
    class NewMemberTest {

        @Test
        void using_fullyQualifiedClassName_and_MemberName() throws Exception {
            // when
            var applicationFeatureId = ApplicationFeatureId.newMember("com.mycompany.Bar", "foo");
            // then
            assertThat(applicationFeatureId.getSort(), is(ApplicationFeatureSort.MEMBER));
            assertThat(applicationFeatureId.getNamespace(), is("com.mycompany"));
            assertThat(applicationFeatureId.getTypeSimpleName(), is("Bar"));
            assertThat(applicationFeatureId.getLogicalMemberName(), is("foo"));
        }

        @Test
        void using_fullyQualifiedName() throws Exception {
            // when
            var applicationFeatureId = ApplicationFeatureId.newMember("com.mycompany.Bar#foo");
            // then
            assertThat(applicationFeatureId.getSort(), is(ApplicationFeatureSort.MEMBER));
            assertThat(applicationFeatureId.getNamespace(), is("com.mycompany"));
            assertThat(applicationFeatureId.getTypeSimpleName(), is("Bar"));
            assertThat(applicationFeatureId.getLogicalMemberName(), is("foo"));
        }
    }

    @Nested
    class NewFeatureTest {

        @Test
        void aftString_whenPackage() throws Exception {
            // when
            var applicationFeatureId = ApplicationFeatureId.newFeature(ApplicationFeatureSort.NAMESPACE, "com.mycompany");
            // then
            assertThat(applicationFeatureId, is(ApplicationFeatureId.newNamespace("com.mycompany")));
        }

        @Test
        void aftString_whenClass() throws Exception {
            // when
            var applicationFeatureId = ApplicationFeatureId.newFeature(ApplicationFeatureSort.TYPE, "com.mycompany.Bar");
            // then
            assertThat(applicationFeatureId, is(ApplicationFeatureId.newType("com.mycompany.Bar")));
        }

        @Test
        void aftString_whenMember() throws Exception {
            // when
            var applicationFeatureId = ApplicationFeatureId.newFeature(ApplicationFeatureSort.MEMBER, "com.mycompany.Bar#foo");
            // then
            assertThat(applicationFeatureId, is(ApplicationFeatureId.newMember("com.mycompany.Bar","foo")));
        }

        @Test
        void string3_whenPackage() throws Exception {
            // when
            var applicationFeatureId = ApplicationFeatureId.newFeature("com.mycompany", null, null);
            // then
            assertThat(applicationFeatureId, is(ApplicationFeatureId.newNamespace("com.mycompany")));
        }

        @Test
        void string3_whenClass() throws Exception {
            // when
            var applicationFeatureId = ApplicationFeatureId.newFeature("com.mycompany", "Bar", null);
            // then
            assertThat(applicationFeatureId, is(ApplicationFeatureId.newType("com.mycompany.Bar")));
        }

        @Test
        void string3_whenMember() throws Exception {
            // when
            var applicationFeatureId = ApplicationFeatureId.newFeature("com.mycompany", "Bar", "foo");
            // then
            assertThat(applicationFeatureId, is(ApplicationFeatureId.newMember("com.mycompany.Bar","foo")));
        }
    }

    @Nested
    class GetParentIdsTest {

        @Test
        void whenPackageWithNoParent() throws Exception {

            // given
            var applicationFeatureId = ApplicationFeatureId.newNamespace("com");

            // when
            final List<ApplicationFeatureId> parentIds = applicationFeatureId.getParentFeatureIds()
                    .toList();

            // then
            assertThat(parentIds, emptyCollectionOf(ApplicationFeatureId.class));
        }

        @Test
        void whenPackageWithHasParent() throws Exception {

            // given
            var applicationFeatureId = ApplicationFeatureId.newNamespace("com.mycompany");

            // when
            final List<ApplicationFeatureId> parentIds = applicationFeatureId.getParentFeatureIds()
                    .toList();

            // then
            assertThat(parentIds, contains(ApplicationFeatureId.newNamespace("com")));
        }

        @Test
        void whenPackageWithHasParents() throws Exception {

            // given
            var applicationFeatureId = ApplicationFeatureId.newNamespace("com.mycompany.bish.bosh");

            // when
            final List<ApplicationFeatureId> parentIds = applicationFeatureId.getParentFeatureIds()
                    .toList();

            // then
            assertThat(parentIds, contains(
                    ApplicationFeatureId.newNamespace("com.mycompany.bish"),
                    ApplicationFeatureId.newNamespace("com.mycompany"),
                    ApplicationFeatureId.newNamespace("com")
                    ));
        }

        @Test
        void whenClassWithParents() throws Exception {

            // given
            var applicationFeatureId = ApplicationFeatureId.newType("com.mycompany.Bar");

            // when
            final List<ApplicationFeatureId> parentIds = applicationFeatureId.getParentFeatureIds()
                    .toList();

            // then
            assertThat(parentIds, contains(
                    ApplicationFeatureId.newNamespace("com.mycompany"),
                    ApplicationFeatureId.newNamespace("com")
                    ));
        }

        @Test
        void whenMember() throws Exception {

            // given
            var applicationFeatureId = ApplicationFeatureId.newMember("com.mycompany.Bar", "foo");

            // when
            final List<ApplicationFeatureId> parentIds = applicationFeatureId.getParentFeatureIds()
                    .toList();

            // then
            assertThat(parentIds, contains(
                    ApplicationFeatureId.newType("com.mycompany.Bar"),
                    ApplicationFeatureId.newNamespace("com.mycompany"),
                    ApplicationFeatureId.newNamespace("com")
                    ));
        }
    }

    @Nested
    class GetParentPackageIdTest {

        @Test
        void givenPackageWhenParentIsNotRoot() throws Exception {
            // given
            var applicationFeatureId = ApplicationFeatureId.newNamespace("com.mycompany");
            // when
            var parentPackageId = applicationFeatureId.getParentNamespaceFeatureId();
            // then
            assertThat(parentPackageId.getSort(), is(ApplicationFeatureSort.NAMESPACE));
            assertThat(parentPackageId.getNamespace(), is("com"));
            assertThat(parentPackageId.getTypeSimpleName(), is(nullValue()));
            assertThat(parentPackageId.getLogicalMemberName(), is(nullValue()));
        }

        @Test
        void givenPackageWhenParentIsRoot() throws Exception {
            // given
            var applicationFeatureId = ApplicationFeatureId.newNamespace("com");
            // when
            var parentPackageId = applicationFeatureId.getParentNamespaceFeatureId();
            // then
            assertThat(parentPackageId, is(nullValue()));
        }

        @Test
        void givenRootPackage() throws Exception {
            // given
            var applicationFeatureId = ApplicationFeatureId.newNamespace("");
            // when
            var parentPackageId = applicationFeatureId.getParentNamespaceFeatureId();
            // then
            assertThat(parentPackageId, is(nullValue()));
        }

        @Test
        void givenClass() throws Exception {
            // given
            var applicationFeatureId = ApplicationFeatureId.newType("com.mycompany.Bar");

            // when
            var parentPackageId = applicationFeatureId.getParentNamespaceFeatureId();

            // then
            assertThat(parentPackageId.getSort(), is(ApplicationFeatureSort.NAMESPACE));
            assertThat(parentPackageId.getNamespace(), is("com.mycompany"));
            assertThat(parentPackageId.getTypeSimpleName(), is(nullValue()));
            assertThat(parentPackageId.getLogicalMemberName(), is(nullValue()));
        }

        @Test
        void givenClassInRootPackage() throws Exception {
            // when
            assertThrows(IllegalArgumentException.class, ()->
                ApplicationFeatureId.newType("Bar"));
        }

        @Test
        void givenMember() throws Exception {
            // given
            var applicationFeatureId = ApplicationFeatureId.newMember("com.mycompany.Bar", "foo");
            // when
            assertThrows(AssertionError.class, ()->
                applicationFeatureId.getParentNamespaceFeatureId());
        }
    }

    @Nested
    class GetParentClassTest {

        @Test
        void givenMember() throws Exception {
            // given
            var applicationFeatureId = ApplicationFeatureId.newMember("com.mycompany.Bar", "foo");

            // when
            var parentClassId = applicationFeatureId.getParentTypeFeatureId();

            // then
            assertThat(parentClassId.getSort(), is(ApplicationFeatureSort.TYPE));
            assertThat(parentClassId.getNamespace(), is("com.mycompany"));
            assertThat(parentClassId.getTypeSimpleName(), is("Bar"));
            assertThat(parentClassId.getLogicalMemberName(), is(nullValue()));
        }

        @Test
        void givenPackage() throws Exception {
            // given
            var applicationFeatureId = ApplicationFeatureId.newNamespace("com");
            // when
            assertThrows(AssertionError.class, ()->
                applicationFeatureId.getParentTypeFeatureId());
        }

        @Test
        void givenClass() throws Exception {
            // given
            var applicationFeatureId = ApplicationFeatureId.newType("com.mycompany.Bar");
            // when
            assertThrows(AssertionError.class, ()->
                applicationFeatureId.getParentTypeFeatureId());
        }
    }

    @Nested
    class CompareToTest {

        ApplicationFeatureId feature1;

        @Test
        void members() throws Exception {
            feature1 = ApplicationFeatureId.newMember("com.mycompany.Bar#b");

            assertThat(feature1.toString(),
                    is(equalTo("ApplicationFeatureId{sort=MEMBER, "
                    + "namespace=com.mycompany, typeSimpleName=Bar, memberName=b}")));
        }

        @Test
        void classes() throws Exception {
            feature1 = ApplicationFeatureId.newType("com.mycompany.B");

            assertThat(feature1.toString(),
                    is(equalTo("ApplicationFeatureId{sort=TYPE, namespace=com.mycompany, typeSimpleName=B}")));
        }

        @Test
        void packages() throws Exception {
            feature1 = ApplicationFeatureId.newNamespace("com.b");

            assertThat(feature1.toString(),
                    is(equalTo("ApplicationFeatureId{sort=NAMESPACE, namespace=com.b}")));
        }
    }

    @Nested
    class ToStringTest {

        ApplicationFeatureId feature1;
        ApplicationFeatureId feature2;

        @Test
        void members() throws Exception {
            feature1 = ApplicationFeatureId.newMember("com.mycompany.Bar#b");

            feature2 = ApplicationFeatureId.newMember("com.mycompany.Bar#c");
            assertThat(feature1.compareTo(feature2), is(lessThan(0)));

            feature2 = ApplicationFeatureId.newMember("com.mycompany.Bar#b");
            assertThat(feature1.compareTo(feature2), is(equalTo(0)));

            feature2 = ApplicationFeatureId.newMember("com.mycompany.Bar#a");
            assertThat(feature1.compareTo(feature2), is(greaterThan(0)));

            feature2 = ApplicationFeatureId.newType("com.mycompany.Bar");
            assertThat(feature1.compareTo(feature2), is(greaterThan(0)));

            feature2 = ApplicationFeatureId.newNamespace("com.mycompany");
            assertThat(feature1.compareTo(feature2), is(greaterThan(0)));
        }

        @Test
        void classes() throws Exception {
            feature1 = ApplicationFeatureId.newType("com.mycompany.B");

            feature2 = ApplicationFeatureId.newType("com.mycompany.C");
            assertThat(feature1.compareTo(feature2), is(lessThan(0)));

            feature2 = ApplicationFeatureId.newType("com.mycompany.B");
            assertThat(feature1.compareTo(feature2), is(equalTo(0)));

            feature2 = ApplicationFeatureId.newType("com.mycompany.A");
            assertThat(feature1.compareTo(feature2), is(greaterThan(0)));

            feature2 = ApplicationFeatureId.newNamespace("com.mycompany");
            assertThat(feature1.compareTo(feature2), is(greaterThan(0)));
        }

        @Test
        void packages() throws Exception {
            feature1 = ApplicationFeatureId.newNamespace("com.b");

            feature2 = ApplicationFeatureId.newNamespace("com.c");
            assertThat(feature1.compareTo(feature2), is(lessThan(0)));

            feature2 = ApplicationFeatureId.newNamespace("com.b");
            assertThat(feature1.compareTo(feature2), is(equalTo(0)));

            feature2 = ApplicationFeatureId.newNamespace("com.a");
            assertThat(feature1.compareTo(feature2), is(greaterThan(0)));
        }
    }

    @Nested
    class GetClassNameTest {

        private Function<ApplicationFeatureId, String> func = ApplicationFeatureId::getTypeSimpleName;

        @Test
        void whenNull() throws Exception {
            assertThrows(NullPointerException.class, ()->
                func.apply(null));
        }

        @Test
        void whenPackage() throws Exception {
            assertThat(func.apply(ApplicationFeatureId.newNamespace("com.mycompany")), is(nullValue()));
        }

        @Test
        void whenClass() throws Exception {
            assertThat(func.apply(ApplicationFeatureId.newType("com.mycompany.Bar")), is("Bar"));
        }

        @Test
        void whenMember() throws Exception {
            assertThat(func.apply(ApplicationFeatureId.newMember("com.mycompany.Bar#foo")), is("Bar"));
        }

    }

    @Nested
    class GetMemberNameTest {

        private Function<ApplicationFeatureId, String> func =
                ApplicationFeatureId::getLogicalMemberName;

        @Test
        void whenNull() throws Exception {
            assertThrows(NullPointerException.class, ()->
            func.apply(null));
        }

        @Test
        void whenPackage() throws Exception {
            assertThat(func.apply(ApplicationFeatureId.newNamespace("com.mycompany")), is(nullValue()));
        }

        @Test
        void whenClass() throws Exception {
            assertThat(func.apply(ApplicationFeatureId.newType("com.mycompany.Bar")), is(nullValue()));
        }

        @Test
        void whenMember() throws Exception {
            assertThat(func.apply(ApplicationFeatureId.newMember("com.mycompany.Bar#foo")), is("foo"));
        }

    }

}