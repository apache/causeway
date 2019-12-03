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

package org.apache.isis.metamodel.facets;

import lombok.val;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.Contributed;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidatorVisiting;
import org.apache.isis.metamodel.specloader.validator.ValidationFailures;

public abstract class MethodPrefixBasedFacetFactoryAbstract
extends FacetFactoryAbstract
implements MethodPrefixBasedFacetFactory {

    private final List<String> prefixes;

    private final OrphanValidation orphanValidation; 

    protected enum OrphanValidation { // remove?
        VALIDATE,
        DONT_VALIDATE
    }

    public MethodPrefixBasedFacetFactoryAbstract(final List<FeatureType> featureTypes, final OrphanValidation orphanValidation, final String... prefixes) {
        super(featureTypes);
        this.orphanValidation = orphanValidation;
        this.prefixes = Collections.unmodifiableList(Arrays.asList(prefixes));
    }
    
    @Override
    public List<String> getPrefixes() {
        return prefixes;
    }

 
    @Override
    public void refineProgrammingModel(ProgrammingModel programmingModel) {

        if(orphanValidation == OrphanValidation.DONT_VALIDATE) {
            return;
        }

        val noParamsOnly = getConfiguration().getReflector().getValidator().isNoParamsOnly();

        programmingModel.addValidator((objectSpec, metaModelValidator) -> {

            val objectActionStream = objectSpec.streamObjectActions(Contributed.EXCLUDED);

            objectActionStream.forEach(objectAction->{
                for (final String prefix : prefixes) {
                    String actionId = objectAction.getId();

                    if (actionId.startsWith(prefix) && prefix.length() < actionId.length()) {

                        val explanation =
                                objectAction.getParameterCount() > 0 &&
                                noParamsOnly &&
                                (Objects.equals(prefix, MethodLiteralConstants.HIDE_PREFIX) ||
                                        Objects.equals(prefix, MethodLiteralConstants.DISABLE_PREFIX))
                                ? " (note that such methods must have no parameters, '"
                                    + "isis.reflector.validator.no-params-only"
                                    + "' config property)"
                                        : "";

                        val message = "%s#%s: has prefix %s, is probably intended as a supporting method for a property, collection or action%s.  If the method is intended to be an action, then rename and use @ActionLayout(named=\"...\") or ignore completely using @Programmatic";
                        metaModelValidator.onFailure(
                                objectSpec,
                                objectSpec.getIdentifier(),
                                message,
                                objectSpec.getIdentifier().getClassName(),
                                actionId,
                                prefix,
                                explanation);
                    }
                }
            });

            return true;

        });
    }

}
