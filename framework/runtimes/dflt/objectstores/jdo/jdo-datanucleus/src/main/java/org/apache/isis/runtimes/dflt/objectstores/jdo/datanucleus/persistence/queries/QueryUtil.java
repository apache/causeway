package org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence.queries;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.log4j.Logger;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public final class QueryUtil {

    private static final Logger LOG = Logger.getLogger(QueryUtil.class);

    private QueryUtil() {}

    public static Query createQuery(
            final PersistenceManager persistenceManager,
            final String alias,
            final String select,
            final ObjectSpecification specification,
            final String whereClause) {

        final StringBuilder buf = new StringBuilder(128);
        appendSelect(buf, select, alias);
        appendFrom(buf, specification, alias);
        appendWhere(buf, whereClause);

        final String queryString = buf.toString();
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating query: " + queryString);
        }

        return persistenceManager.newQuery(queryString);
    }

    private static StringBuilder appendSelect(
            final StringBuilder buf,
            final String select, 
            String alias) {
        if (select != null) {
            buf.append(select);
        } else {
            buf.append("select ");
            // not required in JDOQL (cf JPA QL)
            // buf.append(alias);
        }
        buf.append(" ");
        return buf;
    }

    private static void appendWhere(StringBuilder buf, String whereClause) {
        if(whereClause == null) {
            return;
        }
        buf.append(" where ").append(whereClause);
    }


    private static StringBuilder appendFrom(
            final StringBuilder buf,
            final ObjectSpecification specification,
            final String alias) {
        return buf.append("from ")
                .append(specification.getFullIdentifier())
                .append(" as ")
                .append(alias);
    }
}


// Copyright (c) Naked Objects Group Ltd.
