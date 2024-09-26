package org.apache.causeway.persistence.querydsl.jdo.services.support;

import java.util.function.Supplier;

import javax.inject.Inject;
import javax.jdo.PersistenceManager;

import com.querydsl.core.Tuple;
import com.querydsl.core.dml.DeleteClause;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.jdo.JDOQuery;
import com.querydsl.jdo.JDOQueryFactory;

import org.apache.causeway.persistence.querydsl.applib.DslQuery;
import org.apache.causeway.persistence.querydsl.applib.services.support.QueryDslSupport;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import org.apache.causeway.persistence.jdo.applib.services.JdoSupportService;

@Component
@Primary
public class QueryDslSupportJdoImpl implements QueryDslSupport {

    @Inject protected JdoSupportService jdoSupportService;
    protected CustomizedJdoQueryFactory queryFactory;

    protected CustomizedJdoQueryFactory getQueryFactory(){
        if(queryFactory==null){
            queryFactory = new CustomizedJdoQueryFactory(() -> jdoSupportService.getPersistenceManager());
        }
        return queryFactory;
    }

    class CustomizedJdoQueryFactory extends JDOQueryFactory{
        public CustomizedJdoQueryFactory(Supplier<PersistenceManager> persistenceManager) {
            super(persistenceManager);
        }
        public JDOQuery<?> from(EntityPath<?>... from){
            return this.query().from(from);
        }
    }

    @Override
    public DeleteClause<?> delete(EntityPath<?> path) {
        return getQueryFactory().delete(path);
    }

    @Override
    public <T> DslQuery<T> select(Expression<T> expr) {
        return DslQueryJdoImpl.of(getQueryFactory().select(expr));
    }

    @Override
    public DslQuery<Tuple> select(Expression<?>... exprs) {
        return DslQueryJdoImpl.of(getQueryFactory().select(exprs));
    }

    @Override
    public <T> DslQuery<T> selectDistinct(Expression<T> expr) {
        return DslQueryJdoImpl.of(getQueryFactory().selectDistinct(expr));
    }

    @Override
    public DslQuery<Tuple> selectDistinct(Expression<?>... exprs) {
        return DslQueryJdoImpl.of(getQueryFactory().selectDistinct(exprs));
    }

    @Override
    public DslQuery<Integer> selectZero() {
        return DslQueryJdoImpl.of(getQueryFactory().selectZero());
    }

    @Override
    public DslQuery<Integer> selectOne() {
        return DslQueryJdoImpl.of(getQueryFactory().selectOne());
    }

    @Override
    public <T> DslQuery<T> selectFrom(EntityPath<T> expr) {
        return DslQueryJdoImpl.of(getQueryFactory().selectFrom(expr));
    }

    @Override
    public <T> DslQuery<T> from(EntityPath<T> from) {
        return (DslQuery<T>) DslQueryJdoImpl.of(getQueryFactory().from(from));
    }

    @Override
    public <T> DslQuery<T> from(EntityPath<T>... from) {
        return (DslQuery<T>) DslQueryJdoImpl.of(getQueryFactory().from(from));
    }

    @Override
    public DslQuery<?> query() {
        return DslQueryJdoImpl.of(getQueryFactory().query());
    }

}
