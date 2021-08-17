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
package org.apache.isis.testing.integtestsupport.applib.validate;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;
import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.exceptions.unrecoverable.DomainModelException;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.environment.IsisSystemEnvironment;
import org.apache.isis.core.config.metamodel.specloader.IntrospectionMode;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailure;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailures;

import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * @since 2.0 {@index}
 */
@Log4j2
public class DomainModelValidator {

    private final ValidationFailures validationFailures;

    @Inject
    public DomainModelValidator(ServiceRegistry registry) {
        this(   registry.lookupServiceElseFail(SpecificationLoader.class),
                registry.lookupServiceElseFail(IsisConfiguration.class),
                registry.lookupServiceElseFail(IsisSystemEnvironment.class));
    }

    public DomainModelValidator(
            final SpecificationLoader specificationLoader,
            final IsisConfiguration configuration,
            final IsisSystemEnvironment isisSystemEnvironment) {

        val recreateRequired = isRecreateRequired(configuration, isisSystemEnvironment);
        if(recreateRequired) {
            specificationLoader.createMetaModel();
            if(log.isDebugEnabled()) {
                specificationLoader.forEach(spec->{
                    log.debug("loaded: " + spec.getFullIdentifier());
                });
            }
        }

        this.validationFailures = specificationLoader.getOrAssessValidationResult();
    }


    private static boolean isRecreateRequired(IsisConfiguration configuration, IsisSystemEnvironment isisSystemEnvironment) {
        final IntrospectionMode mode = configuration.getCore().getMetaModel().getIntrospector().getMode();
        switch (mode) {
            case FULL:
                return false;
            case LAZY_UNLESS_PRODUCTION:
                return isisSystemEnvironment.isPrototyping();
            case LAZY:
            default:
                return true;
        }
    }

    /**
     * Typical usage in integration tests.
     */
    public void assertValid() {
        if (validationFailures.hasFailures()) {
            final StringJoiner joiner = new StringJoiner("\n");
            validationFailures.getMessages().forEach(joiner::add);
            Assertions.fail(joiner.toString());
        }
    }

    /**
     * Alternative way of checking
     */
    public void throwIfInvalid() {
        if (validationFailures.hasFailures()) {
            throwFailureException(
                    validationFailures.getNumberOfFailures() + " problems found.",
                    validationFailures.getMessages());
        }
    }

    public Set<ValidationFailure> getFailures() {
        if (validationFailures == null) {
            return Collections.emptySet();
        }
        return validationFailures.getFailures(); // already wrapped unmodifiable
    }

    public Stream<ValidationFailure> streamFailures(@Nullable Predicate<Identifier> filter) {
        if(validationFailures==null) {
            return Stream.empty();
        }
        if(filter==null) {
            return validationFailures.getFailures().stream();
        }
        return validationFailures.getFailures().stream()
                .filter(failure->filter.test(failure.getOrigin()));
    }

    public Stream<ValidationFailure> streamFailuresMatchingOriginatingIdentifier(
            final @NonNull Identifier identifier) {
        return streamFailures(id->id.equals(identifier));
    }


    // -- SHORTCUTS

    /**
     * primarily used for testing
     */
    public boolean anyMatchesContaining(
            final @NonNull Identifier identifier,
            final @NonNull String messageSnippet) {
        return streamFailuresMatchingOriginatingIdentifier(identifier)
                .anyMatch(failure->
                    failure.getMessage().contains(messageSnippet));
    }

    // -- HELPER

    private void throwFailureException(String errorMessage, Collection<String> logMessages) {
        logErrors(logMessages);
        throw new DomainModelException(errorMessage);
    }

    private void logErrors(Collection<String> logMessages) {
        log.error("### Domain Model Deficiencies");
        for (String logMessage : logMessages) {
            log.error(logMessage);
        }
    }

}
