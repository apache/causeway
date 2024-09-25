package org.apache.causeway.persistence.querydsl.applib;

import java.lang.reflect.Field;
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
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.StringPath;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import javax.ejb.ApplicationException;

import org.apache.causeway.applib.exceptions.RecoverableException;

import static org.apache.causeway.persistence.querydsl.applib.QueryDslUtil.replaceWildcards;


/**
 * Dynamically generate an auto complete query on runtime using Query DSL.
 * Auto complete operates on fields of String type ONLY.
 * The autoComplete method ALWAYS applies wildcards when NONE are specified in de given search string, executeQuery does NOT.
 */
@Builder(access = AccessLevel.PUBLIC)
@Getter
public class AutoCompleteGeneratedDslQuery {

    public final static int MIN_LENGTH = 1;
    public final static int LIMIT_RESULTS = 50;

    /**
     * Query DSL is used to dynamically generate the query on runtime
     */
    @NonNull final protected QueryDslSupport queryDslSupport;
    /**
     * the entity for which to generate the auto complete query
     */
    @NonNull final protected Class<?> entity;
    /**
     * The fields to use in the generated query in this way (field1 like searchPhrase || fieldn like searchPhrase || ...)
     */
    @NonNull final protected List<Field> fields;
    /**
     * Add additional criteria that can be added to the autocomplete method in form:
     * public static Function<EntityPathBase<T>, Predicate> autoCompletePredicate(String search)
     */
    final protected Object repository;
    final protected Method predicateMethod;

    protected Integer minLength;
    @Builder.Default
    protected Integer limitResults = LIMIT_RESULTS;

    /**
     * Dynamically generate an auto complete query on runtime using Query DSL.
     * Auto complete operates on fields of String type ONLY.
     * The autoComplete method ALWAYS applies wildcards when NONE are specified in de given search string.
     */
    public <T> List<T> autoComplete(String searchPhrase, Function<PathBuilder<T>, Predicate> additionalPredicate) {

        Function<PathBuilder<T>, Predicate> predicate = additionalPredicate;

        if (additionalPredicate == null && predicateMethod != null && repository != null) {
            // Add optional additional predicate from repository
            try {
                predicate = (Function<PathBuilder<T>, Predicate>) predicateMethod.invoke(repository, searchPhrase);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        return executeQuery(replaceWildcards(searchPhrase, true), predicate);
    }

    /**
     * Dynamically generate an auto complete query on runtime using Query DSL.
     * Auto complete operates on fields of String type ONLY.
     * The executeQuery method NEVER applies wildcards when NONE are specified in de given search string.
     */
    public <T> List<T> executeQuery(String searchPhrase, Function<PathBuilder<T>, Predicate> additionalPredicate) {
        Optional<DslQuery> q = generateQuery(searchPhrase, additionalPredicate);
        return q.map(query -> query.fetch()).orElse(QueryDslUtil.newList());
    }


    public <T> Optional<DslQuery> generateQuery(final String searchPhrase, Function<PathBuilder<T>, Predicate> additionalPredicate) {
        if (fields.isEmpty()) {
            throw new RecoverableException("At least one field should be given");
        }
        if (QueryDslUtil.isNotEmpty(searchPhrase) &&
                searchPhrase.trim().length()>=getMinLength()) {
            // Define entity
            PathBuilder<T> entityPath = new PathBuilder(entity, "e");
            BooleanBuilder where = new BooleanBuilder();
            List<OrderSpecifier<String>> orderSpecifiers = QueryDslUtil.newList();

            // Build where and order clause
            fields.stream().forEach(field -> {
                // Only string type fields are supported
                StringPath stringPath = entityPath.getString(field.getName());
                String searchReplaced = replaceWildcards(searchPhrase, false);
                BooleanExpression expr = QueryDslUtil.search(stringPath, searchReplaced, false);

                // Case-insensitive?
                if (field.isAnnotationPresent(AutoComplete.class) &&
                        field.getAnnotationsByType(AutoComplete.class)[0].caseInsensitive()) {
                    expr = QueryDslUtil.search(stringPath, searchReplaced, true);
                }
                where.or(expr);

                // Build order by clause
                orderSpecifiers.add(stringPath.asc());
            });

            // Build query
            DslQuery q = queryDslSupport.selectFrom(entityPath);
            // Add additional expression if any
            if(additionalPredicate!=null){
                where.and(additionalPredicate.apply(entityPath));
            }
            q.where(where);
            q.orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]));
            q.limit(limitResults==null?LIMIT_RESULTS:limitResults);
            return Optional.of(q);
        }
        return Optional.empty();
    }

    public int getMinLength(){
        if(minLength==null){
            minLength=MIN_LENGTH;
            AutoCompleteDomain acd=entity.getAnnotation(AutoCompleteDomain.class);
            if(acd!=null && acd.minLength()>MIN_LENGTH){
                minLength= acd.minLength();
            }
        }
        return minLength;
    }
}