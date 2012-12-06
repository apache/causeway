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
import static org.apache.isis.core.metamodel.adapter.ResolveState.RESOLVED;
import static org.apache.isis.core.metamodel.adapter.ResolveState.RESOLVING;
import static org.apache.isis.core.metamodel.adapter.ResolveState.TRANSIENT;
import static org.apache.isis.core.metamodel.adapter.ResolveState.UPDATING;
import static org.apache.isis.core.metamodel.adapter.ResolveState.VALUE;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ResolveState_GetEndStateTest {

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { { NEW, null }, //
                { GHOST, null }, //
                { TRANSIENT, null }, //
                // { RESOLVING_PART, PART_RESOLVED },
                // { PART_RESOLVED, null },
                { RESOLVING, RESOLVED }, //
                { RESOLVED, null }, //
                { UPDATING, RESOLVED }, //
                // { SERIALIZING_TRANSIENT, TRANSIENT },
                // { SERIALIZING_PART_RESOLVED, PART_RESOLVED },
                // { SERIALIZING_GHOST, GHOST },
                // { SERIALIZING_RESOLVED, RESOLVED },
                { VALUE, null }, //
                { DESTROYED, null }, //
        });
    }

    private final ResolveState from;
    private final ResolveState to;

    public ResolveState_GetEndStateTest(final ResolveState from, final ResolveState to) {
        this.from = from;
        this.to = to;
    }

    @Test
    public void testGetEndState() {
        assertThat(from.getEndState(), is(to));
    }

}
