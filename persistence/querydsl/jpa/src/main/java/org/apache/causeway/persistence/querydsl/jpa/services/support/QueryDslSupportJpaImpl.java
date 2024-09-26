package org.apache.causeway.persistence.querydsl.jpa.services.support;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.querydsl.core.Tuple;
import com.querydsl.core.dml.DeleteClause;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import org.apache.causeway.persistence.querydsl.applib.DslQuery;
import org.apache.causeway.persistence.querydsl.applib.services.support.QueryDslSupport;


@Component
@ConditionalOnMissingBean(QueryDslSupport.class)
public class QueryDslSupportJpaImpl implements QueryDslSupport {

    @PersistenceContext EntityManager entityManager;

    protected JPAQueryFactory queryFactory;

    protected JPAQueryFactory getQueryFactory(){
        if(queryFactory==null){
            queryFactory = new JPAQueryFactory(() -> entityManager);
        }
        return queryFactory;
    }

    @Override
    public DeleteClause<?> delete(EntityPath<?> path) {
        return getQueryFactory().delete(path);
    }

    @Override
    public <T> DslQuery<T> select(Expression<T> expr) {
        return DslQueryJpaImpl.of(getQueryFactory().select(expr));
    }

    @Override
    public DslQuery<Tuple> select(Expression<?>... exprs) {
        return DslQueryJpaImpl.of(getQueryFactory().select(exprs));
    }

    @Override
    public <T> DslQuery<T> selectDistinct(Expression<T> expr) {
        return DslQueryJpaImpl.of(getQueryFactory().selectDistinct(expr));
    }

    @Override
    public DslQuery<Tuple> selectDistinct(Expression<?>... exprs) {
        return DslQueryJpaImpl.of(getQueryFactory().selectDistinct(exprs));
    }

    @Override
    public DslQuery<Integer> selectZero() {
        return DslQueryJpaImpl.of(getQueryFactory().selectZero());
    }

    @Override
    public DslQuery<Integer> selectOne() {
        return DslQueryJpaImpl.of(getQueryFactory().selectOne());
    }

    @Override
    public <T> DslQuery<T> selectFrom(EntityPath<T> expr) {
        return DslQueryJpaImpl.of(getQueryFactory().selectFrom(expr));
    }

    @Override
    public <T> DslQuery<T> from(EntityPath<T> from) {
        return (DslQuery<T>) DslQueryJpaImpl.of(getQueryFactory().from(from));
    }

    @Override
    public <T> DslQuery<T> from(EntityPath<T>... from) {
        return (DslQuery<T>) DslQueryJpaImpl.of(getQueryFactory().from(from));
    }


    @Override
    public DslQuery<?> query() {
        return DslQueryJpaImpl.of(getQueryFactory().query());
    }
}
