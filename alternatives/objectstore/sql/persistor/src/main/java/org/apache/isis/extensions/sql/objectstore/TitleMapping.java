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


package org.apache.isis.extensions.sql.objectstore;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;


public class TitleMapping {
    private String column = Sql.identifier("NO_title");

     protected String getColumn() {
        return column;
    }
     
    public void appendWhereClause(StringBuffer sql, String title) {
        appendAssignment(sql, title);
    }

    private void appendAssignment(StringBuffer sql, String title) {
        sql.append(column);
        sql.append(" = ");
        appendTitle(sql, title);
    }

    public void appendColumnDefinitions(StringBuffer sql) {
        sql.append(column);
        sql.append(" ");
        sql.append("varchar(200)");
    }

    public void appendColumnNames(StringBuffer sql) {
        sql.append(column);
    }

    //TODO:KAM:here 
    public void appendInsertValues(DatabaseConnector connector, StringBuffer sql, ObjectAdapter object) {
        if (object == null) {
            //sql.append("NULL");
        	connector.addToQueryValues(null);
        } else {
            //appendTitle(sql, object.titleString());
        	connector.addToQueryValues(object.titleString().toLowerCase());
        }
    	sql.append("?");
    }

    private void appendTitle(StringBuffer sql, String title) {
        String titleString = title.toLowerCase();
        sql.append(Sql.escapeAndQuoteValue(titleString));
    }

    public void appendUpdateAssignment(DatabaseConnector connector, StringBuffer sql, ObjectAdapter object) {
        sql.append(column);
        sql.append(" = ");
        appendInsertValues(connector, sql, object);
        
    }
}

