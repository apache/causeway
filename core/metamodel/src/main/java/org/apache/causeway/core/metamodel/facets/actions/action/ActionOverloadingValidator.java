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
package org.apache.causeway.core.metamodel.facets.actions.action;

import javax.inject.Inject;

import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.commons.internal.base._Blackhole;
import org.apache.causeway.commons.internal.collections._Sets;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.spec.ActionScope;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.specloader.validator.MetaModelVisitingValidatorAbstract;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailure;

import lombok.NonNull;
import lombok.val;

/**
 * Rationale:
 * having two actions in the UI with the exact same name wouldn't make sense to the end user,
 * hence fail validation on 'overloading'
 *
 * @since 2.0
 * @see <a href="https://issues.apache.org/jira/browse/CAUSEWAY-2493">CAUSEWAY-2493</a>
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

            _Blackhole.consume( // not strictly required, just to mark this as call with side-effects
                spec.streamActions(ActionScope.ANY, MixedIn.EXCLUDED, oa->{
                    overloadedNames.add(oa.getFeatureIdentifier().getMemberLogicalName());
                })
                .count() // consumes the stream
            );

            if(!overloadedNames.isEmpty()) {

                ValidationFailure.raiseFormatted(
                        spec,
                        ProgrammingModelConstants.Violation.ACTION_METHOD_OVERLOADING_NOT_ALLOWED
                            .builder()
                            .addVariable("type", spec.getCorrespondingClass().getName())
                            .addVariable("overloadedNames", overloadedNames.toString())
                            .buildMessage());
            }

        }
    }

}
