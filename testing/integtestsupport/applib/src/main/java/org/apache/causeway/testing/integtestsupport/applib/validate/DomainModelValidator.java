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
package org.apache.causeway.testing.integtestsupport.applib.validate;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.opentest4j.AssertionFailedError;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.exceptions.unrecoverable.DomainModelException;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.core.config.metamodel.specloader.IntrospectionMode;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailure;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailures;

import org.jspecify.annotations.NonNull;
import lombok.extern.log4j.Log4j2;

/**
 * @since 2.0 {@index}
 */
@Log4j2
public class DomainModelValidator {

    private final ValidationFailures validationFailures;
    private final SpecificationLoader specificationLoader;

    @Inject
    public DomainModelValidator(final ServiceRegistry registry) {
        this(   registry.lookupServiceElseFail(SpecificationLoader.class),
                registry.lookupServiceElseFail(CausewayConfiguration.class),
                registry.lookupServiceElseFail(CausewaySystemEnvironment.class));
    }

    public DomainModelValidator(
            final SpecificationLoader specificationLoader,
            final CausewayConfiguration configuration,
            final CausewaySystemEnvironment causewaySystemEnvironment) {

        var recreateRequired = isRecreateRequired(configuration, causewaySystemEnvironment);
        if(recreateRequired) {
            specificationLoader.createMetaModel();
            if(log.isDebugEnabled()) {
                specificationLoader.forEach(spec->{
                    log.debug("loaded: " + spec.getFullIdentifier());
                });
            }
        }

        this.specificationLoader = specificationLoader;
        this.validationFailures = specificationLoader.getOrAssessValidationResult();
    }

    private static boolean isRecreateRequired(final CausewayConfiguration configuration, final CausewaySystemEnvironment causewaySystemEnvironment) {
        final IntrospectionMode mode = configuration.getCore().getMetaModel().getIntrospector().getMode();
        switch (mode) {
            case FULL:
                return false;
            case LAZY_UNLESS_PRODUCTION:
                return causewaySystemEnvironment.isPrototyping();
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

    public Stream<ValidationFailure> streamFailures(@Nullable final Predicate<Identifier> filter) {
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

    // -- JUNIT SUPPORT

    /**
     * JUnit support
     */
    public void assertAnyFailuresContaining(
            final @NonNull Identifier identifier,
            final @NonNull String messageSnippet) {

        boolean matchFound = streamFailuresMatchingOriginatingIdentifier(identifier)
                .anyMatch(failure->
                    failure.getMessage().contains(messageSnippet));

        if(!matchFound) {
            var msg = String.format("validation snipped '%s' not found within messages:\n%s",
                    messageSnippet,
                    streamFailuresMatchingOriginatingIdentifier(identifier)
                    .map(ValidationFailure::getMessage)
                    .collect(Collectors.joining("\n")));
            throw new AssertionFailedError(msg);
        }
    }

    /**
     * JUnit support
     */
    public void assertAnyFailuresContaining(
            final @NonNull Class<?> domainType,
            final @NonNull String messageSnippet) {

        var logicalType = specificationLoader.specForTypeElseFail(domainType)
            .logicalType();
        final Predicate<Identifier> filterByLogicalType = id->id.logicalType().equals(logicalType);

        boolean matchFound = streamFailures(filterByLogicalType)
                .anyMatch(failure->
                    failure.getMessage().contains(messageSnippet));

        if(!matchFound) {
            var msg = String.format("validation snipped '%s' not found within messages:\n%s",
                    messageSnippet,
                    streamFailures(filterByLogicalType)
                    .map(ValidationFailure::getMessage)
                    .collect(Collectors.joining("\n")));
            throw new AssertionFailedError(msg);
        }
    }

    /**
     * JUnit support
     */
    public void assertAnyOfContainingAnyFailures(
            final Can<Identifier> classIdentifiers,
            final String messageSnippet) {

        boolean matchFound = classIdentifiers
                .stream()
                .anyMatch(identifier->
                        streamFailuresMatchingOriginatingIdentifier(identifier)
                                .anyMatch(failure->
                                failure.getMessage().contains(messageSnippet)));

        if(!matchFound) {
            var msg = String.format("validation snipped '%s' not found within messages:\n%s",
                    messageSnippet,
                    classIdentifiers.stream()
                    .flatMap(identifier->streamFailuresMatchingOriginatingIdentifier(identifier))
                    .map(ValidationFailure::getMessage)
                    .collect(Collectors.joining("\n")));
            throw new AssertionFailedError(msg);
        }

    }

    // -- HELPER

    private void throwFailureException(final String errorMessage, final Collection<String> logMessages) {
        logErrors(logMessages);
        throw new DomainModelException(errorMessage);
    }

    private void logErrors(final Collection<String> logMessages) {
        log.error("### Domain Model Deficiencies");
        for (String logMessage : logMessages) {
            log.error(logMessage);
        }
    }

}
