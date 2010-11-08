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


package org.apache.isis.metamodel.specloader;

import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.isis.core.metamodel.adapter.ObjectList;
import org.apache.isis.core.metamodel.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetdecorator.FacetDecorator;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.ObjectReflectorAbstract;
import org.apache.isis.core.metamodel.specloader.classsubstitutor.ClassSubstitutor;
import org.apache.isis.core.metamodel.specloader.collectiontyperegistry.CollectionTypeRegistry;
import org.apache.isis.core.metamodel.specloader.progmodelfacets.ProgrammingModelFacets;
import org.apache.isis.core.metamodel.specloader.traverser.SpecificationTraverser;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.metamodel.spec.JavaSpecification;
import org.apache.isis.metamodel.specloader.internal.instances.InstanceCollectionSpecification;


public class JavaReflector extends ObjectReflectorAbstract {

    @SuppressWarnings("unused")
    private final static Logger LOG = Logger.getLogger(JavaReflector.class);

    // /////////////////////////////////////////////////////////////
    // constructor
    // /////////////////////////////////////////////////////////////

    public JavaReflector(
            final IsisConfiguration configuration,
            final ClassSubstitutor classSubstitutor,
            final CollectionTypeRegistry collectionTypeRegistry,
            final SpecificationTraverser specificationTraverser,
            final ProgrammingModelFacets programmingModelFacets, 
            final Set<FacetDecorator> facetDecorators, 
            final MetaModelValidator metaModelValidator) {
        super(configuration, classSubstitutor, collectionTypeRegistry, specificationTraverser, programmingModelFacets, facetDecorators, metaModelValidator);
    }

	/**
	 * Overridable method for language-specific subclass to create the
	 * appropriate type of {@link ObjectSpecification}.
	 */
	protected ObjectSpecification createSpecification(final Class<?> cls) {

		if (ObjectList.class.isAssignableFrom(cls)) {
			return new InstanceCollectionSpecification(this,
					getRuntimeContext());
		}

		return new JavaSpecification(cls, this, getRuntimeContext());
	}


}
