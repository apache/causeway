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
package org.apache.isis.testing.specsupport.applib.specs;

import org.joda.time.format.DateTimeFormat;

/**
 * A set of converters for built-in value types; for use in Cucumber step definitions.
 */
public class V {

    private V() {
    }

    /**
     * @deprecated this is just a dummy, it seems the former cucumber.api.Transformer was removed; 
     * what's the replacement?
     *
     * @param <T>
     */
    public static abstract class Transformer<T> {
        public abstract T transform(java.lang.String value);
    }

    /**
     * Converts {@link java.lang.String}s to {@link java.lang.Byte}, but also recognizing the
     * keyword 'null'.
     */
    public static class Byte extends Transformer<java.lang.Byte> {

        @Override
        public java.lang.Byte transform(java.lang.String value) {
            return value == null || "null".equals(value)
                    ? null
                            : java.lang.Byte.parseByte(value);
        }

        public static java.lang.Byte as(Object value) {
            return value != null && value instanceof java.lang.String
                    ? new Byte().transform((java.lang.String) value)
                            : null;
        }
    }

    /**
     * Converts {@link java.lang.String}s to {@link java.lang.Short}, but also recognizing the
     * keyword 'null'.
     */
    public static class Short extends Transformer<java.lang.Short> {

        @Override
        public java.lang.Short transform(java.lang.String value) {
            return value == null || "null".equals(value)
                    ? null
                            : java.lang.Short.parseShort(value);
        }

        public static java.lang.Short as(Object value) {
            return value != null && value instanceof java.lang.String
                    ? new Short().transform((java.lang.String) value)
                            : null;
        }
    }

    /**
     * Converts {@link java.lang.String}s to {@link java.lang.Integer}, but also recognizing the
     * keyword 'null'.
     */
    public static class Integer extends Transformer<java.lang.Integer> {

        @Override
        public java.lang.Integer transform(java.lang.String value) {
            return value == null || "null".equals(value)
                    ? null
                            : java.lang.Integer.parseInt(value);
        }

        public static java.lang.Integer as(Object value) {
            return value != null && value instanceof java.lang.String
                    ? new Integer().transform((java.lang.String) value)
                            : null;
        }
    }

    /**
     * Converts {@link java.lang.String}s to {@link java.lang.Long}, but also recognizing the
     * keyword 'null'.
     */
    public static class Long extends Transformer<java.lang.Long> {

        @Override
        public java.lang.Long transform(java.lang.String value) {
            return value == null || "null".equals(value)
                    ? null
                            : java.lang.Long.parseLong(value);
        }

        public static java.lang.Long as(Object value) {
            return value != null && value instanceof java.lang.String
                    ? new Long().transform((java.lang.String) value)
                            : null;
        }
    }

    /**
     * Converts {@link java.lang.String}s to {@link java.lang.Float}, but also recognizing the
     * keyword 'null'.
     */
    public static class Float extends Transformer<java.lang.Float> {

        @Override
        public java.lang.Float transform(java.lang.String value) {
            return value == null || "null".equals(value)
                    ? null
                            : java.lang.Float.parseFloat(value);
        }

        public static java.lang.Float as(Object value) {
            return value != null && value instanceof java.lang.String
                    ? new Float().transform((java.lang.String) value)
                            : null;
        }
    }

    /**
     * Converts {@link java.lang.String}s to {@link java.lang.Double}, but also recognizing the
     * keyword 'null'.
     */
    public static class Double extends Transformer<java.lang.Double> {

        @Override
        public java.lang.Double transform(java.lang.String value) {
            return value == null || "null".equals(value)
                    ? null
                            : java.lang.Double.parseDouble(value);
        }

        public static java.lang.Double as(Object value) {
            return value != null && value instanceof java.lang.String
                    ? new Double().transform((java.lang.String) value)
                            : null;
        }
    }

    /**
     * Converts {@link java.lang.String}s to {@link java.math.BigInteger}, but also recognizing the
     * keyword 'null'.
     */
    public static class BigInteger extends Transformer<java.math.BigInteger> {

        @Override
        public java.math.BigInteger transform(java.lang.String value) {
            return value == null || "null".equals(value)
                    ? null
                            : new java.math.BigInteger(value);
        }

        public static java.math.BigInteger as(Object value) {
            return value != null && value instanceof java.lang.String
                    ? new BigInteger().transform((java.lang.String) value)
                            : null;
        }
    }

    /**
     * Converts {@link java.lang.String}s to {@link java.math.BigDecimal}, but also recognizing the
     * keyword 'null'.
     */
    public static class BigDecimal extends Transformer<java.math.BigDecimal> {

        @Override
        public java.math.BigDecimal transform(java.lang.String value) {
            return value == null || "null".equals(value)
                    ? null
                            : new java.math.BigDecimal(value);
        }

        public static java.math.BigDecimal as(Object value) {
            return value != null && value instanceof java.lang.String
                    ? new BigDecimal().transform((java.lang.String) value)
                            : null;
        }
    }

    /**
     * Converts {@link java.lang.String}s to {@link java.lang.Character}, but also recognizing the
     * keyword 'null'.
     */
    public static class Character extends Transformer<java.lang.Character> {

        @Override
        public java.lang.Character transform(java.lang.String value) {
            return value == null || "null".equals(value) || value.length() <1
                    ? null
                            : value.charAt(0);
        }

        public static java.lang.Character as(Object value) {
            return value != null && value instanceof java.lang.String
                    ? new Character().transform((java.lang.String) value)
                            : null;
        }
    }

    /**
     * Converts {@link java.lang.String}s to {@link java.lang.String}, but also recognizing the
     * keyword 'null'.
     */
    public static class String extends Transformer<java.lang.String> {

        @Override
        public java.lang.String transform(java.lang.String value) {
            return value == null || "null".equals(value)
                    ? null
                            : value;
        }

        public static java.lang.String as(Object value) {
            return value != null && value instanceof java.lang.String
                    ? new String().transform((java.lang.String) value)
                            : null;
        }
    }

    /**
     * Converts {@link java.lang.String}s to {@link org.joda.time.LocalDate}, but also recognizing the
     * keyword 'null'.
     */
    public static class LyyyyMMdd extends Transformer<org.joda.time.LocalDate> {

        @Override
        public org.joda.time.LocalDate transform(java.lang.String value) {
            return value == null || "null".equals(value)
                    ? null
                            : DateTimeFormat.forPattern("yyyy-MM-dd").parseLocalDate(value);
        }

        public static org.joda.time.LocalDate as(Object value) {
            return value != null && value instanceof java.lang.String
                    ? new LyyyyMMdd().transform((java.lang.String) value)
                            : null;
        }
    }

    /**
     * Converts {@link java.lang.String}s to {@link org.joda.time.DateTime}, but also recognizing the
     * keyword 'null'.
     */
    public static class yyyyMMddHHmmss extends Transformer<org.joda.time.DateTime> {

        @Override
        public org.joda.time.DateTime transform(java.lang.String value) {
            return value == null || "null".equals(value)
                    ? null
                            : DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").parseDateTime(value);
        }

        public static org.joda.time.DateTime as(Object value) {
            return value != null && value instanceof java.lang.String
                    ? new yyyyMMddHHmmss().transform((java.lang.String) value)
                            : null;
        }
    }

    /**
     * Converts {@link java.lang.String}s to {@link org.joda.time.DateTime}, but also recognizing the
     * keyword 'null'.
     */
    public static class yyyyMMddHHmm extends Transformer<org.joda.time.DateTime> {

        @Override
        public org.joda.time.DateTime transform(java.lang.String value) {
            return value == null || "null".equals(value)
                    ? null
                            : DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(value);
        }

        public static org.joda.time.DateTime as(Object value) {
            return value != null && value instanceof java.lang.String
                    ? new yyyyMMddHHmmss().transform((java.lang.String) value)
                            : null;
        }
    }

    /**
     * Converts {@link java.lang.String}s to {@link org.joda.time.LocalDateTime}, but also recognizing the
     * keyword 'null'.
     */
    public static class LyyyyMMddHHmm extends Transformer<org.joda.time.LocalDateTime> {

        @Override
        public org.joda.time.LocalDateTime transform(java.lang.String value) {
            return value == null || "null".equals(value)
                    ? null
                            : DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseLocalDateTime(value);
        }

        public static org.joda.time.DateTime as(Object value) {
            return value != null && value instanceof java.lang.String
                    ? new yyyyMMddHHmmss().transform((java.lang.String) value)
                            : null;
        }
    }

    /**
     * Converts {@link java.lang.String}s to {@link org.joda.time.LocalDateTime}, but also recognizing the
     * keyword 'null'.
     */
    public static class LyyyyMMddHHmmss extends Transformer<org.joda.time.LocalDateTime> {

        @Override
        public org.joda.time.LocalDateTime transform(java.lang.String value) {
            return value == null || "null".equals(value)
                    ? null
                            : DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").parseLocalDateTime(value);
        }

        public static org.joda.time.DateTime as(Object value) {
            return value != null && value instanceof java.lang.String
                    ? new yyyyMMddHHmmss().transform((java.lang.String) value)
                            : null;
        }
    }


}