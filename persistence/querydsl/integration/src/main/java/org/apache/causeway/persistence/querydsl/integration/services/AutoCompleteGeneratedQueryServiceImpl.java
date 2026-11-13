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
package org.apache.causeway.persistence.querydsl.integration.services;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.apache.causeway.core.metamodel.facets.object.autocomplete.AutoCompleteFacet;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.persistence.querydsl.applib.services.auto.AutoCompleteGeneratedQueryService;
import org.apache.causeway.persistence.querydsl.metamodel.facets.AutoCompleteUsingQueryDslFacet;
import org.springframework.stereotype.Service;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;

import jakarta.inject.Inject;
import lombok.NoArgsConstructor;

@Service
@NoArgsConstructor
public class AutoCompleteGeneratedQueryServiceImpl implements AutoCompleteGeneratedQueryService {

    @Inject protected SpecificationLoader specificationLoader;

    /**
     * Convenience method for programmatically delegate to the generated facet query
     * @param cls for which the auto generated query should be called
     * @param searchPhrase wildcard will ALWAYS be added when absent
     * @param <T>
     */
    @Override
	public <T> List<T> autoComplete(final Class<T> cls, final String searchPhrase){
        // Call generated autoComplete
        AutoCompleteUsingQueryDslFacet facet = getFacet(cls);
        if(facet!=null)
			return facet.autoComplete(searchPhrase);
        return new ArrayList<>();
    }

    /**
     * Convenience method for programmatically delegate to the generated facet query
     * @param cls for which the auto generated query should be called
     * @param searchPhrase wildcard will ALWAYS be added when absent
     * @param additionalExpression
     * @param <T>
     */
    @Override
	public <T> List<T> autoComplete(final Class<T> cls, final String searchPhrase, final Function<PathBuilder<T>, Predicate> additionalExpression){
        // Call generated autoComplete
        AutoCompleteUsingQueryDslFacet facet = getFacet(cls);
        if(facet!=null)
			return facet.autoComplete(searchPhrase, additionalExpression);
        return new ArrayList<>();
    }

    /**
     * Convenience method for programmatically delegate to the generated facet query
     * @param cls for which the auto generated query should be called
     * @param searchPhrase wildcard will NEVER be added when absent
     * @param <T>
     */
    @Override
	public <T> List<T> executeQuery(final Class<T> cls, final String searchPhrase){
        // Call generated autoComplete
        AutoCompleteUsingQueryDslFacet facet = getFacet(cls);
        if(facet!=null)
			return facet.executeQuery(searchPhrase);
        return new ArrayList<>();
    }

    /**
     * Convenience method for programmatically delegate to the generated facet query
     * @param cls for which the auto generated query should be called
     * @param searchPhrase wildcard will NEVER be added when absent
     * @param additionalExpression
     * @param <T>
     */
    @Override
	public <T> List<T> executeQuery(final Class<T> cls, final String searchPhrase, final Function<PathBuilder<T>, Predicate> additionalExpression){
        // Call generated autoComplete
        AutoCompleteUsingQueryDslFacet facet = getFacet(cls);
        if(facet!=null)
			return facet.executeQuery(searchPhrase, additionalExpression);
        return new ArrayList<>();
    }

    private <T> AutoCompleteUsingQueryDslFacet getFacet(final Class<T> cls) {
        AutoCompleteFacet facet = specificationLoader.loadSpecification(cls).lookupFacet(AutoCompleteFacet.class).orElse(null);
        if(facet instanceof AutoCompleteUsingQueryDslFacet)
			return (AutoCompleteUsingQueryDslFacet)facet;
        return null;
    }

}
