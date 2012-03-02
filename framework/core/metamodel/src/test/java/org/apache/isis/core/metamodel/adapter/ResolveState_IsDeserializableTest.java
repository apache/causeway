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

package org.apache.isis.core.metamodel.adapter;

import static org.apache.isis.core.metamodel.adapter.ResolveState.DESTROYED;
import static org.apache.isis.core.metamodel.adapter.ResolveState.GHOST;
import static org.apache.isis.core.metamodel.adapter.ResolveState.NEW;
import static org.apache.isis.core.metamodel.adapter.ResolveState.PART_RESOLVED;
import static org.apache.isis.core.metamodel.adapter.ResolveState.RESOLVED;
import static org.apache.isis.core.metamodel.adapter.ResolveState.RESOLVING;
import static org.apache.isis.core.metamodel.adapter.ResolveState.RESOLVING_PART;
import static org.apache.isis.core.metamodel.adapter.ResolveState.SERIALIZING_GHOST;
import static org.apache.isis.core.metamodel.adapter.ResolveState.SERIALIZING_PART_RESOLVED;
import static org.apache.isis.core.metamodel.adapter.ResolveState.SERIALIZING_RESOLVED;
import static org.apache.isis.core.metamodel.adapter.ResolveState.SERIALIZING_TRANSIENT;
import static org.apache.isis.core.metamodel.adapter.ResolveState.TRANSIENT;
import static org.apache.isis.core.metamodel.adapter.ResolveState.UPDATING;
import static org.apache.isis.core.metamodel.adapter.ResolveState.VALUE;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ResolveState_IsDeserializableTest {

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { { null, null, NEW, GHOST }, { null, null, NEW, NEW }, { null, null, NEW, PART_RESOLVED }, { null, null, NEW, RESOLVED }, { false, false, NEW, RESOLVING }, { false, false, NEW, RESOLVING_PART }, { null, null, NEW, TRANSIENT },
                { null, null, NEW, DESTROYED }, { false, false, NEW, UPDATING }, { null, false, NEW, SERIALIZING_TRANSIENT }, { null, null, NEW, SERIALIZING_GHOST }, { null, null, NEW, SERIALIZING_PART_RESOLVED }, { null, null, NEW, SERIALIZING_RESOLVED }, { null, null, NEW, VALUE },

                { null, null, GHOST, GHOST }, { null, null, GHOST, NEW }, { null, null, GHOST, PART_RESOLVED }, { null, null, GHOST, RESOLVED }, { true, true, GHOST, RESOLVING }, { true, true, GHOST, RESOLVING_PART }, { null, null, GHOST, TRANSIENT }, { null, null, GHOST, DESTROYED },
                { true, true, GHOST, UPDATING }, { null, false, GHOST, SERIALIZING_TRANSIENT }, { null, null, GHOST, SERIALIZING_GHOST }, { null, null, GHOST, SERIALIZING_PART_RESOLVED }, { null, null, GHOST, SERIALIZING_RESOLVED }, { null, null, GHOST, VALUE },

                { null, null, TRANSIENT, GHOST }, { null, null, TRANSIENT, NEW }, { null, null, TRANSIENT, PART_RESOLVED }, { null, null, TRANSIENT, RESOLVED }, { false, false, TRANSIENT, RESOLVING }, { false, false, TRANSIENT, RESOLVING_PART }, { null, null, TRANSIENT, TRANSIENT },
                { null, null, TRANSIENT, DESTROYED }, { false, false, TRANSIENT, UPDATING }, { null, true, TRANSIENT, SERIALIZING_TRANSIENT }, { null, null, TRANSIENT, SERIALIZING_GHOST }, { null, null, TRANSIENT, SERIALIZING_PART_RESOLVED }, { null, null, TRANSIENT, SERIALIZING_RESOLVED },
                { null, null, TRANSIENT, VALUE },

                { null, null, RESOLVING_PART, GHOST }, { null, null, RESOLVING_PART, NEW }, { null, null, RESOLVING_PART, PART_RESOLVED }, { null, null, RESOLVING_PART, RESOLVED }, { false, false, RESOLVING_PART, RESOLVING }, { false, false, RESOLVING_PART, RESOLVING_PART },
                { null, null, RESOLVING_PART, TRANSIENT }, { null, null, RESOLVING_PART, DESTROYED }, { false, false, RESOLVING_PART, UPDATING }, { null, false, RESOLVING_PART, SERIALIZING_TRANSIENT }, { null, null, RESOLVING_PART, SERIALIZING_GHOST },
                { null, null, RESOLVING_PART, SERIALIZING_PART_RESOLVED }, { null, null, RESOLVING_PART, SERIALIZING_RESOLVED }, { null, null, RESOLVING_PART, VALUE },

                { null, null, PART_RESOLVED, GHOST }, { null, null, PART_RESOLVED, NEW }, { null, null, PART_RESOLVED, PART_RESOLVED }, { null, null, PART_RESOLVED, RESOLVED }, { true, true, PART_RESOLVED, RESOLVING }, { true, true, PART_RESOLVED, RESOLVING_PART },
                { null, null, PART_RESOLVED, TRANSIENT }, { null, null, PART_RESOLVED, DESTROYED }, { true, true, PART_RESOLVED, UPDATING }, { null, false, PART_RESOLVED, SERIALIZING_TRANSIENT }, { null, null, PART_RESOLVED, SERIALIZING_GHOST },
                { null, null, PART_RESOLVED, SERIALIZING_PART_RESOLVED }, { null, null, PART_RESOLVED, SERIALIZING_RESOLVED }, { null, null, PART_RESOLVED, VALUE },

                { null, null, RESOLVING, GHOST }, { null, null, RESOLVING, NEW }, { null, null, RESOLVING, PART_RESOLVED }, { null, null, RESOLVING, RESOLVED }, { false, false, RESOLVING, RESOLVING }, { false, false, RESOLVING, RESOLVING_PART }, { null, null, RESOLVING, TRANSIENT },
                { null, null, RESOLVING, DESTROYED }, { false, false, RESOLVING, UPDATING }, { null, false, RESOLVING, SERIALIZING_TRANSIENT }, { null, null, RESOLVING, SERIALIZING_GHOST }, { null, null, RESOLVING, SERIALIZING_PART_RESOLVED }, { null, null, RESOLVING, SERIALIZING_RESOLVED },
                { null, null, RESOLVING, VALUE },

                { null, null, RESOLVED, GHOST }, { null, null, RESOLVED, NEW }, { null, null, RESOLVED, PART_RESOLVED }, { null, null, RESOLVED, RESOLVED }, { false, false, RESOLVED, RESOLVING }, { false, false, RESOLVED, RESOLVING_PART }, { null, null, RESOLVED, TRANSIENT },
                { null, null, RESOLVED, DESTROYED }, { true, true, RESOLVED, UPDATING }, { null, false, RESOLVED, SERIALIZING_TRANSIENT }, { null, null, RESOLVED, SERIALIZING_GHOST }, { null, null, RESOLVED, SERIALIZING_PART_RESOLVED }, { null, null, RESOLVED, SERIALIZING_RESOLVED },
                { null, null, RESOLVED, VALUE },

                { null, null, UPDATING, GHOST }, { null, null, UPDATING, NEW }, { null, null, UPDATING, PART_RESOLVED }, { null, null, UPDATING, RESOLVED }, { false, false, UPDATING, RESOLVING }, { false, false, UPDATING, RESOLVING_PART }, { null, null, UPDATING, TRANSIENT },
                { null, null, UPDATING, DESTROYED }, { false, false, UPDATING, UPDATING }, { null, false, UPDATING, SERIALIZING_TRANSIENT }, { null, null, UPDATING, SERIALIZING_GHOST }, { null, null, UPDATING, SERIALIZING_PART_RESOLVED }, { null, null, UPDATING, SERIALIZING_RESOLVED },
                { null, null, UPDATING, VALUE },

                { null, null, SERIALIZING_TRANSIENT, GHOST }, { null, null, SERIALIZING_TRANSIENT, NEW }, { null, null, SERIALIZING_TRANSIENT, PART_RESOLVED }, { null, null, SERIALIZING_TRANSIENT, RESOLVED }, { false, false, SERIALIZING_TRANSIENT, RESOLVING },
                { false, false, SERIALIZING_TRANSIENT, RESOLVING_PART }, { null, null, SERIALIZING_TRANSIENT, TRANSIENT }, { null, null, SERIALIZING_TRANSIENT, DESTROYED }, { false, false, SERIALIZING_TRANSIENT, UPDATING }, { null, false, SERIALIZING_TRANSIENT, SERIALIZING_TRANSIENT },
                { null, null, SERIALIZING_TRANSIENT, SERIALIZING_GHOST }, { null, null, SERIALIZING_TRANSIENT, SERIALIZING_PART_RESOLVED }, { null, null, SERIALIZING_TRANSIENT, SERIALIZING_RESOLVED }, { null, null, SERIALIZING_TRANSIENT, VALUE },

                { null, null, SERIALIZING_PART_RESOLVED, GHOST }, { null, null, SERIALIZING_PART_RESOLVED, NEW }, { null, null, SERIALIZING_PART_RESOLVED, PART_RESOLVED }, { null, null, SERIALIZING_PART_RESOLVED, RESOLVED }, { false, false, SERIALIZING_PART_RESOLVED, RESOLVING },
                { false, false, SERIALIZING_PART_RESOLVED, RESOLVING_PART }, { null, null, SERIALIZING_PART_RESOLVED, TRANSIENT }, { null, null, SERIALIZING_PART_RESOLVED, DESTROYED }, { false, false, SERIALIZING_PART_RESOLVED, UPDATING },
                { null, false, SERIALIZING_PART_RESOLVED, SERIALIZING_TRANSIENT }, { null, null, SERIALIZING_PART_RESOLVED, SERIALIZING_GHOST }, { null, null, SERIALIZING_PART_RESOLVED, SERIALIZING_PART_RESOLVED }, { null, null, SERIALIZING_PART_RESOLVED, SERIALIZING_RESOLVED },
                { null, null, SERIALIZING_PART_RESOLVED, VALUE },

                { null, null, SERIALIZING_RESOLVED, GHOST }, { null, null, SERIALIZING_RESOLVED, NEW }, { null, null, SERIALIZING_RESOLVED, PART_RESOLVED }, { null, null, SERIALIZING_RESOLVED, RESOLVED }, { false, false, SERIALIZING_RESOLVED, RESOLVING },
                { false, false, SERIALIZING_RESOLVED, RESOLVING_PART }, { null, null, SERIALIZING_RESOLVED, TRANSIENT }, { null, null, SERIALIZING_RESOLVED, DESTROYED }, { false, false, SERIALIZING_RESOLVED, UPDATING }, { null, false, SERIALIZING_RESOLVED, SERIALIZING_TRANSIENT },
                { null, null, SERIALIZING_RESOLVED, SERIALIZING_GHOST }, { null, null, SERIALIZING_RESOLVED, SERIALIZING_PART_RESOLVED }, { null, null, SERIALIZING_RESOLVED, SERIALIZING_RESOLVED }, { null, null, SERIALIZING_RESOLVED, VALUE },

                { null, null, SERIALIZING_GHOST, GHOST }, { null, null, SERIALIZING_GHOST, NEW }, { null, null, SERIALIZING_GHOST, PART_RESOLVED }, { null, null, SERIALIZING_GHOST, RESOLVED }, { false, false, SERIALIZING_GHOST, RESOLVING }, { false, false, SERIALIZING_GHOST, RESOLVING_PART },
                { null, null, SERIALIZING_GHOST, TRANSIENT }, { null, null, SERIALIZING_GHOST, DESTROYED }, { false, false, SERIALIZING_GHOST, UPDATING }, { null, false, SERIALIZING_GHOST, SERIALIZING_TRANSIENT }, { null, null, SERIALIZING_GHOST, SERIALIZING_GHOST },
                { null, null, SERIALIZING_GHOST, SERIALIZING_PART_RESOLVED }, { null, null, SERIALIZING_GHOST, SERIALIZING_RESOLVED }, { null, null, SERIALIZING_GHOST, VALUE },

                { null, null, VALUE, GHOST }, { null, null, VALUE, NEW }, { null, null, VALUE, PART_RESOLVED }, { null, null, VALUE, RESOLVED }, { false, false, VALUE, RESOLVING }, { false, false, VALUE, RESOLVING_PART }, { null, null, VALUE, TRANSIENT }, { null, null, VALUE, DESTROYED },
                { false, false, VALUE, UPDATING }, { null, false, VALUE, SERIALIZING_TRANSIENT }, { null, null, VALUE, SERIALIZING_GHOST }, { null, null, VALUE, SERIALIZING_PART_RESOLVED }, { null, null, VALUE, SERIALIZING_RESOLVED }, { null, null, VALUE, VALUE },

                { null, null, DESTROYED, GHOST }, { null, null, DESTROYED, NEW }, { null, null, DESTROYED, PART_RESOLVED }, { null, null, DESTROYED, RESOLVED }, { false, false, DESTROYED, RESOLVING }, { false, false, DESTROYED, RESOLVING_PART }, { null, null, DESTROYED, TRANSIENT },
                { null, null, DESTROYED, DESTROYED }, { false, false, DESTROYED, UPDATING }, { null, false, DESTROYED, SERIALIZING_TRANSIENT }, { null, null, DESTROYED, SERIALIZING_GHOST }, { null, null, DESTROYED, SERIALIZING_PART_RESOLVED }, { null, null, DESTROYED, SERIALIZING_RESOLVED },
                { null, null, DESTROYED, VALUE },

        });
    }

    private final Boolean whetherIsDeserializable;
    private final ResolveState from;
    private final ResolveState to;

    public ResolveState_IsDeserializableTest(final Boolean whetherIsResolvable, final Boolean whetherIsDeserializable, final ResolveState from, final ResolveState to) {
        this.whetherIsDeserializable = whetherIsDeserializable;
        this.from = from;
        this.to = to;
    }

    @Test
    public void testIsDeserialiable() {
        if (whetherIsDeserializable != null) {
            assertThat(from.isDeserializable(to), is(whetherIsDeserializable));
        } else {
            try {
                from.isDeserializable(to);
                fail();
            } catch (final ResolveException ex) {
                // expected
            }
        }
    }

}
