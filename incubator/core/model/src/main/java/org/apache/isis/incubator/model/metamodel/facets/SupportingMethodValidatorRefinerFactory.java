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
package org.apache.isis.incubator.model.metamodel.facets;

import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.incubator.model.applib.annotation.Model;
import org.apache.isis.metamodel.commons.MethodUtil;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.metamodel.facets.ImperativeFacet;
import org.apache.isis.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.metamodel.spec.ObjectSpecification;

import lombok.val;

/**
 * 
 * @since 2.0
 *
 */
public class SupportingMethodValidatorRefinerFactory 
extends FacetFactoryAbstract 
implements MetaModelRefiner {

    public SupportingMethodValidatorRefinerFactory() {
        super(EnumSet.noneOf(FeatureType.class)); // does not contribute any facets
    }

    @Override
    public void process(ProcessMethodContext processMethodContext) {
        // does not contribute any facets
    }

    @Override
    public void refineProgrammingModel(ProgrammingModel programmingModel) {

        programmingModel.addValidator((spec, validationFailures) -> {

            val type = spec.getCorrespondingClass();

//XXX for debugging ...            
            if(spec.getSpecId().asString().contains("ProperMemberSupport")) {
                
                val sb = new StringBuffer();
                
                sb.append("\n### debug " + spec.getSpecId().asString()).append("\n");
                
                spec.streamFacetHolders()
                .flatMap(FacetHolder::streamFacets)
                .forEach(facet->sb.append("facet: " + facet).append("\n"));
                
                System.out.println(sb);
            }

            // methods known to the meta-model
            val recognizedMethods = spec.streamFacetHolders()
                    .flatMap(FacetHolder::streamFacets)
                    .filter(ImperativeFacet.class::isInstance)
                    .map(ImperativeFacet.class::cast)
                    .map(ImperativeFacet::getMethods)
                    .flatMap(List::stream)
                    .collect(Collectors.toCollection(HashSet::new));
            
            // methods intended to be included with the meta-model
            val intendedMethods = _Sets.<Method>newHashSet(); 
            for(val method: type.getDeclaredMethods()) {
                if(method.getDeclaredAnnotation(Model.class)!=null) {
                    intendedMethods.add(method);
                }
            }

            // methods intended to be included with the meta-model but missing
            val notRecognizedMethods =
                    _Sets.minus(intendedMethods, recognizedMethods);

            // find reasons about why these are not recognized    
            notRecognizedMethods.forEach(notRecognizedMethod->{
                val unmetContraints = unmetContraints(spec, notRecognizedMethod);

                val messageFormat = "%s#%s: has annotation @%s, is assumed to support "
                        + "a property, collection or action. Unmet constraint(s): %s";
                validationFailures.onFailure(
                        spec,
                        spec.getIdentifier(),
                        messageFormat,
                        spec.getIdentifier().getClassName(),
                        notRecognizedMethod.getName(),
                        Model.class.getSimpleName(),
                        unmetContraints.stream()
                        .collect(Collectors.joining("; ")));
            });


            return true; // continue
        });

    }

    // -- VALIDATION LOGIC

    private List<String> unmetContraints(
            ObjectSpecification spec, 
            Method method) {

        //val type = spec.getCorrespondingClass();
        val unmetContraints = _Lists.<String>newArrayList();

        if (!MethodUtil.isPublic(method)) {
            unmetContraints.add("method must be 'public'");
            return unmetContraints; // don't check any further
        } 

        unmetContraints.add("misspelled prefix or unsupported method signature");
        return unmetContraints;

    }



}
