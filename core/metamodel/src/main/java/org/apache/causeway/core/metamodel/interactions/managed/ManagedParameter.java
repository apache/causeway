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
package org.apache.causeway.core.metamodel.interactions.managed;

import java.util.Optional;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.consent.Veto;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;

import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class ManagedParameter
implements
    ManagedValue,
    ManagedFeature {

    public abstract int getParamNr();
    @Override public abstract ObjectActionParameter getMetaModel();
    public abstract ParameterNegotiationModel getNegotiationModel();

    /**
     * @param params
     * @return non-empty if not usable/editable (meaning if read-only)
     */
    public final Optional<InteractionVeto> checkUsability(final @NonNull Can<ManagedObject> params) {

        try {
            val head = getNegotiationModel().getHead();

            val usabilityConsent =
                    getMetaModel()
                    .isUsable(head, params, InteractionInitiatedBy.USER);

            return usabilityConsent.isVetoed()
                    ? Optional.of(InteractionVeto.readonly(usabilityConsent))
                    : Optional.empty();

        } catch (final Exception ex) {

            log.warn(ex.getLocalizedMessage(), ex);
            return Optional.of(InteractionVeto.readonly(new Veto("failure during usability evaluation")));

        }

    }


}
