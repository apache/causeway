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
package org.apache.causeway.core.security.authentication.manager;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.exceptions.unrecoverable.NoAuthenticatorException;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.user.UserCurrentSessionTimeZoneHolder;
import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.applib.util.ToString;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Timing;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.core.security.CausewayModuleCoreSecurity;
import org.apache.causeway.core.security.authentication.AuthenticationRequest;
import org.apache.causeway.core.security.authentication.Authenticator;
import org.apache.causeway.core.security.authentication.standard.RandomCodeGenerator;
import org.apache.causeway.core.security.authentication.standard.Registrar;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

@Service
@Named(AuthenticationManager.LOGICAL_TYPE_NAME)
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
public class AuthenticationManager {

    public static final String LOGICAL_TYPE_NAME = CausewayModuleCoreSecurity.NAMESPACE + ".AuthenticationManager";

    @Getter private final @NonNull Can<Authenticator> authenticators;

    private final Map<String, String> userByValidationCode = _Maps.newConcurrentHashMap();
    private final @NonNull InteractionService interactionService;
    private final @NonNull RandomCodeGenerator randomCodeGenerator;
    private final @NonNull Can<Registrar> registrars;
    private final @NonNull List<UserMementoRefiner> userMementoRefiners;
    private final @NonNull Optional<UserCurrentSessionTimeZoneHolder> userCurrentSessionTimeZoneHolder;

    @Inject
    public AuthenticationManager(
            final List<Authenticator> authenticators,
            final InteractionService interactionService,
            final RandomCodeGenerator randomCodeGenerator,
            final Optional<UserCurrentSessionTimeZoneHolder> userCurrentSessionTimeZoneHolder,
            final List<UserMementoRefiner> userMementoRefiners) {
        this.interactionService = interactionService;
        this.randomCodeGenerator = randomCodeGenerator;
        this.authenticators = Can.ofCollection(authenticators);
        this.userCurrentSessionTimeZoneHolder = userCurrentSessionTimeZoneHolder;
        this.userMementoRefiners = userMementoRefiners;
        if (this.authenticators.isEmpty()) {
            throw new NoAuthenticatorException("No authenticators specified");
        }
        this.registrars = this.authenticators
                .filter(Registrar.class::isInstance)
                .map(Registrar.class::cast);
    }

    // -- SESSION MANAGEMENT (including authenticate)

    @Transactional(readOnly = true) // let Spring handle the transactional context for this method
    // cannot use final here, as Spring provides a transaction aware proxy for this type
    public /*final*/ InteractionContext authenticate(final AuthenticationRequest request) {

        if (request == null) {
            return null;
        }

        val compatibleAuthenticators = authenticators
                .filter(authenticator->authenticator.canAuthenticate(request.getClass()));

        if (compatibleAuthenticators.isEmpty()) {
            throw new NoAuthenticatorException(
                    "No authenticator available for processing " + request.getClass().getName());
        }

        // open a new anonymous interaction for this loop to run in
        // we simply participate with the current transaction
        return interactionService.callAnonymous(()->{

            for (val authenticator : compatibleAuthenticators) {
                val interactionContext = authenticator.authenticate(request, getUnusedRandomCode());
                if (interactionContext != null) {

                    val interactionContextWithTimeZone = interactionContext
                            .withTimeZoneIfAny(userCurrentSessionTimeZoneHolder
                                    .flatMap(UserCurrentSessionTimeZoneHolder::getUserTimeZone));

                    val userRefined = UserMementoRefiner.refine(
                            interactionContext.getUser(),
                            userMementoRefiners);

                    userByValidationCode.put(
                            userRefined.getAuthenticationCode(),
                            userRefined.getName());

                    val interactionContextRefined = interactionContextWithTimeZone
                            .withUser(userRefined);
                    return interactionContextRefined;
                }
            }

            return null;
        });
    }

    private String getUnusedRandomCode() {

        val stopWatch = _Timing.now();

        String code;
        do {

            // guard against infinite loop when unique code generation for some reason fails
            if(stopWatch.getMillis()>3000L) {
                throw new NoAuthenticatorException(
                        "RandomCodeGenerator failed to produce a unique code within 3s.");
            }

            code = randomCodeGenerator.generateRandomCode();
        } while (userByValidationCode.containsKey(code));

        return code;
    }


    // cannot use final here, as Spring provides a transaction aware proxy for this type
    public /*final*/ boolean isSessionValid(final @Nullable InteractionContext authentication) {
        if(authentication==null) {
            return false;
        }
        val userMemento = authentication.getUser();
        if(userMemento.getAuthenticationSource().isExternal()) {
            return true;
        }
        if(userMemento.isImpersonating()) {
            return true;
        }
        final String userName = userByValidationCode.get(userMemento.getAuthenticationCode());
        return authentication.getUser().isCurrentUser(userName);
    }


    public void closeSession(final @Nullable UserMemento user) {
        for (val authenticator : authenticators) {
            authenticator.logout();
        }
        if(user==null) return;
        userByValidationCode.remove(user.getAuthenticationCode());
    }

    // -- AUTHENTICATORS

    public boolean register(final RegistrationDetails registrationDetails) {
        for (val registrar : this.registrars) {
            if (registrar.canRegister(registrationDetails.getClass())) {
                return registrar.register(registrationDetails);
            }
        }
        return false;
    }


    public boolean supportsRegistration(final Class<? extends RegistrationDetails> registrationDetailsClass) {
        for (val registrar : this.registrars) {
            if (registrar.canRegister(registrationDetailsClass)) {
                return true;
            }
        }
        return false;
    }

    // -- DEBUGGING

    private static final ToString<AuthenticationManager> toString =
            ToString.<AuthenticationManager>toString("class", obj->obj.getClass().getSimpleName())
            .thenToString("authenticators", obj->""+obj.authenticators.size())
            .thenToString("users", obj->""+obj.userByValidationCode.size());

    @Override
    public String toString() {
        return toString.toString(this);
    }

}
