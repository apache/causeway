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
package org.apache.isis.core.metamodel.facets;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.facets.all.deficiencies.DeficiencyFacet;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectSpecificationAbstract;

import lombok.val;

/**
 * 
 * @since 2.0
 *
 */
public class OrphanedSupportingMethodValidator 
extends FacetFactoryAbstract 
implements MetaModelRefiner {

    public OrphanedSupportingMethodValidator() {
        super(ImmutableEnumSet.noneOf(FeatureType.class)); // does not contribute any facets
    }

    @Override
    public void process(ProcessMethodContext processMethodContext) {
        // does not contribute any facets
    }

    @Override
    public void refineProgrammingModel(ProgrammingModel programmingModel) {
        
        if(getConfiguration().getApplib().getAnnotation().getAction().isExplicit()) {
            return; // continue
        }

        programmingModel.addValidatorSkipManagedBeans((spec, validationFailures) -> {
            
            if(!(spec instanceof ObjectSpecificationAbstract)) {
                return true; // continue
            }

            val potentialOrphans = ((ObjectSpecificationAbstract) spec).getPotentialOrphans();
            if(potentialOrphans.isEmpty()) {
                return true; // continue
            }

            // methods known to the meta-model
            val recognizedMethods = spec.streamFacetHolders()
                    .flatMap(FacetHolder::streamFacets)
                    .filter(ImperativeFacet.class::isInstance)
                    .map(ImperativeFacet.class::cast)
                    .map(ImperativeFacet::getMethods)
                    .flatMap(List::stream)
                    .collect(Collectors.toCollection(HashSet::new));

            // methods intended to be included with the meta-model but missing
            val notRecognizedMethods =
                    _Sets.minus(potentialOrphans, recognizedMethods);

            // find reasons why these are not recognized    
            notRecognizedMethods.forEach(notRecognizedMethod->{
                
                val unmetContraints = unmetContraints(spec, notRecognizedMethod);

                val messageFormat = "%s#%s: is assumed to support "
                        + "a property, collection or action. Unmet constraint(s): %s";
                
                DeficiencyFacet.appendTo(
                        spec,
                        String.format(
                                messageFormat,
                                spec.getIdentifier().getClassName(),
                                notRecognizedMethod.getName(),
                                unmetContraints.stream()
                                .collect(Collectors.joining("; "))));
            });

            potentialOrphans.clear(); // no longer needed  
            
            return true; // continue
        });

    }

    // -- VALIDATION LOGIC

    private List<String> unmetContraints(
            ObjectSpecification spec,
            Method method) {

        val unmetContraints = _Lists.<String>newArrayList();

        unmetContraints.add("misspelled prefix or unsupported method signature or "
                + "orphaned (not associated with an action)");
        return unmetContraints;

    }



}
