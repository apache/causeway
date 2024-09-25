package org.apache.causeway.persistence.querydsl.applib;

import com.querydsl.core.types.OrderSpecifier;

import lombok.Getter;
import lombok.Setter;

/**
 * Helper object constructed by a builder to mimick the behavior of Java Spring Pageable.
 * </p>
 * Holds offset (default 0)
 * Holds limit (default 1000_000)
 * Hold orders which are required to be filled otherwise paging related clauses will not work on MS SQL.
 */
public class Pageable {
    @Getter @Setter
    private long offset;
    @Getter @Setter
    private long limit;
    @Getter @Setter
    OrderSpecifier<? extends Comparable>[] orders;

    protected Pageable(long offset, long limit, OrderSpecifier<? extends Comparable>... orders) {
        this.offset = offset;
        this.limit = limit;
        this.orders = orders;
    }

    public static PageableBuilder builder() {
        return new PageableBuilder();
    }

    public static class PageableBuilder {
        private long offset = 0L;
        private long limit = 1000_000L;//Just an arbitrary length
        private OrderSpecifier<? extends Comparable>[] orders;

        PageableBuilder() {
        }

        public PageableBuilder offset(long offset) {
            this.offset = offset;
            return this;
        }

        public PageableBuilder limit(long limit) {
            this.limit = limit;
            return this;
        }

        public PageableBuilder orders(OrderSpecifier<? extends Comparable>... orders) {
            this.orders = orders;
            return this;
        }

        public Pageable build() {
            if (QueryDslUtil.isEmpty(this.orders)) {
                throw new IllegalArgumentException("'orders' should be configured");
            }
            return new Pageable(this.offset, this.limit, this.orders);
        }
    }
}