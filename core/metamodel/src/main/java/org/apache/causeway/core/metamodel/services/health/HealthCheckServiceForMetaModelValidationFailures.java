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
package org.apache.causeway.core.metamodel.services.health;

import java.util.Optional;

import javax.annotation.Priority;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.health.Health;
import org.apache.causeway.applib.services.health.HealthCheckService;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.core.metamodel.CausewayModuleCoreMetamodel;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailures;

import lombok.extern.log4j.Log4j2;

/**
 * Implementation of {@link HealthCheckService} that checks if the metamodel has been built, and if so whether
 * there were any {@link ValidationFailures}.
 *
 * @since 2.2 {@index}
 */
@Service
@Named(CausewayModuleCoreMetamodel.NAMESPACE + ".HealthCheckServiceForMetaModelValidationFailures")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@Log4j2
public class HealthCheckServiceForMetaModelValidationFailures implements HealthCheckService {

    @Override
    public Health check() {
        return Try.call(HealthCheckServiceForMetaModelValidationFailures::metaModelValidationFailures)
                .fold(throwableIfFailure ->
                        Health.error(ex(throwableIfFailure)),
                        HealthCheckServiceForMetaModelValidationFailures::healthFor
                );
    }

    static ValidationFailures metaModelValidationFailures() {
        return MetaModelContext.instanceElseFail().getSpecificationLoader().getValidationResult().orElseThrow(() -> new RuntimeException("Not yet initialized"));
    }

    static Health healthFor(final Optional<ValidationFailures> validationFailuresIfSuccessIfAny) {
        return validationFailuresIfSuccessIfAny.isEmpty()
                ? Health.error("Unable to obtain metamodel validation failures")
                : healthFor(validationFailuresIfSuccessIfAny.get());
    }

    static Health healthFor(final ValidationFailures validationFailures) {
        return validationFailures.hasFailures()
                ? Health.error(ex(validationFailures))
                : Health.ok();
    }

    static Exception ex(ValidationFailures validationFailures) {
        var messages = validationFailures.getMessages("%d: %s");
        var joinedMessages = String.join("\n", messages);
        return new RuntimeException(joinedMessages);
    }

    static Exception ex(Throwable x) {
        return x instanceof Exception ? (Exception) x : new RuntimeException(x);
    }
}
