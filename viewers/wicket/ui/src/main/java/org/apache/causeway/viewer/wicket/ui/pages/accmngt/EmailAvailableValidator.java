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
package org.apache.causeway.viewer.wicket.ui.pages.accmngt;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.ValidationError;

import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.userreg.UserRegistrationService;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.viewer.wicket.ui.validation.ValidatorBase;

import lombok.val;

/**
 * Validates that an email is or is not already in use by another user
 */
public class EmailAvailableValidator extends ValidatorBase<String> {

    private static final long serialVersionUID = 1L;

    public static EmailAvailableValidator exists(MetaModelContext commonContext) {
        return new EmailAvailableValidator(commonContext, true, "noSuchUserByEmail");
    }

    public static EmailAvailableValidator doesntExist(MetaModelContext commonContext) {
        return new EmailAvailableValidator(commonContext, false, "emailIsNotAvailable");
    }

    private final boolean emailExists;
    private final String resourceKey;

    private EmailAvailableValidator(
            MetaModelContext commonContext,
            boolean emailExists,
            String resourceKey) {

        super(commonContext);
        this.emailExists = emailExists;
        this.resourceKey = resourceKey;
    }

    @Override
    public void validate(final IValidatable<String> validatable) {

        val userRegistrationService = super.getMetaModelContext()
                .lookupServiceElseFail(UserRegistrationService.class);

        val interactionService = super.getMetaModelContext()
                .lookupServiceElseFail(InteractionService.class);

        interactionService.runAnonymous(() -> {
            String email = validatable.getValue();
            boolean emailExists1 = userRegistrationService.emailExists(email);
            if (emailExists1 != emailExists) {
                validatable.error(new ValidationError().addKey(resourceKey));
            }
        });

    }

}
