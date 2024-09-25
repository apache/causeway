package org.apache.causeway.persistence.querydsl.applib;

import com.querydsl.core.QueryFactory;
import com.querydsl.core.Tuple;
import com.querydsl.core.dml.DeleteClause;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;

public interface QueryDslSupport extends QueryFactory<DslQuery<?>> {

    DeleteClause<?> delete(EntityPath<?> path);

    /**
     * Create a new {@link DslQuery} instance with the given projection
     *
     * @param expr projection
     * @param <T>
     * @return select(expr)
     */
    <T> DslQuery<T> select(Expression<T> expr);

    /**
     * Create a new {@link DslQuery} instance with the given projection
     *
     * @param exprs projection
     * @return select(exprs)
     */
    DslQuery<Tuple> select(Expression<?>... exprs);

    /**
     * Create a new {@link DslQuery} instance with the given projection
     *
     * @param expr projection
     * @param <T>
     * @return select(distinct expr)
     */
    <T> DslQuery<T> selectDistinct(Expression<T> expr);

    /**
     * Create a new {@link DslQuery} instance with the given projection
     *
     * @param exprs projection
     * @return select(distinct exprs)
     */
    DslQuery<Tuple> selectDistinct(Expression<?>... exprs);

    /**
     * Create a new {@link DslQuery} instance with the projection 0
     *
     * @return select(0)
     */
    DslQuery<Integer> selectZero();

    /**
     * Create a new {@link DslQuery} instance with the projection 1
     *
     * @return select(1)
     */
    DslQuery<Integer> selectOne();

    /**
     * Create a new {@link DslQuery} instance with the given projection
     *
     * @param expr projection and source
     * @param <T>
     * @return select(expr).from(expr)
     */
    <T> DslQuery<T> selectFrom(EntityPath<T> expr);

    <T> DslQuery<T> from(EntityPath<T> from);
    <T> DslQuery<T> from(EntityPath<T>... from);
}
