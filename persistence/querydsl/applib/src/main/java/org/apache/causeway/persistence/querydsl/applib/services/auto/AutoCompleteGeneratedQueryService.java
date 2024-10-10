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
 *
 */
package org.apache.causeway.persistence.querydsl.applib.services.auto;

import java.util.List;
import java.util.function.Function;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Property;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;

/**
 * Programmatic access to the autocomplete functionality defined declaratively by
 * {@link Property#queryDslAutoComplete()} and can be fine-tuned using the <code>queryDslAutoCompleteXxx...()</code>
 * attributes in {@link DomainObject}.
 *
 * @since 2.1 {@index}
 *
 * @see Property#queryDslAutoComplete()
 * @see DomainObject#queryDslAutoCompleteMinLength()
 * @see DomainObject#queryDslAutoCompleteLimitResults()
 * @see DomainObject#queryDslAutoCompleteAdditionalPredicateRepository()
 * @see DomainObject#queryDslAutoCompleteAdditionalPredicateMethod()
 */
public interface AutoCompleteGeneratedQueryService {

    /**
     * Convenience method for programmatically delegate to the generated facet query
     * @param cls for which the auto generated query should be called
     * @param searchPhrase wildcard will ALWAYS be added when absent
     * @param <T>
     */
    <T> List<T> autoComplete(
            final Class<T> cls,
            final String searchPhrase);

    /**
     * Convenience method for programmatically delegate to the generated facet query
     * @param cls for which the auto generated query should be called
     * @param searchPhrase wildcard will ALWAYS be added when absent
     * @param additionalExpression
     * @param <T>
     */
    <T> List<T> autoComplete(
            final Class<T> cls,
            final String searchPhrase,
            final Function<PathBuilder<T>, Predicate> additionalExpression);

    /**
     * Convenience method for programmatically delegate to the generated facet query
     * @param cls for which the auto generated query should be called
     * @param searchPhrase wildcard will NEVER be added when absent
     * @param <T>
     */
    <T> List<T> executeQuery(
            final Class<T> cls,
            final String searchPhrase);

    /**
     * Convenience method for programmatically delegate to the generated facet query
     * @param cls for which the auto generated query should be called
     * @param searchPhrase wildcard will NEVER be added when absent
     * @param additionalExpression
     * @param <T>
     */
    <T> List<T> executeQuery(
            final Class<T> cls,
            final String searchPhrase,
            final Function<PathBuilder<T>, Predicate> additionalExpression);

}
