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
package org.apache.isis.viewer.bdd.concordion.internal.fixtures;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.viewer.bdd.common.AliasRegistry;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.CellBindingDefault;
import org.apache.isis.viewer.bdd.common.IsisViewerConstants;
import org.apache.isis.viewer.bdd.common.ScenarioBoundValueException;
import org.apache.isis.viewer.bdd.common.ScenarioCell;
import org.apache.isis.viewer.bdd.common.ScenarioCellDefault;
import org.apache.isis.viewer.bdd.common.fixtures.UsingIsisViewerPeer;
import org.apache.isis.viewer.bdd.common.fixtures.perform.Perform;
import org.apache.isis.viewer.bdd.common.parsers.DateParser;

public class UsingIsisViewerForConcordion extends AbstractFixture<UsingIsisViewerPeer> {

    public UsingIsisViewerForConcordion(final AliasRegistry aliasesRegistry, final DeploymentType deploymentType, final DateParser dateParser, final Perform.Mode mode) {
        this(aliasesRegistry, deploymentType, dateParser, mode, CellBindingDefault.builder(IsisViewerConstants.ON_OBJECT_NAME, IsisViewerConstants.ON_OBJECT_HEAD_SET).ditto().build(), CellBindingDefault.builder(IsisViewerConstants.ALIAS_RESULT_NAME, IsisViewerConstants.ALIAS_RESULT_HEAD_SET)
                .optional().build(), CellBindingDefault.builder(IsisViewerConstants.PERFORM_NAME, IsisViewerConstants.PERFORM_HEAD_SET).ditto().build(), CellBindingDefault.builder(IsisViewerConstants.ON_MEMBER_NAME, IsisViewerConstants.ON_MEMBER_HEAD_SET).optional().build(), CellBindingDefault
                .builder(IsisViewerConstants.THAT_IT_NAME, IsisViewerConstants.THAT_IT_HEAD_SET).ditto().optional().build(), CellBindingDefault.builder(IsisViewerConstants.WITH_ARGUMENTS_NAME, IsisViewerConstants.WITH_ARGUMENTS_HEAD_SET).optional().build());
    }

    private UsingIsisViewerForConcordion(final AliasRegistry aliasesRegistry, final DeploymentType deploymentType, final DateParser dateParser, final Perform.Mode mode, final CellBinding onObjectBinding, final CellBinding aliasResultAsBinding, final CellBinding performBinding,
            final CellBinding onMemberBinding, final CellBinding thatItBinding, final CellBinding arg0Binding) {
        super(new UsingIsisViewerPeer(aliasesRegistry, deploymentType, dateParser, mode, onObjectBinding, aliasResultAsBinding, performBinding, onMemberBinding, thatItBinding, arg0Binding));
    }

    public String executeHeader(final String onObject, final String aliasResultAs, final String perform, final String usingMember, final String thatIt, final String arg0, final String... remainingArgs) {

        return setupHeader(onObject, aliasResultAs, perform, usingMember, thatIt, arg0);
    }

    private String setupHeader(final String onObject, final String aliasResultAs, final String perform, final String usingMember, final String thatIt, final String arg0) {
        int colNum = 0;
        getPeer().getOnObjectBinding().setHeadColumn(colNum++);
        getPeer().getAliasResultAsBinding().setHeadColumn(colNum++);
        getPeer().getPerformBinding().setHeadColumn(colNum++);
        getPeer().getOnMemberBinding().setHeadColumn(colNum++);
        if (thatIt != null) {
            getPeer().getThatItBinding().setHeadColumn(colNum++);
        }
        if (arg0 != null) {
            getPeer().getArg0Binding().setHeadColumn(colNum++);
        }

        return ""; // ok
    }

    public String executeRow(final String onObject, final String aliasResultAs, final String perform, final String usingMember, final String thatIt, final String arg0, final String... remainingArgs) {

        setupHeader(onObject, aliasResultAs, perform, usingMember, thatIt, arg0);

        final List<String> argumentCells = new ArrayList<String>();

        // capture current
        getPeer().getOnObjectBinding().captureCurrent(new ScenarioCellDefault(onObject));
        getPeer().getAliasResultAsBinding().captureCurrent(new ScenarioCellDefault(aliasResultAs));
        getPeer().getPerformBinding().captureCurrent(new ScenarioCellDefault(perform));
        getPeer().getOnMemberBinding().captureCurrent(new ScenarioCellDefault(usingMember));
        if (getPeer().getThatItBinding().isFound()) {
            getPeer().getThatItBinding().captureCurrent(new ScenarioCellDefault(thatIt));
        }
        if (getPeer().getArg0Binding().isFound()) {
            getPeer().getArg0Binding().captureCurrent(new ScenarioCellDefault(arg0));
            argumentCells.add(arg0);
        }
        for (final String arg : remainingArgs) {
            argumentCells.add(arg);
        }

        // execute
        try {
            execute(argumentCells);
        } catch (final ScenarioBoundValueException ex) {
            return ex.getMessage();
        }

        return "ok";
    }

    private void execute(final List<String> argumentCells) throws ScenarioBoundValueException {

        final ObjectAdapter onAdapter = getPeer().validateOnObject();
        final String aliasAs = getPeer().validateAliasAs();
        final Perform performCommand = getPeer().validatePerform();

        ObjectMember objectMember = null;
        if (performCommand.requiresMember()) {
            objectMember = getPeer().validateOnMember(onAdapter);
        }

        getPeer().performCommand(onAdapter, aliasAs, objectMember, performCommand, asValues(argumentCells));
    }

    private static List<ScenarioCell> asValues(final List<String> argumentCells) {
        final List<ScenarioCell> storyValues = Lists.newArrayList();
        for (final String arg : argumentCells) {
            storyValues.add(new ScenarioCellDefault(arg));
        }
        return storyValues;
    }
}
