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
package org.apache.causeway.testing.unittestsupport.applib.dom.value;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assume.assumeThat;

/**
 * Contract test for value types ({@link #equals(Object) equals} and
 * {@link #hashCode() hashCode}), and also {@link Comparable#compareTo(Object) compareTo} for
 * any value types that also are {@link Comparable}
 *
 * @since 2.0 {@index}
 */
public abstract class ValueTypeContractTestAbstract<T> {

    @BeforeEach
    public void setUp() throws Exception {
        assertSizeAtLeast(getObjectsWithSameValue(), 2);
        assertSizeAtLeast(getObjectsWithDifferentValue(), 1);
    }

    private void assertSizeAtLeast(final List<T> objects, final int i) {
        assertThat(objects, is(notNullValue()));
        assertThat(objects.size(), is(greaterThan(i - 1)));
    }

    @Test
    public void notEqualToNull() throws Exception {
        for (final T o1 : getObjectsWithSameValue()) {
            assertThat(o1==null, is(false));
        }
        for (final T o1 : getObjectsWithDifferentValue()) {
            assertThat(o1==null, is(false));
        }
    }

    @Test
    public void reflexiveAndSymmetric() throws Exception {
        for (final T o1 : getObjectsWithSameValue()) {
            for (final T o2 : getObjectsWithSameValue()) {
                assertThat(o1.equals(o2), is(true));
                assertThat(o2.equals(o1), is(true));
                assertThat(o1.hashCode(), is(equalTo(o2.hashCode())));
            }
        }
    }

    @Test
    public void notEqual() throws Exception {
        for (final T o1 : getObjectsWithSameValue()) {
            for (final T o2 : getObjectsWithDifferentValue()) {
                assertThat(o1.equals(o2), is(false));
                assertThat(o2.equals(o1), is(false));
            }
        }
    }

    @Test
    public void transitiveWhenEqual() throws Exception {
        for (final T o1 : getObjectsWithSameValue()) {
            for (final T o2 : getObjectsWithSameValue()) {
                for (final Object o3 : getObjectsWithSameValue()) {
                    assertThat(o1.equals(o2), is(true));
                    assertThat(o2.equals(o3), is(true));
                    assertThat(o1.equals(o3), is(true));
                }
            }
        }
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void comparableEquivalence() throws Exception {
        for (final T o1 : getObjectsWithSameValue()) {
            assumeThat(o1 instanceof Comparable, is(true));
            Comparable c1 = (Comparable)o1;

            for (final T o2 : getObjectsWithSameValue()) {
                assumeThat(o2 instanceof Comparable, is(true));
                Comparable c2 = (Comparable)o2;

                assertThat(c1.compareTo(c2), is(0));
                assertThat(c2.compareTo(c1), is(0));
            }

            for (final T o2 : getObjectsWithDifferentValue()) {
                assumeThat(o2 instanceof Comparable, is(true));
                Comparable c2 = (Comparable)o2;

                final int x = c1.compareTo(c2);
                final int y = c2.compareTo(c1);
                assertThat(x, is(not(0)));

                assertThat(x, is(-y));
            }
        }
    }

    /**
     * Returns a list of different instances that are expected to have the same value (according to equals and hashCode).
     *
     * <p>
     *     At least two different instances must be provided.
     * </p>
     */
    protected abstract List<T> getObjectsWithSameValue();

    /**
     * Returns a list of instances that are different from each other and also from the instance(s) provided in the
     * {@link #getObjectsWithSameValue()} method.
     *
     * <p>
     *     It is often sufficient to provide a single instance (as it is compared against the instances provided by
     *     {@link #getObjectsWithSameValue()}).
     * </p>
     */
    protected abstract List<T> getObjectsWithDifferentValue();

}
