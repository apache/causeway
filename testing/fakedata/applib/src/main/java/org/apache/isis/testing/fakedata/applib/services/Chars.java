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

/**
 * Returns random <code>char</code> values, optionally constrained within a range,
 *
 * @since 2.0 {@index}
 */
public class Chars extends AbstractRandomValueGenerator {

    public Chars(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    /**
     * Returns a random upper case characters, between 'A' and 'Z'.
     */
    public char upper() {
        return anyOf("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    /**
     * Returns a random lower case characters, between 'a' and 'z'.
     */
    public char lower() {
        return anyOf("abcdefghijklmonpqrstuvwxyz");
    }

    /**
     * Returns a digit character, between '0' and '9'.
     */
    public char digit() {
        return anyOf("0123456789");
    }

    /**
     * Returns any single character within the provided string.
     */
    public char anyOf(final String s) {
        final char[] chars = s.toCharArray();
        return fake.collections().anyOf(chars);
    }

    /**
     * Returns any character at random.
     */
    public char any() {
        final int any = fake.shorts().any();
        final int i = any - Short.MIN_VALUE;
        return (char) i;
    }


}
