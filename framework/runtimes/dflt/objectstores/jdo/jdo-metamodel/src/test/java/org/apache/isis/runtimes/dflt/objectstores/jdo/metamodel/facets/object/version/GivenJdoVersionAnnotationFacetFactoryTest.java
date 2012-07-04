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
package org.apache.isis.runtimes.dflt.objectstores.jdo.metamodel.facets.object.version;

import java.lang.reflect.Method;
import java.util.List;

import junit.framework.Assert;

import org.jmock.Expectations;

import org.apache.isis.core.commons.matchers.IsisMatchers;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.mandatory.MandatoryFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.progmodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.progmodel.facets.members.disable.DisabledFacet;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2.Mode;

public class GivenJdoVersionAnnotationFacetFactoryTest extends AbstractFacetFactoryTest {

    private JdoVersionAnnotationFacetFactory facetFactory;

    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    private ObjectSpecification objectSpec;
    private OneToOneAssociation versionOtoa;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        objectSpec = context.mock(ObjectSpecification.class);
        versionOtoa = context.mock(OneToOneAssociation.class);
        
        context.checking(new Expectations() {
            {
                allowing(objectSpec).getAssociation("versionColumn");
                will(returnValue(versionOtoa));

                allowing(objectSpec).getAssociation("nonExistentColumn");
                will(returnValue(null));

                allowing(versionOtoa).getId();
                will(returnValue("versionColumn"));
            }
        });
        facetFactory = new JdoVersionAnnotationFacetFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    public void testFeatureTypes() {
        final List<FeatureType> featureTypes = facetFactory.getFeatureTypes();
        assertTrue(contains(featureTypes, FeatureType.OBJECT));
        assertFalse(contains(featureTypes, FeatureType.PROPERTY));
        assertFalse(contains(featureTypes, FeatureType.COLLECTION));
        assertFalse(contains(featureTypes, FeatureType.ACTION));
        assertFalse(contains(featureTypes, FeatureType.ACTION_PARAMETER));
    }

    public void testVersionFacetAndRelatedInstalledOnProperty() throws Exception {

        context.checking(new Expectations() {
            {
                one(versionOtoa).addFacet(with(IsisMatchers.anInstanceOf(JdoVersionFacetAnnotation.class)));
                one(versionOtoa).addFacet(with(IsisMatchers.anInstanceOf(DisabledFacetDerivedFromJdoVersionAnnotation.class)));
                one(versionOtoa).addFacet(with(IsisMatchers.anInstanceOf(OptionalFacetDerivedFromJdoVersionAnnotation.class)));
            }
        });

        final Class<?> cls = SimpleObjectWithVersionAnnotation.class;
        facetFactory.process(new FacetFactory.ProcessClassContext(cls, methodRemover, objectSpec));

        assertNoMethodsRemoved();
    }

    public void testIfInvalidVersionAnnotationThenThrowsException() throws Exception {

        final Class<?> cls = SimpleObjectWithInvalidVersionAnnotation.class;
        try {
            facetFactory.process(new FacetFactory.ProcessClassContext(cls, methodRemover, objectSpec));
            fail();
        } catch(RuntimeException ex) {
            // expected
        }
    }

    public void testIfNoVersionAnnotationThenNoVersionFacet() throws Exception {

        final Class<?> cls = SimpleObjectWithNoVersionAnnotation.class;
        facetFactory.process(new FacetFactory.ProcessClassContext(cls, methodRemover, objectSpec));

        assertNull(facetedMethod.getFacet(JdoVersionFacet.class));
    }

    public void testIfNoTransientAnnotationThenNoDisabledFacet() throws Exception {

        final Class<?> cls = SimpleObjectWithNoVersionAnnotation.class;
        facetFactory.process(new FacetFactory.ProcessClassContext(cls, methodRemover, objectSpec));

        assertNull(facetedMethod.getFacet(DisabledFacet.class));
    }

}
