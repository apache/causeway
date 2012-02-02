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
package org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.collections;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.ScenarioBoundValueException;
import org.apache.isis.viewer.bdd.common.ScenarioCell;
import org.apache.isis.viewer.bdd.common.fixtures.perform.PerformContext;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.AssertsContainment;

public class Containment extends ThatAbstract {

    private final AssertsContainment assertion;

    public Containment(final AssertsContainment assertion) {
        super(assertion.getKey());
        this.assertion = assertion;
    }

    @Override
    protected void doThat(final PerformContext performContext, final Iterable<ObjectAdapter> collection) throws ScenarioBoundValueException {

        final ObjectMember nakedObjectMember = performContext.getObjectMember();
        final CellBinding thatBinding = performContext.getPeer().getThatItBinding();
        final CellBinding arg0Binding = performContext.getPeer().getArg0Binding();

        if (!arg0Binding.isFound()) {
            throw ScenarioBoundValueException.current(thatBinding, "(requires argument)");
        }

        final ScenarioCell arg0Cell = arg0Binding.getCurrentCell();

        final ObjectAdapter containedAdapter = performContext.getPeer().getAdapter(null, nakedObjectMember.getSpecification(), arg0Binding, arg0Cell);

        boolean contains = false;
        for (final ObjectAdapter eachAdapter : collection) {
            if (containedAdapter == eachAdapter) {
                contains = true;
                break;
            }
        }

        if (!assertion.isSatisfiedBy(contains)) {
            throw ScenarioBoundValueException.current(arg0Binding, assertion.getErrorMsgIfNotSatisfied());
        }

    }

}
