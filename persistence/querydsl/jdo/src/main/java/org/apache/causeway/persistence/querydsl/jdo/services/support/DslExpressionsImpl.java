package org.apache.causeway.persistence.querydsl.jdo.services.support;

import org.apache.causeway.persistence.querydsl.applib.DslQuery;

import org.apache.causeway.persistence.querydsl.applib.services.support.DslExpressions;

import org.springframework.stereotype.Service;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.jdo.JDOQuery;

@Service
public class DslExpressionsImpl implements DslExpressions {

    @Override
    public <T> DslQuery<T> select(Expression<T> expr) {
        return DslQueryJdoImpl.of(new JDOQuery<Void>().select(expr));
    }

    @Override
    public DslQuery<Tuple> select(Expression<?>... exprs) {
        return DslQueryJdoImpl.of(new JDOQuery<Void>().select(exprs));
    }

}
