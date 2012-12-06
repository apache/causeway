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
public class ResolveState_IsValidToChangeToTest {

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { { true, NEW, GHOST },//
                { false, NEW, NEW },//
                // { false, NEW, PART_RESOLVED },
                { false, NEW, RESOLVED },//
                { false, NEW, RESOLVING },//
                // { false, NEW, RESOLVING_PART },//
                { true, NEW, TRANSIENT },//
                { false, NEW, DESTROYED },//
                { false, NEW, UPDATING },//
                // { false, NEW, SERIALIZING_TRANSIENT },
                // { false, NEW, SERIALIZING_GHOST },
                // { false, NEW, SERIALIZING_PART_RESOLVED },
                // { false, NEW, SERIALIZING_RESOLVED },//
                { true, NEW, VALUE },//

                { false, GHOST, GHOST },//
                { false, GHOST, NEW },//
                // { false, GHOST, PART_RESOLVED },
                { false, GHOST, RESOLVED },//
                { true, GHOST, RESOLVING },//
                // { true, GHOST, RESOLVING_PART },//
                { false, GHOST, TRANSIENT },//
                { true, GHOST, DESTROYED },//
                { true, GHOST, UPDATING },//
                // { false, GHOST, SERIALIZING_TRANSIENT },
                // { true, GHOST, SERIALIZING_GHOST },
                // { false, GHOST, SERIALIZING_PART_RESOLVED },
                // { false, GHOST, SERIALIZING_RESOLVED }, //
                { false, GHOST, VALUE },//

                { false, TRANSIENT, GHOST },//
                { false, TRANSIENT, NEW },//
                // { false, TRANSIENT, PART_RESOLVED },
                { true, TRANSIENT, RESOLVED },//
                { false, TRANSIENT, RESOLVING },//
                // { false, TRANSIENT, RESOLVING_PART },
                { false, TRANSIENT, TRANSIENT },//
                { false, TRANSIENT, DESTROYED },//
                { false, TRANSIENT, UPDATING },//
                // { true, TRANSIENT, SERIALIZING_TRANSIENT },
                // { false, TRANSIENT, SERIALIZING_GHOST },
                // { false, TRANSIENT, SERIALIZING_PART_RESOLVED },
                // { false, TRANSIENT, SERIALIZING_RESOLVED },//
                { false, TRANSIENT, VALUE },//

                // { false, RESOLVING_PART, GHOST }, //
                // { false, RESOLVING_PART, NEW },//
                // { true, RESOLVING_PART, PART_RESOLVED },//
                // { true, RESOLVING_PART, RESOLVED },//
                // { false, RESOLVING_PART, RESOLVING },//
                // { false, RESOLVING_PART, RESOLVING_PART },
                // { false, RESOLVING_PART, TRANSIENT },//
                // { false, RESOLVING_PART, DESTROYED },//
                // { false, RESOLVING_PART, UPDATING },//
                // { false, RESOLVING_PART, SERIALIZING_TRANSIENT },//
                // { false, RESOLVING_PART, SERIALIZING_GHOST },
                // { false, RESOLVING_PART, SERIALIZING_PART_RESOLVED },
                // { false, RESOLVING_PART, SERIALIZING_RESOLVED }, //
                // { false, RESOLVING_PART, VALUE },//

                // { false, PART_RESOLVED, GHOST }, //
                // { false, PART_RESOLVED, NEW }, //
                // { false, PART_RESOLVED, PART_RESOLVED }, //
                // { false, PART_RESOLVED, RESOLVED },//
                // { true, PART_RESOLVED, RESOLVING },//
                // // { true, PART_RESOLVED, RESOLVING_PART },//
                // { false, PART_RESOLVED, TRANSIENT },//
                // { true, PART_RESOLVED, DESTROYED },//
                // { true, PART_RESOLVED, UPDATING },//
                // { false, PART_RESOLVED, SERIALIZING_TRANSIENT },//
                // // { false, PART_RESOLVED, SERIALIZING_GHOST },
                // // { true, PART_RESOLVED, SERIALIZING_PART_RESOLVED },
                // { false, PART_RESOLVED, SERIALIZING_RESOLVED }, //
                // { false, PART_RESOLVED, VALUE },//

                { false, RESOLVING, GHOST }, //
                { false, RESOLVING, NEW }, //
                // { false, RESOLVING, PART_RESOLVED }, //
                { true, RESOLVING, RESOLVED },//
                { false, RESOLVING, RESOLVING },//
                // { false, RESOLVING, RESOLVING_PART },//
                { false, RESOLVING, TRANSIENT },//
                { false, RESOLVING, DESTROYED },//
                { false, RESOLVING, UPDATING },//
                // { false, RESOLVING, SERIALIZING_TRANSIENT },
                // { false, RESOLVING, SERIALIZING_GHOST },
                // { false, RESOLVING, SERIALIZING_PART_RESOLVED },
                // { false, RESOLVING, SERIALIZING_RESOLVED },//
                { false, RESOLVING, VALUE },//

                { true, RESOLVED, GHOST }, //
                { false, RESOLVED, NEW }, //
                // { false, RESOLVED, PART_RESOLVED }, //
                { false, RESOLVED, RESOLVED }, //
                { false, RESOLVED, RESOLVING }, //
                // { false, RESOLVED, RESOLVING_PART }, //
                { false, RESOLVED, TRANSIENT },//
                { true, RESOLVED, DESTROYED },//
                { true, RESOLVED, UPDATING },//
                // { false, RESOLVED, SERIALIZING_TRANSIENT },
                // { false, RESOLVED, SERIALIZING_GHOST },
                // { false, RESOLVED, SERIALIZING_PART_RESOLVED },
                // { true, RESOLVED, SERIALIZING_RESOLVED }, //
                { false, RESOLVED, VALUE },

                { false, UPDATING, GHOST }, //
                { false, UPDATING, NEW }, //
                // { false, UPDATING, PART_RESOLVED }, //
                { true, UPDATING, RESOLVED }, //
                { false, UPDATING, RESOLVING }, //
                // { false, UPDATING, RESOLVING_PART }, //
                { false, UPDATING, TRANSIENT },//
                { false, UPDATING, DESTROYED },//
                { false, UPDATING, UPDATING },//
                // { false, UPDATING, SERIALIZING_TRANSIENT },
                // { false, UPDATING, SERIALIZING_GHOST },
                // { false, UPDATING, SERIALIZING_PART_RESOLVED },
                // { false, UPDATING, SERIALIZING_RESOLVED }, //
                { false, UPDATING, VALUE },//

                // { false, SERIALIZING_TRANSIENT, GHOST },//
                // { false, SERIALIZING_TRANSIENT, NEW }, //
                // // { false, SERIALIZING_TRANSIENT, PART_RESOLVED }, //
                // { false, SERIALIZING_TRANSIENT, RESOLVED }, //
                // { false, SERIALIZING_TRANSIENT, RESOLVING }, //
                // // { false, SERIALIZING_TRANSIENT, RESOLVING_PART },
                // { true, SERIALIZING_TRANSIENT, TRANSIENT }, //
                // { false, SERIALIZING_TRANSIENT, DESTROYED }, //
                // { false, SERIALIZING_TRANSIENT, UPDATING }, //
                // { false, SERIALIZING_TRANSIENT, SERIALIZING_TRANSIENT },//
                // // { false, SERIALIZING_TRANSIENT, SERIALIZING_GHOST },
                // // { false, SERIALIZING_TRANSIENT, SERIALIZING_PART_RESOLVED
                // },
                // // { false, SERIALIZING_TRANSIENT, SERIALIZING_RESOLVED }, //
                // { false, SERIALIZING_TRANSIENT, VALUE },//

                // { false, SERIALIZING_PART_RESOLVED, GHOST }, { false,
                // SERIALIZING_PART_RESOLVED, NEW }, { true,
                // SERIALIZING_PART_RESOLVED, PART_RESOLVED }, { false,
                // SERIALIZING_PART_RESOLVED, RESOLVED }, { false,
                // SERIALIZING_PART_RESOLVED, RESOLVING },
                // { false, SERIALIZING_PART_RESOLVED, RESOLVING_PART }, {
                // false, SERIALIZING_PART_RESOLVED, TRANSIENT }, { false,
                // SERIALIZING_PART_RESOLVED, DESTROYED }, { false,
                // SERIALIZING_PART_RESOLVED, UPDATING }, { false,
                // SERIALIZING_PART_RESOLVED, SERIALIZING_TRANSIENT },
                // { false, SERIALIZING_PART_RESOLVED, SERIALIZING_GHOST }, {
                // false, SERIALIZING_PART_RESOLVED, SERIALIZING_PART_RESOLVED
                // }, { false, SERIALIZING_PART_RESOLVED, SERIALIZING_RESOLVED
                // }, { false, SERIALIZING_PART_RESOLVED, VALUE },

                // { false, SERIALIZING_RESOLVED, GHOST }, //
                // { false, SERIALIZING_RESOLVED, NEW }, //
                // // { false, SERIALIZING_RESOLVED, PART_RESOLVED }, //
                // { true, SERIALIZING_RESOLVED, RESOLVED }, //
                // { false, SERIALIZING_RESOLVED, RESOLVING }, //
                // // { false, SERIALIZING_RESOLVED, RESOLVING_PART }, //
                // { false, SERIALIZING_RESOLVED, TRANSIENT }, //
                // { false, SERIALIZING_RESOLVED, DESTROYED }, //
                // { false, SERIALIZING_RESOLVED, UPDATING }, //
                // { false, SERIALIZING_RESOLVED, SERIALIZING_TRANSIENT },
                // // { false, SERIALIZING_RESOLVED, SERIALIZING_GHOST },
                // // { false, SERIALIZING_RESOLVED, SERIALIZING_PART_RESOLVED
                // },
                // { false, SERIALIZING_RESOLVED, SERIALIZING_RESOLVED }, //
                // { false, SERIALIZING_RESOLVED, VALUE },

                // { true, SERIALIZING_GHOST, GHOST }, { false,
                // SERIALIZING_GHOST, NEW }, { false, SERIALIZING_GHOST,
                // PART_RESOLVED }, { false, SERIALIZING_GHOST, RESOLVED }, {
                // false, SERIALIZING_GHOST, RESOLVING }, { false,
                // SERIALIZING_GHOST, RESOLVING_PART }, { false,
                // SERIALIZING_GHOST, TRANSIENT },
                // { false, SERIALIZING_GHOST, DESTROYED }, { false,
                // SERIALIZING_GHOST, UPDATING }, { false, SERIALIZING_GHOST,
                // SERIALIZING_TRANSIENT }, { false, SERIALIZING_GHOST,
                // SERIALIZING_GHOST }, { false, SERIALIZING_GHOST,
                // SERIALIZING_PART_RESOLVED },
                // { false, SERIALIZING_GHOST, SERIALIZING_RESOLVED }, { false,
                // SERIALIZING_GHOST, VALUE },

                { false, VALUE, GHOST }, //
                { false, VALUE, NEW }, //
                // { false, VALUE, PART_RESOLVED }, //
                { false, VALUE, RESOLVED }, //
                { false, VALUE, RESOLVING }, //
                // { false, VALUE, RESOLVING_PART }, //
                { false, VALUE, TRANSIENT }, //
                { false, VALUE, DESTROYED }, //
                { false, VALUE, UPDATING }, //
                // { false, VALUE, SERIALIZING_TRANSIENT },
                // { false, VALUE, SERIALIZING_GHOST },
                // { false, VALUE, SERIALIZING_PART_RESOLVED },
                // { false, VALUE, SERIALIZING_RESOLVED }, //
                { false, VALUE, VALUE },

                { false, DESTROYED, GHOST }, //
                { false, DESTROYED, NEW }, //
                // { false, DESTROYED, PART_RESOLVED }, //
                { false, DESTROYED, RESOLVED }, //
                { false, DESTROYED, RESOLVING }, //
                // { false, DESTROYED, RESOLVING_PART }, //
                { false, DESTROYED, TRANSIENT }, //
                { false, DESTROYED, DESTROYED }, //
                { false, DESTROYED, UPDATING }, //
                // { false, DESTROYED, SERIALIZING_TRANSIENT },
                // { false, DESTROYED, SERIALIZING_GHOST },
                // { false, DESTROYED, SERIALIZING_PART_RESOLVED },
                // { false, DESTROYED, SERIALIZING_RESOLVED }, //
                { false, DESTROYED, VALUE },

        });
    }

    private final boolean whetherValidToChangeTo;
    private final ResolveState from;
    private final ResolveState to;

    public ResolveState_IsValidToChangeToTest(final boolean whetherValidToChangeTo, final ResolveState from, final ResolveState to) {
        this.whetherValidToChangeTo = whetherValidToChangeTo;
        this.from = from;
        this.to = to;
    }

    @Test
    public void testIsValidToChange() {
        assertThat(from.isValidToChangeTo(to), is(whetherValidToChangeTo));
    }

}
