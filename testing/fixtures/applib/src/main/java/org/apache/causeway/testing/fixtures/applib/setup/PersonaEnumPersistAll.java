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
package org.apache.causeway.testing.fixtures.applib.setup;

import java.util.List;

import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScriptWithExecutionStrategy;
import org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScripts;
import org.apache.causeway.testing.fixtures.applib.personas.BuilderScriptAbstract;
import org.apache.causeway.testing.fixtures.applib.personas.PersonaWithBuilderScript;

/**
 * @since 2.x {@index}
 */
@Programmatic
public class PersonaEnumPersistAll<T, E extends Enum<E> & PersonaWithBuilderScript<T, B>, B extends BuilderScriptAbstract<T>>
        extends FixtureScript
        implements FixtureScriptWithExecutionStrategy {

    private final Class<E> personaEnumClass;

    public PersonaEnumPersistAll(final Class<E> personaEnumClass) {
        this.personaEnumClass = personaEnumClass;
    }

    /**
     * The number of objects to create.
     */
    private Integer number;
    public Integer getNumber() {
        return number;
    }

    public void setNumber(final Integer number) {
        this.number = number;
    }

    /**
     * The objects created by this fixture (output).
     */
    private final List<T> objects = _Lists.newArrayList();
    public List<T> getObjects() {
        return objects;
    }

    @Override
    protected void execute(final FixtureScript.ExecutionContext ec) {

        final E[] enumConstants = personaEnumClass.getEnumConstants();
        final int max = enumConstants.length;

        // defaults
        final int number = defaultParam("number", ec, max);

        // validate
        if(number < 0 || number > max) {
            throw new IllegalArgumentException(String.format("number must be in range [0,%d)", max));
        }

        for (int i = 0; i < number; i++) {
            final BuilderScriptAbstract<T> enumFixture = enumConstants[i].builder();
            final T domainObject = ec.executeChildT(this, enumFixture).getObject();
            ec.addResult(this, domainObject);
            objects.add(domainObject);
        }
    }

    @Override
    public FixtureScripts.MultipleExecutionStrategy getMultipleExecutionStrategy() {
        return FixtureScripts.MultipleExecutionStrategy.EXECUTE_ONCE_BY_VALUE;
    }

}

