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
package org.apache.isis.subdomains.base.applib.with;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.testing.unittestsupport.applib.core.comparable.ComparableContractTester;


public class ComparableByNameContractTester<T extends WithNameComparable<T>> {
    protected final Class<T> cls;

    public ComparableByNameContractTester(Class<T> cls) {
        this.cls = cls;
    }

    public static <E> List<E> listOf(E... elements) {
        return _Lists.of(elements);
    }

    public void test() {
        System.out.println("ComparableByNameContractTester: " + cls.getName());
        new ComparableContractTester<>(orderedTuples()).test();

        testToString();

    }

    protected void testToString() {
        final String str = "ABC";

        final T withName = newWithName(str);
        String expectedToString = "";//MoreObjects.toStringHelper(withName).add("name", "ABC").toString();

        assertThat(withName.toString(), is(expectedToString));
    }

    @SuppressWarnings("unchecked")
    protected List<List<T>> orderedTuples() {
        return listOf(
                listOf(
                        newWithName(null),
                        newWithName("ABC"),
                        newWithName("ABC"),
                        newWithName("DEF")));
    }

    private T newWithName(String reference) {
        final T wr = newWithName();
        wr.setName(reference);
        return wr;
    }

    private T newWithName() {
        try {
            return cls.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
