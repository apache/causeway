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
package org.apache.causeway.persistence.querydsl.metamodel.facets;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.object.autocomplete.AutoCompleteFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmVisibilityUtils;
import org.apache.causeway.core.metamodel.object.PackedManagedObject;
import org.apache.causeway.persistence.querydsl.applib.services.support.QueryDslSupport;

import org.springframework.lang.Nullable;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;

public class AutoCompleteUsingQueryDslFacet extends FacetAbstract implements AutoCompleteFacet {

    private final Class<?> entity;

    private final Optional<Object> additionalPredicateRepositoryIfAny;
    private final Optional<Method> autoCompletePredicateMethodIfAny;
    private final Optional<Integer> limitResultsIfAny;
    private final Optional<Integer> minLengthIfAny;

    private final List<AutoCompleteGeneratedDslQuery.SearchableProperty> searchableProperties = new ArrayList<>();

    private final QueryDslSupport queryDslSupport;
    private final Optional<AutoCompleteFacet> previousAutoCompleteFacetIfAny;

    /**
     * Remains empty until properties are added using {@link #addSearchableProperty(String, Property.QueryDslAutoCompletePolicy)}.
     */
    private Optional<AutoCompleteGeneratedDslQuery> autoCompleteGeneratedDslQueryIfAny;

    public AutoCompleteUsingQueryDslFacet(
            final Class<?> entity,
            final FacetHolder facetHolder,
            @Nullable final Object additionalPredicateRepository,
            @Nullable final Method additionalPredicateMethod,
            @Nullable final Integer limit,
            @Nullable final Integer minLength,
            final QueryDslSupport queryDslSupport
    ) {
        super(AutoCompleteFacet.class, facetHolder);

        this.entity = entity;
        this.additionalPredicateRepositoryIfAny = Optional.ofNullable(additionalPredicateRepository);
        this.autoCompletePredicateMethodIfAny = Optional.ofNullable(additionalPredicateMethod);
        this.limitResultsIfAny = Optional.ofNullable(limit);
        this.minLengthIfAny = Optional.ofNullable(minLength);
        this.queryDslSupport = queryDslSupport;

        // in case no searchable properties are provided, we keep track of any previous implementation, to delegate to if required
        this.previousAutoCompleteFacetIfAny = Optional.ofNullable(facetHolder.getFacet(AutoCompleteFacet.class));

        // this remains empty until at least one searchableProperty is added.
        autoCompleteGeneratedDslQueryIfAny = Optional.empty();
    }

    @Override
    public Can<ManagedObject> execute(
            final String search,
            final InteractionInitiatedBy interactionInitiatedBy
    ){

        if (autoCompleteGeneratedDslQueryIfAny.isEmpty()) {
            return previousAutoCompleteFacetIfAny
                    .map(previousFacet -> previousFacet.execute(search, interactionInitiatedBy))
                    .orElse(Can.empty());
        }

        List<?> results = autoComplete(search);
        if(!results.isEmpty()){
            // Transform results to list managed objects, then filter out any that are not visible (eg due to ApplicationTenancyEvaluator)
            return ((PackedManagedObject) getObjectManager().adapt(results)).unpack()
                    .filter(MmVisibilityUtils.filterOn(interactionInitiatedBy));
        }
        return Can.empty();
    }

    public <T> List<T> autoComplete(
            final String search
    ) {
        return autoComplete(search,null);
    }

    public <T> List<T> autoComplete(
            final String search,
            final Function<PathBuilder<T>, Predicate> additionalExpression
    ){
        return autoCompleteGeneratedDslQueryIfAny
                .map(query -> query.autoComplete(search, additionalExpression))
                .orElse(Collections.emptyList());
    }

    public <T> List<T> executeQuery(
            final String search
    ) {
        return executeQuery(search,null);
    }

    public <T> List<T> executeQuery(
            final String search,
            final Function<PathBuilder<T>, Predicate> additionalExpression
    ){
        return autoCompleteGeneratedDslQueryIfAny
                .map(query -> query.executeQuery(search, additionalExpression))
                .orElse(Collections.emptyList());
    }

    @Override
    public int getMinLength() {
        return minLengthIfAny.orElse(DomainObject.QueryDslAutoCompleteConstants.MIN_LENGTH);
    }

    public int getLimitResults() {
        return limitResultsIfAny.orElse(DomainObject.QueryDslAutoCompleteConstants.LIMIT_RESULTS);
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("entity", entity.getName());
        visitor.accept("searchableProperties", searchableProperties
                .stream()
                .map(AutoCompleteGeneratedDslQuery.SearchableProperty::toString)
                .collect(Collectors.joining(",")));
        visitor.accept("minLength", getMinLength());
        visitor.accept("limitResults", getLimitResults());
    }

    public void addSearchableProperty(
            final String propertyId,
            final Property.QueryDslAutoCompletePolicy queryDslAutoCompletePolicy
    ) {
        searchableProperties.add(new AutoCompleteGeneratedDslQuery.SearchableProperty(propertyId, queryDslAutoCompletePolicy));

        // replace the dsl query
        this.autoCompleteGeneratedDslQueryIfAny =
                Optional.of(
                    AutoCompleteGeneratedDslQuery
                    .builder()
                    .queryDslSupport(queryDslSupport)
                    .entity(entity)
                    .searchableProperties(searchableProperties)
                    .repository(additionalPredicateRepositoryIfAny.orElse(null))
                    .predicateMethod(autoCompletePredicateMethodIfAny.orElse(null))
                    .limitResults(limitResultsIfAny.orElse(DomainObject.QueryDslAutoCompleteConstants.LIMIT_RESULTS))
                    .minLength(minLengthIfAny.orElse(DomainObject.QueryDslAutoCompleteConstants.MIN_LENGTH))
                    .build()
                );
    }
}
