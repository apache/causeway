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
package org.apache.isis.testing.fixtures.applib.fixturescripts;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;

/**
 * Collecting parameter.
 *
 * <p>
 * Instantiate using {@link FixtureScripts#newExecutionContext(String)}
 */
public class FixtureResultList {

    private final FixtureScripts fixtureScripts;
    private final FixtureScript.ExecutionContext executionContext;

    // -- constructor

    FixtureResultList(
            final FixtureScripts fixtureScripts,
            final FixtureScript.ExecutionContext executionContext) {
        this.fixtureScripts = fixtureScripts;
        this.executionContext = executionContext;
    }

    // -- list of FixtureResults

    private final List<FixtureResult> list = _Lists.newArrayList();

    /**
     * Irrespective of the setting for {@link FixtureScripts#getMultipleExecutionStrategy()}, this list ensures
     * that any given fixture script instance is only executed once.
     *
     * <p>
     *     (The {@link FixtureScripts.MultipleExecutionStrategy} in contrast
     *     controls whether two instances of the same class can be executed).
     * </p>
     *
     * <p>
     *     REVIEW: I think this should probably be removed; it certainly does nothing if
     *     {@link FixtureScripts.MultipleExecutionStrategy#IGNORE} is set,
     *     and is arguably counter to the spirit of
     *     {@link FixtureScripts.MultipleExecutionStrategy#EXECUTE} being set.
     * </p>
     * {@link FixtureScript}s used to generate this result list.
     */
    private final List<FixtureScript> fixtureScriptList = _Lists.newArrayList();

    private final Map<String, FixtureResult> fixtureResultByKey = _Maps.newHashMap();

    public <T> T add(final FixtureScript script, final T object) {
        return add(script, nextItemFor(script), object);
    }

    /**
     * Wraps the object within a {@link FixtureResult} and add to this list.
     *
     * <P>
     *     If the object is not yet persisted, then the {@link FixtureScripts#getNonPersistedObjectsStrategy() FixtureScripts}'
     *     configuration will determine whether it is {@link FixtureScripts.NonPersistedObjectsStrategy#PERSIST eagerly persisted}
     *     or simply {@link FixtureScripts.NonPersistedObjectsStrategy#IGNORE ignored}.
     * </P>
     */
    public <T> T add(final FixtureScript fixtureScript, final String key, final T object) {
        final boolean firstTime = !fixtureScriptList.contains(fixtureScript);
        final FixtureResult fixtureResult = fixtureScripts.newFixtureResult(fixtureScript, key, object, firstTime);
        if(fixtureResult != null) {
            list.add(fixtureResult);
            fixtureResultByKey.put(fixtureResult.getKey(), fixtureResult);
            if(firstTime) {
                fixtureScriptList.add(fixtureScript);
            }
        }
        return object;
    }

    public List<FixtureResult> getResults() {
        return Collections.unmodifiableList(list);
    }



    // -- nextItemFor

    private final Map<FixtureScript, AtomicInteger> itemNumberByScript = _Maps.newHashMap();

    String nextItemFor(final FixtureScript script) {
        AtomicInteger atomicInteger = itemNumberByScript.get(script);
        if(atomicInteger == null) {
            atomicInteger = new AtomicInteger();
            itemNumberByScript.put(script, atomicInteger);
        }
        return "item-"+atomicInteger.incrementAndGet();
    }




    // -- lookup

    <T> T lookup(final String key, Class<T> cls) {
        final FixtureResult fixtureResult = fixtureResultByKey.get(key);
        if(fixtureResult == null) {
            return null;
        }
        final Object object = fixtureResult.getObject();
        if(object == null) {
            throw new IllegalStateException("Fixture result exists but has NULL object");
        }
        if (!cls.isAssignableFrom(object.getClass())) {
            throw new IllegalStateException(String.format("Fixture result exists and contains object but is of type %s, not %s", object.getClass().getName(), cls.getName()));
        }
        return _Casts.uncheckedCast(object);
    }


}