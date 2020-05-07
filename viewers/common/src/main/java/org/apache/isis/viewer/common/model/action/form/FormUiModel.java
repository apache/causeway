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
package org.apache.isis.viewer.common.model.action.form;

import java.util.stream.Stream;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.specloader.specimpl.PendingParameterModelHead;
import org.apache.isis.viewer.common.model.HasTitle;
import org.apache.isis.viewer.common.model.feature.ParameterUiModel;

import lombok.val;

public interface FormUiModel extends HasTitle {

    ObjectAction getMetaModel();
    
    /** action's owner
     * @apiNote for mixins this is not the target to use on mixin actions
     * instead the logic of resolving the target for action invocation is 
     * encapsulated within the {@link PendingParameterModelHead}
     */
    ManagedObject getOwner();
    
    Stream<FormPendingParamUiModel> streamPendingParamUiModels();

    // -- USABILITY
    
    default Consent getUsabilityConsent() {
        return getMetaModel().isUsable(
                getOwner(),
                InteractionInitiatedBy.USER,
                Where.OBJECT_FORMS);
    }

    // -- VISABILITY
    
    default Consent getVisibilityConsent() {
        return getMetaModel().isVisible(
                getOwner(),
                InteractionInitiatedBy.USER,
                Where.OBJECT_FORMS);
    }

    // -- VALIDITY
    
    default Consent getValidityConsent() {
        
        val proposedArguments = streamPendingParamUiModels()
                .map(FormPendingParamUiModel::getParamModel)
                .map(ParameterUiModel::getValue)
                .collect(Can.toCan());
                
        return getMetaModel().isProposedArgumentSetValid(
                getOwner(), 
                proposedArguments, 
                InteractionInitiatedBy.USER);
        
    }
    
    // -- HAS TITLE
    
    default String getTitle() {
        val target = getOwner();
        val objectAction = getMetaModel();

        val buf = new StringBuilder();
        
        streamPendingParamUiModels()
        .filter(argAndConsent->argAndConsent.getVisibilityConsent().isAllowed())
        .map(FormPendingParamUiModel::getParamModel)
        .map(ParameterUiModel::getValue)
        .forEach(paramValue->{
            if(buf.length() > 0) {
                buf.append(",");
            }
            buf.append(ManagedObjects.abbreviatedTitleOf(paramValue, 8, "..."));
        });
        return target.titleString(null) + "." + objectAction.getName() + (buf.length()>0?"(" + buf.toString() + ")":"");
    }
    
    // -- SHORTCUTS
    
    default boolean hasParameters() {
        return getMetaModel().getParameterCount() > 0;
    }
    
}
