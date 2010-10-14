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

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.Hibernate;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.type.Type;
import org.apache.isis.applib.value.Money;


/**
 * A user type that maps an SQL BIG_DECIMAL and String to a NOF Money value.
 */

public class MoneyType extends ImmutableCompositeType {

    public Class<Money> returnedClass() {
        return Money.class;
    }

    public Object nullSafeGet(
            final ResultSet resultSet,
            final String[] names,
            final SessionImplementor session,
            final Object owner) throws SQLException {
        final BigDecimal amount = resultSet.getBigDecimal(names[0]);
        if (resultSet.wasNull()) {
            return null;
        }
        final String currency = resultSet.getString(names[1]);
        return new Money(amount.doubleValue(), currency);
    }

    public void nullSafeSet(
            final PreparedStatement statement,
            final Object value,
            final int index,
            final SessionImplementor session) throws SQLException {
        if (value == null) {
            statement.setNull(index, Hibernate.BIG_DECIMAL.sqlType());
            statement.setNull(index + 1, Hibernate.STRING.sqlType());
        } else {
            final Money amount = (Money) value;
            statement.setBigDecimal(index, amount.getAmount());
            statement.setString(index + 1, amount.getCurrency());
        }
    }

    public String[] getPropertyNames() {
        return new String[] { "amount", "currency" };
    }

    public Type[] getPropertyTypes() {
        return new Type[] { Hibernate.BIG_DECIMAL, Hibernate.STRING };
    }

    public Object getPropertyValue(final Object component, final int property) {
        final Money monetaryAmount = (Money) component;
        if (property == 0) {
            return monetaryAmount.getAmount();
        } else {
            return monetaryAmount.getCurrency();
        }
    }

    public void setPropertyValue(final Object component, final int property, final Object value) {
        throw new UnsupportedOperationException("Money is immutable");
    }

}
