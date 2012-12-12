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

package org.apache.isis.objectstore.sql;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;

public class TitleMapping {
    private final String column = Sql.identifier("NO_title");

    protected String getColumn() {
        return column;
    }

    public void appendWhereClause(final StringBuffer sql, final String title) {
        appendAssignment(sql, title);
    }

    private void appendAssignment(final StringBuffer sql, final String title) {
        sql.append(column);
        sql.append(" = ");
        appendTitle(sql, title);
    }

    public void appendColumnDefinitions(final StringBuffer sql) {
        sql.append(column);
        sql.append(" ");
        sql.append("varchar(200)");
    }

    public void appendColumnNames(final StringBuffer sql) {
        sql.append(column);
    }

    public void appendInsertValues(final DatabaseConnector connector, final StringBuffer sql, final ObjectAdapter object) {
        if (object == null) {
            sql.append("NULL");
        } else {
            connector.addToQueryValues(object.titleString().toLowerCase());
        }
        sql.append("?");
    }

    private void appendTitle(final StringBuffer sql, final String title) {
        final String titleString = title.toLowerCase();
        sql.append(Sql.escapeAndQuoteValue(titleString));
    }

    public void appendUpdateAssignment(final DatabaseConnector connector, final StringBuffer sql, final ObjectAdapter object) {
        sql.append(column);
        sql.append(" = ");
        appendInsertValues(connector, sql, object);

    }
}
