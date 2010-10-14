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


package org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.usertype;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.apache.isis.applib.value.Time;


/**
 * A user type that maps an SQL TIME to a NOF Time value.
 */
public class TimeType extends ImmutableUserType {

    public Object nullSafeGet(final ResultSet rs, final String[] names, final Object owner) throws HibernateException,
            SQLException {
        final java.sql.Time time = rs.getTime(names[0]);
        if (rs.wasNull()) {
            return null;
        }
        return new Time(time);
    }

    public void nullSafeSet(final PreparedStatement st, final Object value, final int index) throws HibernateException,
            SQLException {
        if (value == null) {
            st.setNull(index, Hibernate.TIME.sqlType());
        } else {
            final Time nofTime = (Time) value;
            final java.sql.Time sqlTime = new java.sql.Time(nofTime.dateValue().getTime());
            st.setTime(index, sqlTime);
        }
    }

    public Class<Time> returnedClass() {
        return Time.class;
    }

    public int[] sqlTypes() {
        return new int[] { Hibernate.TIME.sqlType() };
    }
}
