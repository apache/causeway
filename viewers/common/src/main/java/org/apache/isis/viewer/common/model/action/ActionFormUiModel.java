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
package org.apache.isis.viewer.common.model.action;

import java.util.stream.Stream;

import org.apache.isis.applib.annotations.Where;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.viewer.common.model.feature.ParameterUiModel;
import org.apache.isis.viewer.common.model.mixin.HasTitle;

import lombok.val;

public interface ActionFormUiModel
extends HasTitle, HasActionInteraction {

    Stream<? extends ParameterUiModel> streamPendingParamUiModels();

    // -- USABILITY

    default Consent getUsabilityConsent() {
        return getAction().isUsable(
                getActionOwner(),
                InteractionInitiatedBy.USER,
                Where.OBJECT_FORMS);
    }

    // -- VISABILITY

    default Consent getVisibilityConsent() {
        return getAction().isVisible(
                getActionOwner(),
                InteractionInitiatedBy.USER,
                Where.OBJECT_FORMS);
    }

    // -- VALIDITY

    default Consent getValidityConsent() {

        val proposedArguments = streamPendingParamUiModels()
                .map(ParameterUiModel::getValue)
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
        .filter(paramModel->paramModel.getParameterNegotiationModel().getVisibilityConsent(paramModel.getParameterIndex()).isAllowed())
        .map(ParameterUiModel::getValue)
        .forEach(paramValue->{
            if(buf.length() > 0) {
                buf.append(",");
            }
            buf.append(ManagedObjects.abbreviatedTitleOf(paramValue, 8, "..."));
        });
        return owner.titleString() + "." + getFriendlyName()
            + (buf.length()>0
                    ?"(" + buf.toString() + ")"
                    :"");
    }


}
