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

package org.apache.isis.core.progmodel.facets.object.membergroups;

import java.util.Arrays;
import java.util.List;

import org.apache.isis.applib.annotation.MemberGroups;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.isis.core.metamodel.facets.object.membergroups.MemberGroupsFacet;
import org.apache.isis.core.progmodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.progmodel.facets.object.membergroups.annotation.MemberGroupsAnnotationElseFallbackFacetFactory;
import org.apache.isis.core.progmodel.facets.object.membergroups.annotation.MemberGroupsFacetAnnotation;
import org.apache.isis.core.progmodel.facets.object.membergroups.annotation.MemberGroupsFacetFallback;

public class MemberGroupsAnnotationElseFallbackFacetFactoryTest extends AbstractFacetFactoryTest {

    private MemberGroupsAnnotationElseFallbackFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new MemberGroupsAnnotationElseFallbackFacetFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    @MemberGroups({"General","Foo", "Bar"})
    public static class ClassWithMemberGroupsAnnotation {
        
    }
    
    @MemberGroups()
    public static class ClassWithMemberGroupsAnnotationButNoGroupsNamed {
    }
    
    public static class ClassWithoutMemberGroupsAnnotation {
    }
    

    public void testWithMemberGroups() {
        facetFactory.process(new ProcessClassContext(ClassWithMemberGroupsAnnotation.class, methodRemover, facetHolder));

        final Facet facet = facetHolder.getFacet(MemberGroupsFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof MemberGroupsFacetAnnotation);
        final MemberGroupsFacetAnnotation memberGroupsFacet = (MemberGroupsFacetAnnotation) facet;
        final List<String> groupNames = memberGroupsFacet.value();
        assertEquals(Arrays.asList("General", "Foo", "Bar"), groupNames);

        assertNoMethodsRemoved();
    }

    public void testWithMemberGroupsButNoGroupsNamed() {
        facetFactory.process(new ProcessClassContext(ClassWithMemberGroupsAnnotationButNoGroupsNamed.class, methodRemover, facetHolder));

        final Facet facet = facetHolder.getFacet(MemberGroupsFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof MemberGroupsFacetAnnotation);
        final MemberGroupsFacetAnnotation memberGroupsFacet = (MemberGroupsFacetAnnotation) facet;
        final List<String> groupNames = memberGroupsFacet.value();
        assertEquals(Arrays.asList("General"), groupNames);

        assertNoMethodsRemoved();
    }

    public void testWithoutMemberGroups() {
        facetFactory.process(new ProcessClassContext(ClassWithoutMemberGroupsAnnotation.class, methodRemover, facetHolder));

        final Facet facet = facetHolder.getFacet(MemberGroupsFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof MemberGroupsFacetFallback);
        final MemberGroupsFacetFallback memberGroupsFacet = (MemberGroupsFacetFallback) facet;
        final List<String> groupNames = memberGroupsFacet.value();
        assertEquals(Arrays.asList("General"), groupNames);

        assertNoMethodsRemoved();
    }

}
