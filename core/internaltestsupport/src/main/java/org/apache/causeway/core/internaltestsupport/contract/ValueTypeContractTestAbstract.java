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
package org.apache.causeway.core.internaltestsupport.contract;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Contract test for value types ({@link #equals(Object) equals} and
 * {@link #hashCode() hashCode}), and also {@link Comparable#compareTo(Object) compareTo} for
 * any value types that also are {@link Comparable}
 *
 * <p>
 *     Used by core and domain apps only.
 * </p>
 */
public abstract class ValueTypeContractTestAbstract<T> {

    @BeforeEach
    public void setUp() throws Exception {
        assertSizeAtLeast(getObjectsWithSameValue(), 2);
        assertSizeAtLeast(getObjectsWithDifferentValue(), 1);
    }

    private void assertSizeAtLeast(final List<T> objects, final int i) {
        assertThat(objects).isNotNull();
        assertThat(objects).hasSizeGreaterThan(i - 1);
    }

    @Test
    public void notEqualToNull() throws Exception {
        for (final T o1 : getObjectsWithSameValue()) {
            assertThat(o1).isNotNull();
        }
        for (final T o1 : getObjectsWithDifferentValue()) {
            assertThat(o1).isNotNull();
        }
    }

    @Test
    public void reflexiveAndSymmetric() throws Exception {
        for (final T o1 : getObjectsWithSameValue()) {
            for (final T o2 : getObjectsWithSameValue()) {
                assertThat(o1).isEqualTo(o2);
                assertThat(o2).isEqualTo(o1);
                assertThat(o1.hashCode()).isEqualTo(o2.hashCode());
            }
        }
    }

    @Test
    public void notEqual() throws Exception {
        for (final T o1 : getObjectsWithSameValue()) {
            for (final T o2 : getObjectsWithDifferentValue()) {
                assertThat(o1).isNotEqualTo(o2);
                assertThat(o2).isNotEqualTo(o1);
            }
        }
    }

    @Test
    public void transitiveWhenEqual() throws Exception {
        for (final T o1 : getObjectsWithSameValue()) {
            for (final T o2 : getObjectsWithSameValue()) {
                for (final Object o3 : getObjectsWithSameValue()) {
                    assertThat(o1).isEqualTo(o2);
                    assertThat(o2).isEqualTo(o3);
                    assertThat(o3).isEqualTo(o1);
                }
            }
        }
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void comparableEquivalence() throws Exception {
        for (final T o1 : getObjectsWithSameValue()) {
            if(! (o1 instanceof Comparable)) continue;
            Comparable c1 = (Comparable)o1;

            for (final T o2 : getObjectsWithSameValue()) {
                if(! (o2 instanceof Comparable)) continue;
                Comparable c2 = (Comparable)o2;

                assertThat(c1).isEqualByComparingTo(c2);
                assertThat(c2).isEqualByComparingTo(c1);
            }

            for (final T o2 : getObjectsWithDifferentValue()) {
                if(! (o2 instanceof Comparable)) continue;
                Comparable c2 = (Comparable)o2;

                final int x = c1.compareTo(c2);
                final int y = c2.compareTo(c1);
                assertThat(x).isNotZero();

                assertThat(x).isEqualTo(-y);
            }
        }
    }

    protected abstract List<T> getObjectsWithSameValue();

    protected abstract List<T> getObjectsWithDifferentValue();

}
