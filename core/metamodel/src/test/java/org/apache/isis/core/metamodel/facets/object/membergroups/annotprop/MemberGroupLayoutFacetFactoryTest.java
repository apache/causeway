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

package org.apache.isis.core.metamodel.facets.object.membergroups.annotprop;

import java.util.Arrays;
import java.util.List;

import org.apache.isis.applib.annotation.MemberGroupLayout;
import org.apache.isis.applib.annotation.MemberGroupLayout.ColumnSpans;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.metamodel.facets.object.membergroups.MemberGroupLayoutFacet;

public class MemberGroupLayoutFacetFactoryTest extends AbstractFacetFactoryTest {

    private MemberGroupLayoutFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new MemberGroupLayoutFacetFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    @MemberGroupLayout(
            left={"General","Foo", "Bar"}, 
            middle={"Baz", "Boz"}, 
            right={"Flip", "Flop"}, 
            columnSpans={2,4,6,0})
    public static class ClassWithMemberGroupLayoutAnnotation {
        
    }

    @MemberGroupLayout(
            left={"General","Foo", "Bar"}, 
            middle={"Baz", "Boz"}, 
            columnSpans={2,4,0,6})
    public static class ClassWithMemberGroupLayoutAndMemberGroupsAnnotation {
        
    }

    public static class ClassWithMemberGroupsAnnotation {
        
    }
    
    public static class ClassWithMemberGroupsAnnotationButNoGroupsNamed {
    }
    
    public static class ClassWithoutMemberGroupsAnnotation {
    }
    

    public void testWithMemberGroupLayout() {
        facetFactory.process(new ProcessClassContext(ClassWithMemberGroupLayoutAnnotation.class, methodRemover, facetHolder));

        final Facet facet = facetHolder.getFacet(MemberGroupLayoutFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof MemberGroupLayoutFacetAnnotation);
        final MemberGroupLayoutFacetAnnotation memberGroupsFacet = (MemberGroupLayoutFacetAnnotation) facet;
        final List<String> leftNames = memberGroupsFacet.getLeft();
        final List<String> middleNames = memberGroupsFacet.getMiddle();
        final List<String> rightNames = memberGroupsFacet.getRight();
        final ColumnSpans columnSpans = memberGroupsFacet.getColumnSpans();
        assertEquals(Arrays.asList("General", "Foo", "Bar"), leftNames);
        assertEquals(Arrays.asList("Baz", "Boz"), middleNames);
        assertEquals(Arrays.asList("Flip", "Flop"), rightNames);
        assertEquals(ColumnSpans.asSpans(2,4,6,0), columnSpans);

        assertNoMethodsRemoved();
    }


    public void testWithoutMemberGroups() {
        facetFactory.process(new ProcessClassContext(ClassWithoutMemberGroupsAnnotation.class, methodRemover, facetHolder));

        final Facet facet = facetHolder.getFacet(MemberGroupLayoutFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof MemberGroupLayoutFacetFallback);
        final MemberGroupLayoutFacetFallback memberGroupsFacet = (MemberGroupLayoutFacetFallback) facet;
        final List<String> groupNames = memberGroupsFacet.getLeft();
        assertEquals(Arrays.asList("General"), groupNames);

        assertNoMethodsRemoved();
    }

}
