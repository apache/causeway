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
package org.apache.isis.core.metamodel.facets.actions.action;

import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.services.metamodel.BeanSort;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelVisitingValidatorAbstract;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailure;

import lombok.NonNull;
import lombok.val;

public class ActionAnnotationShouldEnforceConcreteTypeToBeIncludedWithMetamodelValidator
extends MetaModelVisitingValidatorAbstract {

    @Inject
    public ActionAnnotationShouldEnforceConcreteTypeToBeIncludedWithMetamodelValidator(final MetaModelContext mmc) {
        super(mmc);
    }

    @Override
    public void validate(@NonNull final ObjectSpecification spec) {
        if(spec.getBeanSort()==BeanSort.UNKNOWN
                && !spec.isAbstract()) {

            val actions = spec.streamAnyActions(MixedIn.EXCLUDED).collect(Collectors.toList());

            final int numActions = actions.size();
            if (numActions > 0) {

                val actionIds = actions.stream()
                .map(ObjectAction::getFeatureIdentifier)
                .map(Identifier::toString)
                .collect(Collectors.joining(", "));

                ValidationFailure.raiseFormatted(
                        spec,
                        "%s: is a (concrete) but UNKNOWN sort, yet has %d actions: {%s}",
                        spec.getCorrespondingClass().getName(),
                        numActions,
                        actionIds);
            }

        }
    }

}
