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

import javax.inject.Inject;

import org.apache.isis.applib.services.metamodel.BeanSort;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.spec.ActionScope;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelVisitingValidatorAbstract;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailure;

import lombok.NonNull;
import lombok.val;

/**
 * Rationale:
 * having two actions in the UI with the exact same name wouldn't make sense to the end user,
 * hence fail validation on 'overloading'
 *
 * @since 2.0
 * @see <a href="https://issues.apache.org/jira/browse/ISIS-2493">ISIS-2493</a>
 */
public class ActionOverloadingValidator
extends MetaModelVisitingValidatorAbstract {

    @Inject
    public ActionOverloadingValidator(final MetaModelContext mmc) {
        super(mmc);
    }

    @Override
    public void validate(final @NonNull ObjectSpecification spec) {

        if(spec.getBeanSort()!=BeanSort.UNKNOWN
                && !spec.isAbstract()) {

            val overloadedNames = _Sets.<String>newHashSet();

            spec.streamActions(ActionScope.ANY, MixedIn.EXCLUDED, oa->{
                overloadedNames.add(oa.getFeatureIdentifier().getMemberLogicalName());
            })
            .count(); // consumer the stream

            if(!overloadedNames.isEmpty()) {

                //XXX there is a small chance of a false positive with method overriding,
                // when the method signatures are not exactly the same;
                // meaning, when the parameter classes differ

                ValidationFailure.raiseFormatted(
                        spec,
                        "Action method overloading is not allowed, "
                        + "yet %s has action(s) that have a the same member name: %s",
                        spec.getCorrespondingClass().getName(),
                        overloadedNames);
            }

        }
    }

}
