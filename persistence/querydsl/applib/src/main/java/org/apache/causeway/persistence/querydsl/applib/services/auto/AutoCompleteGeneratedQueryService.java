package org.apache.causeway.persistence.querydsl.applib.services.auto;

import java.util.List;
import java.util.function.Function;

import org.apache.causeway.persistence.querydsl.applib.annotation.AutoComplete;
import org.apache.causeway.persistence.querydsl.applib.annotation.AutoCompleteDomain;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;

/**
 * Programmatic access to the autocomplete functionality defined declaratively by
 * {@link AutoCompleteDomain} and {@link AutoComplete}.
 */
public interface AutoCompleteGeneratedQueryService {

    /**
     * Convenience method for programmatically delegate to the generated facet query
     * @param cls for which the auto generated query should be called
     * @param searchPhrase wildcard will ALWAYS be added when absent
     * @return
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
     * @return
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
     * @return
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
     * @return
     * @param <T>
     */
    <T> List<T> executeQuery(
            final Class<T> cls,
            final String searchPhrase,
            final Function<PathBuilder<T>, Predicate> additionalExpression);

}
