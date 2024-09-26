package org.apache.causeway.persistence.querydsl.jpa.services.support;

import org.apache.causeway.persistence.querydsl.applib.DslQuery;
import org.apache.causeway.persistence.querydsl.applib.services.support.DslExpressions;

import org.springframework.stereotype.Service;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.jpa.impl.JPAQuery;

@Service
public class DslExpressionsImpl implements DslExpressions {

    @Override
    public <T> DslQuery<T> select(Expression<T> expr) {
        return DslQueryJpaImpl.of(new JPAQuery<Void>().select(expr));
    }

    @Override
    public DslQuery<Tuple> select(Expression<?>... exprs) {
        return DslQueryJpaImpl.of(new JPAQuery<Void>().select(exprs));
    }

}
