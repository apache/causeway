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
package org.apache.causeway.core.runtimeservices.session;

import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.commons.internal.base._Strings;

import static org.apache.causeway.commons.internal.observation.CausewayObservationIntegration.interactionClock;
import static org.apache.causeway.commons.internal.observation.CausewayObservationIntegration.interactionDepth;
import static org.apache.causeway.commons.internal.observation.CausewayObservationIntegration.interactionLanguage;
import static org.apache.causeway.commons.internal.observation.CausewayObservationIntegration.interactionNumberFormat;
import static org.apache.causeway.commons.internal.observation.CausewayObservationIntegration.interactionTimeFormat;
import static org.apache.causeway.commons.internal.observation.CausewayObservationIntegration.interactionTimezone;
import static org.apache.causeway.commons.internal.observation.CausewayObservationIntegration.userImpersonating;
import static org.apache.causeway.commons.internal.observation.CausewayObservationIntegration.userMultiTenancyToken;
import static org.apache.causeway.commons.internal.observation.CausewayObservationIntegration.userName;

import lombok.experimental.UtilityClass;

import io.micrometer.observation.Observation;

@UtilityClass
class _Observation {

    void addTags(final Observation obs, final InteractionContext ic, final int depth) {
        if(depth>0) {
            obs.lowCardinalityKeyValue(interactionDepth(depth));
        }

        obs.highCardinalityKeyValue(interactionClock(ic.getClock().nowAsInstant()));
        obs.lowCardinalityKeyValue(interactionLanguage(ic.getLocale().languageLocale()));
        obs.lowCardinalityKeyValue(interactionNumberFormat(ic.getLocale().numberFormatLocale()));
        obs.lowCardinalityKeyValue(interactionTimeFormat(ic.getLocale().timeFormatLocale()));
        obs.lowCardinalityKeyValue(interactionTimezone(ic.getTimeZone()));

        obs.lowCardinalityKeyValue(userImpersonating(ic.getUser().isImpersonating()));

        _Strings.nonEmpty(ic.getUser().multiTenancyToken())
            .ifPresent(value->obs.highCardinalityKeyValue(userMultiTenancyToken(value)));

        _Strings.nonEmpty(ic.getUser().name())
            .ifPresent(value->obs.highCardinalityKeyValue(userName(value)));


    }

}
