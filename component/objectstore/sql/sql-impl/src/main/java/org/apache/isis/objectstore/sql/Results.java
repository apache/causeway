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

import java.io.InputStream;
import java.sql.Time;
import java.util.Calendar;
import java.util.Date;

public interface Results {

    void close();

    int getInt(String columnName);

    long getLong(String columnName);

    double getDouble(String columnName);

    String getString(String columnName);

    Float getFloat(String columnName);

    Object getShort(String columnName);

    Object getBoolean(String columnName);

    boolean next();

    Date getJavaDateOnly(String dateColumn);

    Time getJavaTimeOnly(String timeColumn);

    Date getJavaDateTime(String lastActivityDateColumn, Calendar calendar);

    org.apache.isis.applib.value.Date getDate(String columnName);

    org.apache.isis.applib.value.Time getTime(String columnName);

    Object getObject(String column);

    Object getAsType(String columnName, Class<?> clazz);

    InputStream getStream(String column);

}
