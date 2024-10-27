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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.exceptions.RecoverableException;
import org.apache.causeway.persistence.querydsl.applib.query.DslQuery;
import org.apache.causeway.persistence.querydsl.applib.services.support.QueryDslSupport;
import org.apache.causeway.persistence.querydsl.applib.util.CaseSensitivity;
import org.apache.causeway.persistence.querydsl.applib.util.DslExpressions;
import org.apache.causeway.persistence.querydsl.applib.util.Wildcards;

/**
 * Dynamically generate an auto complete query on runtime using Query DSL.
 * Auto complete operates on fields of String type ONLY.
 * The autoComplete method ALWAYS applies wildcards when NONE are specified in de given search string, executeQuery does NOT.
 */
@Builder(access = AccessLevel.PUBLIC)
@Getter
public class AutoCompleteGeneratedDslQuery {

    /**
     * Query DSL is used to dynamically generate the query on runtime
     */
    @NonNull final protected QueryDslSupport queryDslSupport;
    /**
     * the entity for which to generate the auto complete query
     */
    @NonNull final protected Class<?> entity;

    @lombok.Value
    static class SearchableProperty {
        @Getter
        String propertyId;
        Property.QueryDslAutoCompletePolicy queryDslAutoCompletePolicy;

        public String toString() {
            return propertyId + " (" + queryDslAutoCompletePolicy.getDescription() + ")";
        }

        public CaseSensitivity getCaseSensitivity() {
            return CaseSensitivity.of(this.queryDslAutoCompletePolicy.isIgnoreCase());
        }
    }
    /**
     * The properties to use in the generated query (using <code>OR</code>).
     */
    @NonNull final protected List<SearchableProperty> searchableProperties;
    /**
     * Add additional criteria that can be added to the autocomplete method in form:
     * <pre>
     * public Function<EntityPathBase<T>, Predicate> autoCompletePredicate(String search)
     * </pre>
     */
    final protected Object repository;
    final protected Method predicateMethod;

    @Builder.Default
    protected Integer minLength = DomainObject.QueryDslAutoCompleteConstants.MIN_LENGTH;

    @Builder.Default
    protected Integer limitResults = DomainObject.QueryDslAutoCompleteConstants.LIMIT_RESULTS;

    /**
     * Dynamically generate an auto complete query on runtime using Query DSL.
     * Auto complete operates on fields of String type ONLY.
     * The autoComplete method ALWAYS applies wildcards when NONE are specified in de given search string.
     */
    public <T> List<T> autoComplete(
            final String searchPhrase,
            final Function<PathBuilder<T>, Predicate> additionalPredicate) {

        Function<PathBuilder<T>, Predicate> predicate = additionalPredicate;

        if (additionalPredicate == null && predicateMethod != null && repository != null) {
            // Add optional additional predicate from repository
            try {
                predicate = (Function<PathBuilder<T>, Predicate>) predicateMethod.invoke(repository, searchPhrase);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        return executeQuery(Wildcards.toAnsiSqlWildcard(searchPhrase), predicate);
    }

    /**
     * Dynamically generate an auto complete query on runtime using Query DSL.
     * Auto complete operates on fields of String type ONLY.
     * The executeQuery method NEVER applies wildcards when NONE are specified in de given search string.
     */
    public <T> List<T> executeQuery(
            final String searchPhrase,
            final Function<PathBuilder<T>, Predicate> additionalPredicate) {
        val dslQueryIfAny = generateQuery(searchPhrase, additionalPredicate);
        return dslQueryIfAny.map(query -> query.fetch()).orElse(newList());
    }

    public <T> Optional<DslQuery> generateQuery(
            final String searchPhrase,
            final Function<PathBuilder<T>, Predicate> additionalPredicate) {

        if (searchableProperties.isEmpty()) {
            // not expected
            throw new RecoverableException("At least one searchable property should be specified");
        }

        if (isNotEmpty(searchPhrase) && searchPhrase.trim().length() >= getMinLength()) {

            // define entity
            PathBuilder<T> entityPath = new PathBuilder(entity, "e");
            BooleanBuilder where = new BooleanBuilder();
            List<OrderSpecifier<String>> orderSpecifiers = newList();

            // Build where and order clause
            searchableProperties.forEach(se -> {

                // Only string type fields are supported
                val propertyPath = entityPath.getString(se.getPropertyId());
                val searchReplaced = Wildcards.toAnsiSqlWildcard(searchPhrase);

                val expr = DslExpressions.search(propertyPath, searchReplaced, se.getCaseSensitivity());
                where.or(expr);

                // Build order by clause
                orderSpecifiers.add(propertyPath.asc());
            });

            // Build query
            val dslQuery = queryDslSupport.selectFrom(entityPath);

            // add additional expression if any
            if(additionalPredicate!=null){
                where.and(additionalPredicate.apply(entityPath));
            }

            dslQuery.where(where);
            dslQuery.orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]));
            dslQuery.limit(limitResults==null ? DomainObject.QueryDslAutoCompleteConstants.LIMIT_RESULTS : limitResults);

            return Optional.of(dslQuery);
        }
        return Optional.empty();
    }

    static <T> List<T> newList(T... objs) {
        return newArrayList(objs);
    }

    static <T> ArrayList<T> newArrayList(T... objs) {
        ArrayList<T> result = new ArrayList();
        Collections.addAll(result, objs);
        return result;
    }

    static boolean isNotEmpty(CharSequence cs) {
        return !isEmpty(cs);
    }

    static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

}
