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

import org.apache.isis.applib.PersistFailedException;
import org.apache.isis.applib.value.Money;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.objectstore.sql.Results;
import org.apache.isis.objectstore.sql.mapping.FieldMapping;
import org.apache.isis.objectstore.sql.mapping.FieldMappingFactory;

/**
 * Money needs to implement a two-column persistence, 1 for amount, 1 for
 * 3-digit currency
 * 
 * @version $Rev$ $Date$
 */
public class JdbcMoneyValueMapper extends AbstractJdbcMultiFieldMapping {

    public static class Factory implements FieldMappingFactory {
        private final String type1;
        private final String type2;

        public Factory(final String type1, final String type2) {
            this.type1 = type1;
            this.type2 = type2;
        }

        @Override
        public FieldMapping createFieldMapping(final ObjectSpecification object, final ObjectAssociation field) {
            return new JdbcMoneyValueMapper(field, type1, type2);
        }
    }

    public JdbcMoneyValueMapper(final ObjectAssociation field, final String type1, final String type2) {
        super(field, 2, type1, type2);
    }

    @Override
    protected Object preparedStatementObject(final int index, final Object o) {

        if (o instanceof Money) {
            if (index == 0) {
                return ((Money) o).doubleValue();
            } else {
                return ((Money) o).getCurrency();
            }
        } else {
            throw new PersistFailedException("Invalid object type " + o.getClass().getCanonicalName() + " for MoneyValueMapper");
        }
    }

    @Override
    protected Object getObjectFromResults(final Results results) {
        final double doubleValue = results.getDouble(columnName(0));
        final String currencyValue = results.getString(columnName(1));

        final Money moneyObject = new Money(doubleValue, currencyValue);

        return moneyObject;
    }

}
