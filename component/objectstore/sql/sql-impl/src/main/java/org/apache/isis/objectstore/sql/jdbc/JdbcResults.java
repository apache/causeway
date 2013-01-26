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

package org.apache.isis.objectstore.sql.jdbc;

import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import org.apache.isis.applib.value.Date;
import org.apache.isis.applib.value.Time;
import org.apache.isis.objectstore.sql.Defaults;
import org.apache.isis.objectstore.sql.Results;
import org.apache.isis.objectstore.sql.SqlObjectStoreException;

public class JdbcResults implements Results {
    ResultSet set;

    public JdbcResults(final CallableStatement statement) {
    }

    public JdbcResults(final ResultSet set) {
        this.set = set;
    }

    @Override
    public void close() {
        try {
            set.close();
        } catch (final SQLException e) {
            throw new SqlObjectStoreException(e);
        }
    }

    @Override
    public int getInt(final String columnName) {
        try {
            return set.getInt(columnName);
        } catch (final SQLException e) {
            throw new SqlObjectStoreException(e);
        }
    }

    @Override
    public long getLong(final String columnName) {
        try {
            return set.getLong(columnName);
        } catch (final SQLException e) {
            throw new SqlObjectStoreException(e);
        }
    }

    @Override
    public String getString(final String columnName) {
        try {
            return set.getString(columnName);
        } catch (final SQLException e) {
            throw new SqlObjectStoreException(e);
        }
    }

    @Override
    public boolean next() {
        try {
            return set.next();
        } catch (final SQLException e) {
            throw new SqlObjectStoreException(e);
        }
    }

    @Override
    public java.sql.Date getJavaDateOnly(final String columnName) {
        try {
            // 2010-03-05 = 1267747200000
            // 2010-04-08 = 1270684800000
            // This is really painful! Java refuses to create java.util.Date in
            // UTC!
            // It creates java.util.Dates in Local time-zone, but assumes the DB
            // date is UTC.
            final String string = set.getString(columnName);
            final DateTime utcDate = new DateTime(string, Defaults.getTimeZone());
            final java.sql.Date date = new java.sql.Date(utcDate.getMillis());
            return date;
        } catch (final SQLException e) {
            throw new SqlObjectStoreException(e);
        }
    }

    @Override
    public java.sql.Time getJavaTimeOnly(final String columnName) {
        try {
            final String string = set.getString(columnName);

            final DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm:ss");

            final DateTime utcDate = formatter.withZone(Defaults.getTimeZone()).parseDateTime(string);
            final java.sql.Time time = new java.sql.Time(utcDate.getMillis());

            return time;
        } catch (final SQLException e) {
            throw new SqlObjectStoreException(e);
        }
    }

    @Override
    public java.util.Date getJavaDateTime(final String columnName, final Calendar calendar) {
        try {
            return set.getDate(columnName, calendar);
        } catch (final SQLException e) {
            throw new SqlObjectStoreException(e);
        }
    }

    @Override
    public Object getObject(final String columnName) {
        try {
            return set.getObject(columnName);
        } catch (final SQLException e) {
            throw new SqlObjectStoreException(e);
        }
    }

    @Override
    public Date getDate(final String columnName) {
        try {
            // 2010-03-05 = 1267747200000
            // 2010-04-08 = 1270684800000
            // This is really painful! Java refuses to create java.util.Date in
            // UTC!
            // It creates java.util.Dates in Local time-zone, but assumes the DB
            // date is UTC.
            final String string = set.getString(columnName);
            final DateTime utcDate = new DateTime(string, Defaults.getTimeZone());
            return new Date(utcDate);
        } catch (final SQLException e) {
            throw new SqlObjectStoreException(e);
        }
    }

    @Override
    public Time getTime(final String columnName) {
        try {
            final String string = set.getString(columnName);
            final DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm:ss");
            final DateTimeZone defaultTimeZone = Defaults.getTimeZone();
            final DateTime utcDate = formatter.withZone(defaultTimeZone).parseDateTime(string);
            return new Time(utcDate);
        } catch (final SQLException e) {
            throw new SqlObjectStoreException(e);
        }
    }

    @Override
    public double getDouble(final String columnName) {
        try {
            return set.getDouble(columnName);
        } catch (final SQLException e) {
            throw new SqlObjectStoreException(e);
        }
    }

    @Override
    public Object getAsType(final String columnName, final Class<?> clazz) {
        try {
            final HashMap<String, Class<?>> map = new HashMap<String, Class<?>>();
            map.put("FLOAT", float.class);
            return set.getObject(columnName, map);
        } catch (final SQLException e) {
            throw new SqlObjectStoreException(e);
        }
    }

    @Override
    public Float getFloat(final String columnName) {
        try {
            return set.getFloat(columnName);
        } catch (final SQLException e) {
            throw new SqlObjectStoreException(e);
        }
    }

    @Override
    public Short getShort(final String columnName) {
        try {
            return set.getShort(columnName);
        } catch (final SQLException e) {
            throw new SqlObjectStoreException(e);
        }
    }

    @Override
    public Boolean getBoolean(final String columnName) {
        try {
            return set.getBoolean(columnName);
        } catch (final SQLException e) {
            throw new SqlObjectStoreException(e);
        }
    }

    @Override
    public InputStream getStream(String column) {
        try {
            return set.getBinaryStream(column);
        } catch (final SQLException e) {
            throw new SqlObjectStoreException(e);
        }
    }
}
