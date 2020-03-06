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
package org.apache.isis.testing.fakedata.applib.services;

import java.math.BigDecimal;

import org.apache.isis.applib.annotation.Programmatic;

public class BigDecimals extends AbstractRandomValueGenerator {

    public BigDecimals(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    @Programmatic
    public BigDecimal any() {
        final long x = fake.longs().any();
        final long y = fake.ints().upTo(4);
        return new BigDecimal(String.format("%d.%d", x, y));
    }

    @Programmatic
    public BigDecimal any(final int precision, final int scale) {
        final String sign = fake.booleans().coinFlip()? "": "-";
        final String x = fake.strings().digits(precision-scale);
        final String y = fake.strings().digits(scale);
        return new BigDecimal(String.format("%s%s.%s", sign, x, y));
    }

}
