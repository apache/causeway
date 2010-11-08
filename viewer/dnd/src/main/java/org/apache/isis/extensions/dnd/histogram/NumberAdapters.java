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


package org.apache.isis.extensions.dnd.histogram;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.progmodel.facets.value.DoubleFloatingPointValueFacet;
import org.apache.isis.core.progmodel.facets.value.FloatingPointValueFacet;
import org.apache.isis.core.progmodel.facets.value.IntegerValueFacet;
import org.apache.isis.core.progmodel.facets.value.MoneyValueFacet;


public class NumberAdapters {
    static interface Converter {
        double value(ObjectAdapter value);
    }

    static {
        new Converter() {
            public double value(ObjectAdapter arg0) {
                return 0;
            }
        };

    }

    public static boolean contains(ObjectAssociation t) {
        return t.getSpecification().containsFacet(IntegerValueFacet.class)
                || t.getSpecification().containsFacet(DoubleFloatingPointValueFacet.class)
                || t.getSpecification().containsFacet(FloatingPointValueFacet.class)
                || t.getSpecification().containsFacet(MoneyValueFacet.class);
    }

    public static double doubleValue(ObjectAssociation field, ObjectAdapter value) {
        ObjectSpecification specification = value.getSpecification();

        IntegerValueFacet intValueFacet = specification.getFacet(IntegerValueFacet.class);
        if (intValueFacet != null) {
            return intValueFacet.integerValue(value).doubleValue();
        }

        DoubleFloatingPointValueFacet doubleValueFacet = specification.getFacet(DoubleFloatingPointValueFacet.class);
        if (doubleValueFacet != null) {
            return doubleValueFacet.doubleValue(value).doubleValue();
        }

        FloatingPointValueFacet floatValueFacet = specification.getFacet(FloatingPointValueFacet.class);
        if (floatValueFacet != null) {
            return floatValueFacet.floatValue(value).doubleValue();
        }

        MoneyValueFacet moneyValueFacet = specification.getFacet(MoneyValueFacet.class);
        if (moneyValueFacet != null) {
            return moneyValueFacet.getAmount(value);
        }

        return 0.0;
    }

}

