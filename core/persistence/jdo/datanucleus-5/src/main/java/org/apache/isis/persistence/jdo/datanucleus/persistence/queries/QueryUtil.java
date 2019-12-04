/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.persistence.jdo.datanucleus.persistence.queries;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.isis.metamodel.spec.ObjectSpecification;

import lombok.extern.log4j.Log4j2;

@Log4j2
public final class QueryUtil {

    private QueryUtil() {}

    public static Query<?> createQuery(
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
        log.debug("creating query: {}", queryString);

        final Query<?> jdoQuery = persistenceManager.newQuery(queryString);

        // http://www.datanucleus.org/servlet/jira/browse/NUCCORE-1103
        jdoQuery.addExtension("datanucleus.multivaluedFetch", "none");

        return jdoQuery;
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
