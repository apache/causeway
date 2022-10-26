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
package org.apache.causeway.viewer.commons.model.action;

import java.util.stream.Stream;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.core.metamodel.consent.Consent;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.consent.Veto;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.object.MmTitleUtil;
import org.apache.causeway.viewer.commons.model.UiModel;
import org.apache.causeway.viewer.commons.model.mixin.HasTitle;
import org.apache.causeway.viewer.commons.model.scalar.UiParameter;

import lombok.val;

public interface UiActionForm
extends
    UiModel,
    HasTitle,
    HasActionInteraction {

    Stream<? extends UiParameter> streamPendingParamUiModels();

    // -- USABILITY

    default Consent getUsabilityConsent() {
        return getAction().isUsable(
                getActionOwner(),
                InteractionInitiatedBy.USER,
                Where.OBJECT_FORMS);
    }

    // -- VISABILITY

    default Consent getVisibilityConsent() {

        // guard against missing action owner
        val actionOwner = getActionOwner();
        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(actionOwner)) {
            return Veto.DEFAULT; // veto, so we don't render the action
        }

        // check whether action owner type is hidden
        if (getActionOwner().getSpecification().isHidden()) {
            return Veto.DEFAULT;
        }

        return getAction().isVisible(
                actionOwner,
                InteractionInitiatedBy.USER,
                Where.OBJECT_FORMS);
    }

    // -- VALIDITY

    default Consent getValidityConsent() {

        val proposedArguments = streamPendingParamUiModels()
                .map(UiParameter::getValue)
                .collect(Can.toCan());

        _Assert.assertEquals(getAction().getParameterCount(), proposedArguments.size());

        val head = getAction().interactionHead(getActionOwner());

        return getAction().isArgumentSetValid(
                head,
                proposedArguments,
                InteractionInitiatedBy.USER);

    }

    // -- HAS TITLE

    @Override
    default String getTitle() {
        val owner = getActionOwner();

        val buf = new StringBuilder();

        streamPendingParamUiModels()
        .filter(paramModel->paramModel.getParameterNegotiationModel()
                .getVisibilityConsent(paramModel.getParameterIndex()).isAllowed())
        .map(UiParameter::getValue)
        .forEach(paramValue->{
            if(buf.length() > 0) {
                buf.append(",");
            }
            buf.append(MmTitleUtil.abbreviatedTitleOf(paramValue, 8, "..."));
        });
        return owner.getTitle() + "." + getFriendlyName()
            + (buf.length()>0
                    ?"(" + buf.toString() + ")"
                    :"");
    }


}
