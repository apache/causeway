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
package org.apache.isis.integtestsupport.validate;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.isis.applib.Identifier;
import org.apache.isis.metamodel.spec.DomainModelException;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.metamodel.specloader.validator.ValidationFailure;
import org.apache.isis.metamodel.specloader.validator.ValidationFailures;

import static org.apache.isis.commons.internal.base._With.requires;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * 
 * @since 2.0
 *
 */
@Log4j2
@RequiredArgsConstructor
public class ValidateDomainModel implements Runnable {

    private final SpecificationLoader specificationLoader;
    private ValidationFailures validationFailures;

    @Override
    public void run() {

        specificationLoader.createMetaModel();
        
        this.validationFailures = specificationLoader.getValidationResult();
      

        if(log.isDebugEnabled()) {
            specificationLoader.forEach(spec->{
                log.debug("loaded: " + spec.getFullIdentifier());
            });
        }

        if (validationFailures.hasFailures()) {
            throwFailureException(
                    validationFailures.getNumberOfFailures() + " problems found.", 
                    validationFailures.getMessages());
        }
    }
    
    public Set<ValidationFailure> getFailures() {
        if(validationFailures==null) {
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
    
    public Stream<ValidationFailure> streamFailuresMatchingOriginatingClass(Class<?> cls) {
        requires(cls, "cls");
        return streamFailures(origin->origin.getClassName().equals(cls.getName()));
    }
    
    // -- SHORTCUTS
    
    /**
     * primarily used for testing 
     */
    public boolean anyMatchesContaining(Class<?> cls, String messageSnippet) {
        return streamFailuresMatchingOriginatingClass(cls)
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