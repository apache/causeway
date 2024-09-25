package org.apache.causeway.persistence.querydsl.applib;

import java.util.List;
import java.util.function.Function;

import javax.inject.Inject;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;

import org.apache.causeway.core.metamodel.facets.object.autocomplete.AutoCompleteFacet;

import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

import org.springframework.stereotype.Service;


import lombok.NoArgsConstructor;

@Service
@NoArgsConstructor
public class AutoCompleteGeneratedQueryService{

    @Inject protected SpecificationLoader specificationLoader;

    /**
     * Convenience method for programmatically delegate to the generated facet query
     * @param cls for which the auto generated query should be called
     * @param searchPhrase wildcard will ALWAYS be added when absent
     * @return
     * @param <T>
     */
    public <T> List<T> autoComplete(Class<T> cls, String searchPhrase){
        // Call generated autoComplete
        AutoCompleteGeneratedQueryFacet facet = getFacet(cls);
        if(facet!=null){
            return facet.autoComplete(searchPhrase);
        }
        return QueryDslUtil.newList();
    }

    /**
     * Convenience method for programmatically delegate to the generated facet query
     * @param cls for which the auto generated query should be called
     * @param searchPhrase wildcard will ALWAYS be added when absent
     * @param additionalExpression
     * @return
     * @param <T>
     */
    public <T> List<T> autoComplete(Class<T> cls, String searchPhrase, Function<PathBuilder<T>, Predicate> additionalExpression){
        // Call generated autoComplete
        AutoCompleteGeneratedQueryFacet facet = getFacet(cls);
        if(facet!=null){
            return facet.autoComplete(searchPhrase, additionalExpression);
        }
        return QueryDslUtil.newList();
    }

    /**
     * Convenience method for programmatically delegate to the generated facet query
     * @param cls for which the auto generated query should be called
     * @param searchPhrase wildcard will NEVER be added when absent
     * @return
     * @param <T>
     */
    public <T> List<T> executeQuery(Class<T> cls, String searchPhrase){
        // Call generated autoComplete
        AutoCompleteGeneratedQueryFacet facet = getFacet(cls);
        if(facet!=null){
            return facet.executeQuery(searchPhrase);
        }
        return QueryDslUtil.newList();
    }

    /**
     * Convenience method for programmatically delegate to the generated facet query
     * @param cls for which the auto generated query should be called
     * @param searchPhrase wildcard will NEVER be added when absent
     * @param additionalExpression
     * @return
     * @param <T>
     */
    public <T> List<T> executeQuery(Class<T> cls, String searchPhrase, Function<PathBuilder<T>, Predicate> additionalExpression){
        // Call generated autoComplete
        AutoCompleteGeneratedQueryFacet facet = getFacet(cls);
        if(facet!=null){
            return facet.executeQuery(searchPhrase, additionalExpression);
        }
        return QueryDslUtil.newList();
    }

    private <T> AutoCompleteGeneratedQueryFacet getFacet(Class<T> cls) {
        AutoCompleteFacet facet = specificationLoader.loadSpecification(cls)
                .getFacet(AutoCompleteFacet.class);
        if(facet instanceof AutoCompleteGeneratedQueryFacet){
            return (AutoCompleteGeneratedQueryFacet)facet;
        }
        return null;
    }
}
