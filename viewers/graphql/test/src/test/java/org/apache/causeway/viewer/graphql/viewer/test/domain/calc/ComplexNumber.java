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
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR  CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.viewer.graphql.viewer.test.domain.calc;

import java.math.BigDecimal;
import java.util.regex.Pattern;

import jakarta.inject.Named;

import org.apache.causeway.applib.annotation.Value;

@Named("university.calc.ComplexNumber")
@Value
public record ComplexNumber(BigDecimal real, BigDecimal imaginary) {

    private static final Pattern CANONICAL_PATTERN = Pattern.compile(
            "^([+-]?(?:\\d+(?:\\.\\d*)?|\\.\\d+))([+-](?:\\d+(?:\\.\\d*)?|\\.\\d+))i$");

    public static ComplexNumber parse(final String text) {
        var matcher = CANONICAL_PATTERN.matcher(text);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("ComplexNumber requires algebraic form such as 3+4i or 3-4i");
        }
        return new ComplexNumber(new BigDecimal(matcher.group(1)), new BigDecimal(matcher.group(2)));
    }

    public String canonical() {
        var imaginaryText = imaginary.toPlainString();
        return real.toPlainString()
                + (imaginary.signum() >= 0 ? "+" : "")
                + imaginaryText
                + "i";
    }
}
