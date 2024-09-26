package org.apache.causeway.persistence.querydsl.metamodel.facets;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.object.autocomplete.AutoCompleteFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmVisibilityUtils;
import org.apache.causeway.core.metamodel.object.PackedManagedObject;
import org.apache.causeway.persistence.querydsl.applib.services.support.QueryDslSupport;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;

public class AutoCompleteGeneratedQueryFacet extends FacetAbstract implements AutoCompleteFacet {

    final protected AutoCompleteGeneratedDslQuery autoCompleteGeneratedDslQuery;

    public AutoCompleteGeneratedQueryFacet(
            final Class<?> entity,
            final FacetHolder facetHolder,
            final List<Field> fields,
            final Object repository,
            final Method autoCompletePredicateMethod,
            final Integer limit,
            final Integer minLength
    ) {
        super(AutoCompleteFacet.class, facetHolder);
        this.autoCompleteGeneratedDslQuery = AutoCompleteGeneratedDslQuery
                .builder()
                .queryDslSupport(facetHolder.getMetaModelContext().lookupService(QueryDslSupport.class).get())
                .entity(entity)
                .fields(fields)
                .repository(repository)
                .predicateMethod(autoCompletePredicateMethod)
                .limitResults(limit)
                .minLength(minLength)
                .build();
    }

    @Override
    public Can<ManagedObject> execute(String search, InteractionInitiatedBy interactionInitiatedBy){
        List<?> results = autoComplete( search);
        if(!results.isEmpty()){
            // Transform results to list managed objects, then filter out any that are not visible (eg due to ApplicationTenancyEvaluator)
            return ((PackedManagedObject) getObjectManager().adapt(results)).unpack()
                    .filter(MmVisibilityUtils.filterOn(interactionInitiatedBy));
        }
        return Can.empty();
    }

    public <T> List<T> autoComplete(String search) {
        return autoComplete(search,null);
    }
    public <T> List<T> autoComplete(String search, Function<PathBuilder<T>, Predicate> additionalExpression){
        return autoCompleteGeneratedDslQuery.autoComplete(search, additionalExpression);
    }

    public <T> List<T> executeQuery(String search) {
        return executeQuery(search,null);
    }

    public <T> List<T> executeQuery(String search, Function<PathBuilder<T>, Predicate> additionalExpression){
        return autoCompleteGeneratedDslQuery.executeQuery(search, additionalExpression);
    }

    @Override
    public int getMinLength() {
        return autoCompleteGeneratedDslQuery.getMinLength();
    }

    @Override
    public void visitAttributes(BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("entity", autoCompleteGeneratedDslQuery.getEntity().getName());
        visitor.accept("fields", autoCompleteGeneratedDslQuery
                .getFields()
                .stream()
                .map(field -> field.getName())
                .collect(Collectors.joining(",")));
        visitor.accept("minLength", autoCompleteGeneratedDslQuery.getMinLength());
        visitor.accept("limitResults", autoCompleteGeneratedDslQuery.getLimitResults());
    }
}
