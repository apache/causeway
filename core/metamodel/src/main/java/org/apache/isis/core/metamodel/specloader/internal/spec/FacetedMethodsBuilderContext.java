/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.metamodel.specloader.internal.spec;

import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.classsubstitutor.ClassSubstitutor;
import org.apache.isis.core.metamodel.specloader.internal.facetprocessor.FacetProcessor;
import org.apache.isis.core.metamodel.specloader.traverser.SpecificationTraverser;

public class FacetedMethodsBuilderContext {
    public final SpecificationLoader specificationLoader;
    public final SpecificationTraverser specificationTraverser;
    public final FacetProcessor facetProcessor;
    public final ClassSubstitutor classSubstitutor;

    public FacetedMethodsBuilderContext(SpecificationLoader specificationLoader, ClassSubstitutor classSubstitutor,
        SpecificationTraverser specificationTraverser, FacetProcessor facetProcessor) {
        this.specificationLoader = specificationLoader;
        this.classSubstitutor = classSubstitutor;
        this.specificationTraverser = specificationTraverser;
        this.facetProcessor = facetProcessor;
    }
}