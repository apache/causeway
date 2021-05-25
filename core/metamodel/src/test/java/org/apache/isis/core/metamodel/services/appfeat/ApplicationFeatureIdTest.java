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

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;

import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureSort;
import org.apache.isis.core.internaltestsupport.contract.ValueTypeContractTestAbstract;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2;

import lombok.val;

public class ApplicationFeatureIdTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    public static class Title extends ApplicationFeatureIdTest {

        @Test
        public void happyCase() throws Exception {
            val applicationFeatureId = ApplicationFeatureId.newMember("com.mycompany.Bar#foo");

            assertThat(applicationFeatureId.title(), is("com.mycompany.Bar#foo"));
        }
    }

    public static class NewPackage extends ApplicationFeatureIdTest {

        @Test
        public void testNewPackage() throws Exception {
            // when
            val applicationFeatureId = ApplicationFeatureId.newNamespace("com.mycompany");
            // then
            assertThat(applicationFeatureId.getSort(), is(ApplicationFeatureSort.NAMESPACE));
            assertThat(applicationFeatureId.getNamespace(), is("com.mycompany"));
            assertThat(applicationFeatureId.getTypeSimpleName(), is(nullValue()));
            assertThat(applicationFeatureId.getMemberLogicalName(), is(nullValue()));
        }
    }

    public static class NewClass extends ApplicationFeatureIdTest {

        @Test
        public void testNewClass() throws Exception {
            // when
            val applicationFeatureId = ApplicationFeatureId.newType("com.mycompany.Bar");
            // then
            assertThat(applicationFeatureId.getSort(), is(ApplicationFeatureSort.TYPE));
            assertThat(applicationFeatureId.getNamespace(), is("com.mycompany"));
            assertThat(applicationFeatureId.getTypeSimpleName(), is("Bar"));
            assertThat(applicationFeatureId.getMemberLogicalName(), is(nullValue()));
        }
    }

    public static class NewMember extends ApplicationFeatureIdTest {

        @Test
        public void using_fullyQualifiedClassName_and_MemberName() throws Exception {
            // when
            val applicationFeatureId = ApplicationFeatureId.newMember("com.mycompany.Bar", "foo");
            // then
            assertThat(applicationFeatureId.getSort(), is(ApplicationFeatureSort.MEMBER));
            assertThat(applicationFeatureId.getNamespace(), is("com.mycompany"));
            assertThat(applicationFeatureId.getTypeSimpleName(), is("Bar"));
            assertThat(applicationFeatureId.getMemberLogicalName(), is("foo"));
        }

        @Test
        public void using_fullyQualifiedName() throws Exception {
            // when
            val applicationFeatureId = ApplicationFeatureId.newMember("com.mycompany.Bar#foo");
            // then
            assertThat(applicationFeatureId.getSort(), is(ApplicationFeatureSort.MEMBER));
            assertThat(applicationFeatureId.getNamespace(), is("com.mycompany"));
            assertThat(applicationFeatureId.getTypeSimpleName(), is("Bar"));
            assertThat(applicationFeatureId.getMemberLogicalName(), is("foo"));
        }

    }

    public static class NewFeature_AFT_String extends ApplicationFeatureIdTest {

        @Test
        public void whenPackage() throws Exception {
            // when
            val applicationFeatureId = ApplicationFeatureId.newFeature(ApplicationFeatureSort.NAMESPACE, "com.mycompany");
            // then
            assertThat(applicationFeatureId, is(ApplicationFeatureId.newNamespace("com.mycompany")));
        }

        @Test
        public void whenClass() throws Exception {
            // when
            val applicationFeatureId = ApplicationFeatureId.newFeature(ApplicationFeatureSort.TYPE, "com.mycompany.Bar");
            // then
            assertThat(applicationFeatureId, is(ApplicationFeatureId.newType("com.mycompany.Bar")));
        }

        @Test
        public void whenMember() throws Exception {
            // when
            val applicationFeatureId = ApplicationFeatureId.newFeature(ApplicationFeatureSort.MEMBER, "com.mycompany.Bar#foo");
            // then
            assertThat(applicationFeatureId, is(ApplicationFeatureId.newMember("com.mycompany.Bar","foo")));
        }
    }

    public static class NewFeature_String_String_String extends ApplicationFeatureIdTest {

        @Test
        public void whenPackage() throws Exception {
            // when
            val applicationFeatureId = ApplicationFeatureId.newFeature("com.mycompany", null, null);
            // then
            assertThat(applicationFeatureId, is(ApplicationFeatureId.newNamespace("com.mycompany")));
        }

        @Test
        public void whenClass() throws Exception {
            // when
            val applicationFeatureId = ApplicationFeatureId.newFeature("com.mycompany", "Bar", null);
            // then
            assertThat(applicationFeatureId, is(ApplicationFeatureId.newType("com.mycompany.Bar")));
        }

        @Test
        public void whenMember() throws Exception {
            // when
            val applicationFeatureId = ApplicationFeatureId.newFeature("com.mycompany", "Bar", "foo");
            // then
            assertThat(applicationFeatureId, is(ApplicationFeatureId.newMember("com.mycompany.Bar","foo")));
        }
    }

    public static class GetParentIds extends ApplicationFeatureIdTest {

        @Test
        public void whenPackageWithNoParent() throws Exception {

            // given
            val applicationFeatureId = ApplicationFeatureId.newNamespace("com");

            // when
            final List<ApplicationFeatureId> parentIds = applicationFeatureId.getParentFeatureIds()
                    .toList();

            // then
            assertThat(parentIds, emptyCollectionOf(ApplicationFeatureId.class));
        }

        @Test
        public void whenPackageWithHasParent() throws Exception {

            // given
            val applicationFeatureId = ApplicationFeatureId.newNamespace("com.mycompany");

            // when
            final List<ApplicationFeatureId> parentIds = applicationFeatureId.getParentFeatureIds()
                    .toList();

            // then
            assertThat(parentIds, contains(ApplicationFeatureId.newNamespace("com")));
        }

        @Test
        public void whenPackageWithHasParents() throws Exception {

            // given
            val applicationFeatureId = ApplicationFeatureId.newNamespace("com.mycompany.bish.bosh");

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
        public void whenClassWithParents() throws Exception {

            // given
            val applicationFeatureId = ApplicationFeatureId.newType("com.mycompany.Bar");

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
        public void whenMember() throws Exception {

            // given
            val applicationFeatureId = ApplicationFeatureId.newMember("com.mycompany.Bar", "foo");

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

    public static class GetParentPackageId extends ApplicationFeatureIdTest {

        @Test
        public void givenPackageWhenParentIsNotRoot() throws Exception {
            // given
            val applicationFeatureId = ApplicationFeatureId.newNamespace("com.mycompany");
            // when
            val parentPackageId = applicationFeatureId.getParentNamespaceFeatureId();
            // then
            assertThat(parentPackageId.getSort(), is(ApplicationFeatureSort.NAMESPACE));
            assertThat(parentPackageId.getNamespace(), is("com"));
            assertThat(parentPackageId.getTypeSimpleName(), is(nullValue()));
            assertThat(parentPackageId.getMemberLogicalName(), is(nullValue()));
        }

        @Test
        public void givenPackageWhenParentIsRoot() throws Exception {
            // given
            val applicationFeatureId = ApplicationFeatureId.newNamespace("com");
            // when
            val parentPackageId = applicationFeatureId.getParentNamespaceFeatureId();
            // then
            assertThat(parentPackageId, is(nullValue()));
        }

        @Test
        public void givenRootPackage() throws Exception {
            // given
            val applicationFeatureId = ApplicationFeatureId.newNamespace("");
            // when
            val parentPackageId = applicationFeatureId.getParentNamespaceFeatureId();
            // then
            assertThat(parentPackageId, is(nullValue()));
        }

        @Test
        public void givenClass() throws Exception {
            // given
            val applicationFeatureId = ApplicationFeatureId.newType("com.mycompany.Bar");

            // when
            val parentPackageId = applicationFeatureId.getParentNamespaceFeatureId();

            // then
            assertThat(parentPackageId.getSort(), is(ApplicationFeatureSort.NAMESPACE));
            assertThat(parentPackageId.getNamespace(), is("com.mycompany"));
            assertThat(parentPackageId.getTypeSimpleName(), is(nullValue()));
            assertThat(parentPackageId.getMemberLogicalName(), is(nullValue()));
        }

        @Test
        public void givenClassInRootPackage() throws Exception {

            // expect
            expectedException.expect(IllegalArgumentException.class);

            // when
            ApplicationFeatureId.newType("Bar");
        }

        @Test
        public void givenMember() throws Exception {

            // given
            val applicationFeatureId = ApplicationFeatureId.newMember("com.mycompany.Bar", "foo");

            // then
            expectedException.expect(AssertionError.class);

            // when
            applicationFeatureId.getParentNamespaceFeatureId();
        }

    }

    public static class GetParentClass extends ApplicationFeatureIdTest {

        @Test
        public void givenMember() throws Exception {
            // given
            val applicationFeatureId = ApplicationFeatureId.newMember("com.mycompany.Bar", "foo");

            // when
            val parentClassId = applicationFeatureId.getParentTypeFeatureId();

            // then
            assertThat(parentClassId.getSort(), is(ApplicationFeatureSort.TYPE));
            assertThat(parentClassId.getNamespace(), is("com.mycompany"));
            assertThat(parentClassId.getTypeSimpleName(), is("Bar"));
            assertThat(parentClassId.getMemberLogicalName(), is(nullValue()));
        }

        @Test
        public void givenPackage() throws Exception {
            // given
            val applicationFeatureId = ApplicationFeatureId.newNamespace("com");

            // then
            expectedException.expect(AssertionError.class);

            // when
            applicationFeatureId.getParentTypeFeatureId();
        }

        @Test
        public void givenClass() throws Exception {

            // given
            val applicationFeatureId = ApplicationFeatureId.newType("com.mycompany.Bar");

            // then
            expectedException.expect(AssertionError.class);

            // when
            applicationFeatureId.getParentTypeFeatureId();
        }
    }

    public static abstract class ValueTypeContractTest extends ValueTypeContractTestAbstract<ApplicationFeatureId> {

        public static class PackageFeatures extends ValueTypeContractTest {

            @Override
            protected List<ApplicationFeatureId> getObjectsWithSameValue() {
                return Arrays.asList(
                        ApplicationFeatureId.newNamespace("com.mycompany"),
                        ApplicationFeatureId.newNamespace("com.mycompany"));
            }

            @Override
            protected List<ApplicationFeatureId> getObjectsWithDifferentValue() {
                return Arrays.asList(
                        ApplicationFeatureId.newNamespace("com.mycompany2"),
                        ApplicationFeatureId.newType("com.mycompany.Foo"),
                        ApplicationFeatureId.newMember("com.mycompany.Foo#bar"));
            }
        }

        public static class ClassFeatures extends ValueTypeContractTest {

            @Override
            protected List<ApplicationFeatureId> getObjectsWithSameValue() {
                return Arrays.asList(
                        ApplicationFeatureId.newType("com.mycompany.Foo"),
                        ApplicationFeatureId.newType("com.mycompany.Foo"));
            }

            @Override
            protected List<ApplicationFeatureId> getObjectsWithDifferentValue() {
                return Arrays.asList(
                        ApplicationFeatureId.newNamespace("com.mycompany"),
                        ApplicationFeatureId.newType("com.mycompany.Foo2"),
                        ApplicationFeatureId.newMember("com.mycompany.Foo#bar"));
            }
        }

        public static class MemberFeatures extends ValueTypeContractTest {

            @Override
            protected List<ApplicationFeatureId> getObjectsWithSameValue() {
                return Arrays.asList(
                        ApplicationFeatureId.newMember("com.mycompany.Foo#bar"),
                        ApplicationFeatureId.newMember("com.mycompany.Foo#bar"));
            }

            @Override
            protected List<ApplicationFeatureId> getObjectsWithDifferentValue() {
                return Arrays.asList(
                        ApplicationFeatureId.newNamespace("com.mycompany"),
                        ApplicationFeatureId.newType("com.mycompany.Foo"),
                        ApplicationFeatureId.newMember("com.mycompany.Foo#bar2"));
            }
        }

    }

    public static class CompareToTest extends ApplicationFeatureIdTest {

        ApplicationFeatureId feature1;

        @Test
        public void members() throws Exception {
            feature1 = ApplicationFeatureId.newMember("com.mycompany.Bar#b");

            assertThat(feature1.toString(),
                    is(equalTo("ApplicationFeatureId{sort=MEMBER, "
                    + "namespace=com.mycompany, typeSimpleName=Bar, memberName=b}")));
        }

        @Test
        public void classes() throws Exception {
            feature1 = ApplicationFeatureId.newType("com.mycompany.B");

            assertThat(feature1.toString(),
                    is(equalTo("ApplicationFeatureId{sort=TYPE, namespace=com.mycompany, typeSimpleName=B}")));
        }

        @Test
        public void packages() throws Exception {
            feature1 = ApplicationFeatureId.newNamespace("com.b");

            assertThat(feature1.toString(),
                    is(equalTo("ApplicationFeatureId{sort=NAMESPACE, namespace=com.b}")));
        }
    }

    public static class ToStringTest extends ApplicationFeatureIdTest {

        ApplicationFeatureId feature1;
        ApplicationFeatureId feature2;

        @Test
        public void members() throws Exception {
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
        public void classes() throws Exception {
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
        public void packages() throws Exception {
            feature1 = ApplicationFeatureId.newNamespace("com.b");

            feature2 = ApplicationFeatureId.newNamespace("com.c");
            assertThat(feature1.compareTo(feature2), is(lessThan(0)));

            feature2 = ApplicationFeatureId.newNamespace("com.b");
            assertThat(feature1.compareTo(feature2), is(equalTo(0)));

            feature2 = ApplicationFeatureId.newNamespace("com.a");
            assertThat(feature1.compareTo(feature2), is(greaterThan(0)));
        }
    }


    public static class FunctionsTest extends ApplicationFeatureIdTest {

        public static class GET_CLASS_NAME extends FunctionsTest {

            private Function<ApplicationFeatureId, String> func = ApplicationFeatureId::getTypeSimpleName;

            @Test
            public void whenNull() throws Exception {
                expectedException.expect(NullPointerException.class);
                func.apply(null);
            }

            @Test
            public void whenPackage() throws Exception {
                assertThat(func.apply(ApplicationFeatureId.newNamespace("com.mycompany")), is(nullValue()));
            }

            @Test
            public void whenClass() throws Exception {
                assertThat(func.apply(ApplicationFeatureId.newType("com.mycompany.Bar")), is("Bar"));
            }

            @Test
            public void whenMember() throws Exception {
                assertThat(func.apply(ApplicationFeatureId.newMember("com.mycompany.Bar#foo")), is("Bar"));
            }

        }

        public static class GET_MEMBER_NAME extends FunctionsTest {

            private Function<ApplicationFeatureId, String> func =
                    ApplicationFeatureId::getMemberLogicalName;

            @Test
            public void whenNull() throws Exception {
                expectedException.expect(NullPointerException.class);
                func.apply(null);
            }

            @Test
            public void whenPackage() throws Exception {
                assertThat(func.apply(ApplicationFeatureId.newNamespace("com.mycompany")), is(nullValue()));
            }

            @Test
            public void whenClass() throws Exception {
                assertThat(func.apply(ApplicationFeatureId.newType("com.mycompany.Bar")), is(nullValue()));
            }

            @Test
            public void whenMember() throws Exception {
                assertThat(func.apply(ApplicationFeatureId.newMember("com.mycompany.Bar#foo")), is("foo"));
            }

        }

    }

}