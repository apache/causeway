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

package org.apache.isis.core.metamodel.specloader;

import java.util.Collections;
import java.util.HashSet;
import com.google.common.collect.Lists;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetdecorator.FacetDecorator;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.facets.all.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.object.plural.PluralFacet;
import org.apache.isis.core.metamodel.layoutmetadata.LayoutMetadataReader;
import org.apache.isis.core.metamodel.layoutmetadata.json.LayoutMetadataReaderFromJson;
import org.apache.isis.core.metamodel.metamodelvalidator.dflt.MetaModelValidatorDefault;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.runtimecontext.noruntime.RuntimeContextNoRuntime;
import org.apache.isis.core.metamodel.services.ServicesInjectorDefault;
import org.apache.isis.core.metamodel.services.container.DomainObjectContainerDefault;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.unittestsupport.jmocking.InjectIntoJMockAction;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;
import org.apache.isis.progmodels.dflt.ProgrammingModelFacetsJava5;

public abstract class ObjectReflectorDefaultTestAbstract {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_ONLY);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private RuntimeContext runtimeContext;

    @Mock
    private IsisConfiguration mockConfiguration;
    
    // is loaded by subclasses
    protected ObjectSpecification specification;

    
    @Before
    public void setUp() throws Exception {

        context.checking(new Expectations() {
            {
                allowing(mockConfiguration).injectInto(with(anything()));
                will(InjectIntoJMockAction.injectInto());
                
                ignoring(mockConfiguration);
            }
        });

        runtimeContext = new RuntimeContextNoRuntime();
        final ObjectReflectorDefault reflector = 
                new ObjectReflectorDefault(
                        mockConfiguration,
                        new ProgrammingModelFacetsJava5(),
                        new HashSet<FacetDecorator>(),
                        new MetaModelValidatorDefault(),
                        Lists.<LayoutMetadataReader>newArrayList(new LayoutMetadataReaderFromJson()));
        reflector.setRuntimeContext(runtimeContext);
        reflector.setServiceInjector(new ServicesInjectorDefault().withServices(Collections.<Object>singletonList(new DomainObjectContainerDefault())));
        reflector.init();
        
        specification = loadSpecification(reflector);
    }

    protected abstract ObjectSpecification loadSpecification(ObjectReflectorDefault reflector);

    @Test
    public void testLayoutMetadataReaderEmptyList() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("illegal argument, expected: is not an empty collection");

        new ObjectReflectorDefault(
            mockConfiguration,
            new ProgrammingModelFacetsJava5(),
            new HashSet<FacetDecorator>(),
            new MetaModelValidatorDefault(),
            Lists.<LayoutMetadataReader>newArrayList()
        );
    }

    @Test
    public void testLayoutMetadataReaderNull() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("illegal argument, expected: is not null");

        new ObjectReflectorDefault(
            mockConfiguration,
            new ProgrammingModelFacetsJava5(),
            new HashSet<FacetDecorator>(),
            new MetaModelValidatorDefault(),
            null
        );
    }

    @Test
    public void testCollectionFacet() throws Exception {
        final Facet facet = specification.getFacet(CollectionFacet.class);
        Assert.assertNull(facet);
    }

    @Test
    public void testTypeOfFacet() throws Exception {
        final TypeOfFacet facet = specification.getFacet(TypeOfFacet.class);
        Assert.assertNull(facet);
    }

    @Test
    public void testNamedFaced() throws Exception {
        final Facet facet = specification.getFacet(NamedFacet.class);
        Assert.assertNotNull(facet);
    }

    @Test
    public void testPluralFaced() throws Exception {
        final Facet facet = specification.getFacet(PluralFacet.class);
        Assert.assertNotNull(facet);
    }

    @Test
    public void testDescriptionFacet() throws Exception {
        final Facet facet = specification.getFacet(DescribedAsFacet.class);
        Assert.assertNotNull(facet);
    }

}
