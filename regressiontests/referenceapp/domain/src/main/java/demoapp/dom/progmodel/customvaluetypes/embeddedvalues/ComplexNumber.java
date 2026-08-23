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
package demoapp.dom.progmodel.customvaluetypes.embeddedvalues;

import java.util.function.BiFunction;

import lombok.Value;

public interface ComplexNumber {

    double getRe();
    double getIm();

    /**
     * convert to string
     */
    default String asString() {
        return getRe() +
                (getIm() >= 0
                        ? (" + " +  getIm())
                        : (" - " + (-getIm())))
                + "i";
    }

    /**
     * convert from string
     */
    static <T extends ComplexNumber> T parse(
            final String complexNumberString, final BiFunction<Double, Double, T> factory) {
        if(!org.springframework.util.StringUtils.hasLength(complexNumberString)
                || complexNumberString.contains("NaN")) {
            return null;
        }
        // this is a naive implementation, just for demo
        final String[] parts = complexNumberString.split("\\+|i");
        var real = Double.parseDouble(parts[0]);
        var imaginary = Double.parseDouble(parts[1]);
        return factory.apply(real, imaginary);
    }

    // used for seeding
    public static SimpleNamedComplexNumber named(
            final String name,
            final double re,
            final double im) {
        return new SimpleNamedComplexNumber(name, re, im);
    }

    // helper type used for seeding
    @Value
    static class SimpleNamedComplexNumber
    implements ComplexNumber {
        final String name;
        final double re;
        final double im;
    }

}
