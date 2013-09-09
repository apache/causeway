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

package org.apache.isis.objectstore.sql.mapping;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.objectstore.sql.DatabaseConnector;
import org.apache.isis.objectstore.sql.Results;

public interface FieldMapping {
	
    public ObjectAssociation getField();

    void appendColumnDefinitions(StringBuffer sql);

    void appendColumnNames(StringBuffer sql);

    void appendInsertValues(DatabaseConnector connector, StringBuffer sql, ObjectAdapter object);

    void appendUpdateValues(DatabaseConnector connector, StringBuffer sql, ObjectAdapter object);

    void initializeField(ObjectAdapter object, Results rs);

    void appendWhereClause(DatabaseConnector connector, StringBuffer sql, ObjectAdapter object);

    void debugData(DebugBuilder debug);

    void appendWhereObject(DatabaseConnector connector, ObjectAdapter objectAdapter);

}
