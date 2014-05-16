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
package org.apache.isis.applib.fixturescripts;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Collecting parameter.
 *
 * <p>
 * Instantiate using {@link FixtureScripts#newExecutionContext(String)}
 */
public class FixtureResultList {

    private final FixtureScripts fixtureScripts;

    FixtureResultList(FixtureScripts fixtureScripts) {
        this.fixtureScripts = fixtureScripts;
    }

    // //////////////////////////////////////

    private final List<FixtureResult> list = Lists.newArrayList();

    public <T> T add(final FixtureScript script, final T object) {
        return add(script, nextItemFor(script), object);
    }

    /**
     * Wraps the object within a {@link org.apache.isis.applib.fixturescripts.FixtureResult} and add to this list.
     *
     * <P>
     *     If the object is not yet persisted, then the {@link FixtureScripts#getNonPersistedObjectsStrategy() FixtureScripts}'
     *     configuration will determine whether it is {@link org.apache.isis.applib.fixturescripts.FixtureScripts.NonPersistedObjectsStrategy#PERSIST eagerly persisted}
     *     or simply {@link org.apache.isis.applib.fixturescripts.FixtureScripts.NonPersistedObjectsStrategy#IGNORE ignored}.
     * </P>
     */
    public <T> T add(final FixtureScript script, final String key, final T object) {
        final FixtureResult fr = fixtureScripts.newFixtureResult(script, key, object);
        if(fr != null) {
            list.add(fr);
        }
        return object;
    }



    public List<FixtureResult> getResults() {
        return list;
    }

    // //////////////////////////////////////

    private final Map<FixtureScript, AtomicInteger> itemNumberByScript = Maps.newHashMap();

    String nextItemFor(final FixtureScript script) {
        AtomicInteger atomicInteger = itemNumberByScript.get(script);
        if(atomicInteger == null) {
            atomicInteger = new AtomicInteger();
            itemNumberByScript.put(script, atomicInteger);
        }
        return "item-"+atomicInteger.incrementAndGet();
    }

}