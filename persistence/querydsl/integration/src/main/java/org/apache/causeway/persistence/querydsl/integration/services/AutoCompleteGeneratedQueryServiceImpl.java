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

import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import javax.inject.Inject;

import org.apache.causeway.core.metamodel.facets.object.autocomplete.AutoCompleteFacet;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

import org.apache.causeway.persistence.querydsl.applib.services.auto.AutoCompleteGeneratedQueryService;

import org.apache.causeway.persistence.querydsl.metamodel.facets.AutoCompleteUsingQueryDslFacet;

import org.springframework.stereotype.Service;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;

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
    public <T> List<T> autoComplete(Class<T> cls, String searchPhrase){
        // Call generated autoComplete
        AutoCompleteUsingQueryDslFacet facet = getFacet(cls);
        if(facet!=null){
            return facet.autoComplete(searchPhrase);
        }
        return newList();
    }

    /**
     * Convenience method for programmatically delegate to the generated facet query
     * @param cls for which the auto generated query should be called
     * @param searchPhrase wildcard will ALWAYS be added when absent
     * @param additionalExpression
     * @param <T>
     */
    public <T> List<T> autoComplete(Class<T> cls, String searchPhrase, Function<PathBuilder<T>, Predicate> additionalExpression){
        // Call generated autoComplete
        AutoCompleteUsingQueryDslFacet facet = getFacet(cls);
        if(facet!=null){
            return facet.autoComplete(searchPhrase, additionalExpression);
        }
        return newList();
    }

    /**
     * Convenience method for programmatically delegate to the generated facet query
     * @param cls for which the auto generated query should be called
     * @param searchPhrase wildcard will NEVER be added when absent
     * @param <T>
     */
    public <T> List<T> executeQuery(Class<T> cls, String searchPhrase){
        // Call generated autoComplete
        AutoCompleteUsingQueryDslFacet facet = getFacet(cls);
        if(facet!=null){
            return facet.executeQuery(searchPhrase);
        }
        return newList();
    }

    /**
     * Convenience method for programmatically delegate to the generated facet query
     * @param cls for which the auto generated query should be called
     * @param searchPhrase wildcard will NEVER be added when absent
     * @param additionalExpression
     * @param <T>
     */
    public <T> List<T> executeQuery(Class<T> cls, String searchPhrase, Function<PathBuilder<T>, Predicate> additionalExpression){
        // Call generated autoComplete
        AutoCompleteUsingQueryDslFacet facet = getFacet(cls);
        if(facet!=null){
            return facet.executeQuery(searchPhrase, additionalExpression);
        }
        return newList();
    }

    private <T> AutoCompleteUsingQueryDslFacet getFacet(Class<T> cls) {
        AutoCompleteFacet facet = specificationLoader.loadSpecification(cls)
                .getFacet(AutoCompleteFacet.class);
        if(facet instanceof AutoCompleteUsingQueryDslFacet){
            return (AutoCompleteUsingQueryDslFacet)facet;
        }
        return null;
    }

    private static <T> List<T> newList(T... objs) {
        return newArrayList(objs);
    }

    static <T> ArrayList<T> newArrayList(T... objs) {
        ArrayList<T> result = new ArrayList();
        Collections.addAll(result, objs);
        return result;
    }

}
