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

import java.sql.Timestamp;
import java.util.Date;

import org.apache.isis.core.metamodel.adapter.version.SerialNumberVersion;
import org.apache.isis.core.metamodel.adapter.version.Version;


public class VersionMapping {
    private String lastActivityDateColumn;
    private String lastActivityUserColumn;
    private String versionColumn;

    public void init() {
        lastActivityDateColumn = Sql.identifier("MODIFIED_ON");
        lastActivityUserColumn = Sql.identifier("MODIFIED_BY");
        versionColumn = Sql.identifier("VERSION");
    }

    public String insertColumns() {
        return versionColumn + ", " + lastActivityUserColumn + ", " + lastActivityDateColumn;
    }

    public String insertValues(SerialNumberVersion version) {
        String timestamp = new Timestamp(new Date().getTime()).toString();
        return version.sequence() + ", '" + version.getUser() + "',  '" + timestamp + "'";
    }

    public String whereClause(SerialNumberVersion version) {
        return versionColumn + " = " + version.getSequence();
    }

    public String updateAssigment(long nextSequence) {
        return versionColumn + " = " + nextSequence;
    }

    public String appendSelectColumns() {
        StringBuffer sql = new StringBuffer();
        sql.append(versionColumn);
        sql.append(",");
        sql.append(lastActivityUserColumn);
        sql.append(",");
        sql.append(lastActivityDateColumn);
        return sql.toString();
    }

    public String appendColumnDefinitions() {
        StringBuffer sql = new StringBuffer();

        sql.append(versionColumn);
        sql.append(" bigint");

        sql.append(",");
        sql.append(lastActivityUserColumn);
        sql.append(" varchar(32)");

        sql.append(",");
        sql.append(lastActivityDateColumn);
        sql.append(" timestamp");

        return sql.toString();
    }

    public Object appendUpdateValues(long versionSequence) {
        return versionColumn + "=" + versionSequence;
    }

    public Version getLock(Results rs) {
        long number = rs.getLong(versionColumn);
        String user = rs.getString(lastActivityUserColumn);
        Date time = rs.getDate(lastActivityDateColumn);
        Version version = new SerialNumberVersion(number, user, time);
        return version;
    }

}

