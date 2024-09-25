package org.apache.causeway.persistence.querydsl.applib;

import com.querydsl.core.FetchableQuery;
import com.querydsl.core.Query;
import com.querydsl.core.support.ExtendedSubQuery;
import com.querydsl.core.types.CollectionExpression;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;

public interface DslQuery<T> extends FetchableQuery<T, DslQuery<T>>, Query<DslQuery<T>>, ExtendedSubQuery<T> {

    /**
     * Change the projection of this query
     *
     * @param <U> the new subtype
     * @param expr new projection
     *
     * @return the current object
     */
    <U> DslQuery<U> projection(Expression<U> expr);

    /**
     * Add query sources
     *
     * @param sources sources
     * @return the current object
     */
    DslQuery<T> from(EntityPath<?>... sources);

    /**
     * Add query sources
     *
     * @param path source
     * @param alias alias
     * @param <U>
     * @return the current object
     */
    <U> DslQuery<T> from(CollectionExpression<?, U> path, Path<U> alias);
}
