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
package org.apache.causeway.core.runtimeservices.locale;

import java.util.Locale;
import java.util.Optional;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.locale.UserLocale;
import org.apache.causeway.applib.services.i18n.LanguageProvider;
import org.apache.causeway.applib.services.iactn.InteractionProvider;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;

import lombok.RequiredArgsConstructor;

@Service
@Named(LanguageProviderDefault.LOGICAL_TYPE_NAME)
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class LanguageProviderDefault
implements LanguageProvider {

    static final String LOGICAL_TYPE_NAME = CausewayModuleCoreRuntimeServices.NAMESPACE + ".LanguageProviderDefault";

    private final InteractionProvider interactionProvider;

    @Override
    public Optional<Locale> getPreferredLanguage() {
        return interactionProvider.currentInteractionContext()
        .map(InteractionContext::getLocale)
        .map(UserLocale::getLanguageLocale);
    }

}
