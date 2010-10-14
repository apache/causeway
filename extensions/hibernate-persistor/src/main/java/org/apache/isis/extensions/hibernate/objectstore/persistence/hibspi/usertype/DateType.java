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
import org.apache.isis.applib.value.Date;


/**
 * A user type that maps an SQL DATE to a NOF Date value.
 */
public class DateType extends ImmutableUserType {

    public Object nullSafeGet(final ResultSet rs, final String[] names, final Object owner) throws HibernateException,
            SQLException {
        final java.util.Date date = rs.getDate(names[0]);
        if (rs.wasNull()) {
            return null;
        }
        return new Date(date);
    }

    public void nullSafeSet(final PreparedStatement st, final Object value, final int index) throws HibernateException,
            SQLException {
        if (value == null) {
            st.setNull(index, Hibernate.DATE.sqlType());
        } else {
            final Date nofDate = (Date) value;
            final java.sql.Date sqlDate = new java.sql.Date(nofDate.dateValue().getTime());
            st.setDate(index, sqlDate);
        }
    }

    public Class<Date> returnedClass() {
        return Date.class;
    }

    public int[] sqlTypes() {
        return new int[] { Hibernate.DATE.sqlType() };
    }
}
