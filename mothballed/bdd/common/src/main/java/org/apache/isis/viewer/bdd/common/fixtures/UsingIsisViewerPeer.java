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
package org.apache.isis.viewer.bdd.common.fixtures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.isis.core.commons.exceptions.NotYetImplementedException;
import org.apache.isis.core.commons.lang.StringUtils;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.facets.object.parseable.TextEntryParseException;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectActionSet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionContainer.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.viewer.bdd.common.AliasRegistry;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.ScenarioBoundValueException;
import org.apache.isis.viewer.bdd.common.ScenarioCell;
import org.apache.isis.viewer.bdd.common.fixtures.perform.AddToCollection;
import org.apache.isis.viewer.bdd.common.fixtures.perform.CheckAction;
import org.apache.isis.viewer.bdd.common.fixtures.perform.CheckAddToCollection;
import org.apache.isis.viewer.bdd.common.fixtures.perform.CheckClearProperty;
import org.apache.isis.viewer.bdd.common.fixtures.perform.CheckCollection;
import org.apache.isis.viewer.bdd.common.fixtures.perform.CheckObject;
import org.apache.isis.viewer.bdd.common.fixtures.perform.CheckProperty;
import org.apache.isis.viewer.bdd.common.fixtures.perform.CheckRemoveFromCollection;
import org.apache.isis.viewer.bdd.common.fixtures.perform.CheckSetProperty;
import org.apache.isis.viewer.bdd.common.fixtures.perform.ClearProperty;
import org.apache.isis.viewer.bdd.common.fixtures.perform.GetActionParameterChoices;
import org.apache.isis.viewer.bdd.common.fixtures.perform.GetActionParameterDefault;
import org.apache.isis.viewer.bdd.common.fixtures.perform.GetCollection;
import org.apache.isis.viewer.bdd.common.fixtures.perform.GetProperty;
import org.apache.isis.viewer.bdd.common.fixtures.perform.GetPropertyChoices;
import org.apache.isis.viewer.bdd.common.fixtures.perform.GetPropertyDefault;
import org.apache.isis.viewer.bdd.common.fixtures.perform.InvokeAction;
import org.apache.isis.viewer.bdd.common.fixtures.perform.Perform;
import org.apache.isis.viewer.bdd.common.fixtures.perform.PerformContext;
import org.apache.isis.viewer.bdd.common.fixtures.perform.RemoveFromCollection;
import org.apache.isis.viewer.bdd.common.fixtures.perform.SaveObject;
import org.apache.isis.viewer.bdd.common.fixtures.perform.SetProperty;
import org.apache.isis.viewer.bdd.common.parsers.DateParser;

public class UsingIsisViewerPeer extends AbstractFixturePeer {

    private static List<Perform> performCommands(final Perform.Mode mode) {
        final ArrayList<Perform> commands = new ArrayList<Perform>();

        commands.add(new CheckProperty(mode));
        commands.add(new CheckSetProperty(mode));
        commands.add(new CheckClearProperty(mode));
        commands.add(new GetProperty(mode));
        commands.add(new SetProperty(mode));
        commands.add(new ClearProperty(mode));
        commands.add(new GetPropertyDefault(mode));
        commands.add(new GetPropertyChoices(mode));

        commands.add(new CheckCollection(mode));
        commands.add(new CheckAddToCollection(mode));
        commands.add(new CheckRemoveFromCollection(mode));
        commands.add(new AddToCollection(mode));
        commands.add(new RemoveFromCollection(mode));
        commands.add(new GetCollection(mode));

        commands.add(new CheckAction(mode));
        commands.add(new InvokeAction(mode));
        commands.add(new GetActionParameterDefault(mode));
        commands.add(new GetActionParameterChoices(mode));

        commands.add(new CheckObject(mode));
        commands.add(new SaveObject(mode));

        return commands;
    }

    // //////////////////////////////////////////////////////////////////
    // constructor
    // //////////////////////////////////////////////////////////////////

    private final CellBinding onObjectBinding;
    private final CellBinding aliasResultAsBinding;
    private final CellBinding performBinding;
    private final CellBinding onMemberBinding;
    private final CellBinding thatItBinding;
    private final CellBinding arg0Binding;

    private final DeploymentType deploymentType;
    private final DateParser dateParser;

    private final Map<String, Perform> commandByKey = new HashMap<String, Perform>();

    public UsingIsisViewerPeer(final AliasRegistry aliasesRegistry, final DeploymentType deploymentType, final DateParser dateParser, final Perform.Mode mode, final CellBinding onObjectBinding, final CellBinding aliasResultAsBinding, final CellBinding performBinding,
            final CellBinding onMemberBinding, final CellBinding thatItBinding, final CellBinding arg0Binding) {
        super(aliasesRegistry, onObjectBinding, aliasResultAsBinding, performBinding, onMemberBinding, thatItBinding, arg0Binding);

        this.onObjectBinding = onObjectBinding;
        this.aliasResultAsBinding = aliasResultAsBinding;
        this.performBinding = performBinding;
        this.onMemberBinding = onMemberBinding;
        this.thatItBinding = thatItBinding;
        this.arg0Binding = arg0Binding;

        this.deploymentType = deploymentType;
        this.dateParser = dateParser;

        final List<Perform> performCommands = performCommands(mode);
        for (final Perform command : performCommands) {
            commandByKey.put(command.getKey(), command);
        }
    }

    public DeploymentType getDeploymentType() {
        return deploymentType;
    }

    public DateParser getDateParser() {
        return dateParser;
    }

    public CellBinding getOnObjectBinding() {
        return onObjectBinding;
    }

    public CellBinding getAliasResultAsBinding() {
        return aliasResultAsBinding;
    }

    public CellBinding getOnMemberBinding() {
        return onMemberBinding;
    }

    public CellBinding getPerformBinding() {
        return performBinding;
    }

    public CellBinding getThatItBinding() {
        return thatItBinding;
    }

    public CellBinding getArg0Binding() {
        return arg0Binding;
    }

    public boolean isArg0BindingLast() {
        return !bindingAfterArg0();

    }

    private boolean bindingAfterArg0() {
        if (!getArg0Binding().isFound()) {
            return false;
        }
        for (final CellBinding binding : getCellBindings()) {
            if (binding.getColumn() > getArg0Binding().getColumn()) {
                return true;
            }
        }
        return false;
    }

    // //////////////////////////////////////////////////////////////////
    // validate API
    // //////////////////////////////////////////////////////////////////

    public ObjectAdapter validateOnObject() throws ScenarioBoundValueException {

        final ScenarioCell onObjectCell = onObjectBinding.getCurrentCell();
        String onObject = onObjectCell.getText();
        if (onObject == null) {
            if (previousOnObject == null) {
                throw ScenarioBoundValueException.current(onObjectBinding, "(required)");
            }
            onObject = previousOnObject;
        } else {
            previousOnObject = onObject;
        }
        final ObjectAdapter onAdapter = getAliasRegistry().getAliased(onObject);
        if (onAdapter == null) {
            throw ScenarioBoundValueException.current(onMemberBinding, "(unknown object)");
        }
        return onAdapter;
    }

    public String validateAliasAs() throws ScenarioBoundValueException {
        if (getAliasResultAsBinding() == null) {
            return null;
        }
        final ScenarioCell aliasCell = aliasResultAsBinding.getCurrentCell();
        if (aliasCell == null) {
            return null;
        }

        final String aliasAs = aliasCell.getText();
        if (getAliasRegistry().getAliased(aliasAs) != null) {
            throw ScenarioBoundValueException.current(aliasResultAsBinding, "(already used)");
        }
        return aliasAs;
    }

    public ObjectMember validateOnMember(final ObjectAdapter onAdapter) throws ScenarioBoundValueException {

        final ScenarioCell onMemberCell = onMemberBinding.getCurrentCell();
        final String onMember = onMemberCell.getText();

        if (StringUtils.isNullOrEmpty(onMember)) {
            throw ScenarioBoundValueException.current(onMemberBinding, "(required)");
        }

        if (onAdapter == null) {
            return null;
        }

        // see if property, collection or action.
        final String memberId = StringUtils.memberIdFor(onMember);
        final ObjectSpecification spec = onAdapter.getSpecification();
        final List<ObjectMember> objectMembers = new ArrayList<ObjectMember>();

        objectMembers.addAll(spec.getAssociations());

        // see if action (of any type)
        objectMembers.addAll(spec.getObjectActions(Arrays.asList(ActionType.USER, ActionType.EXPLORATION, ActionType.DEBUG), Contributed.INCLUDED));
        for (final ObjectMember member : objectMembers) {
            if (matchesId(member, memberId)) {
                return member;
            }
            // special handling for contributed actions.
            if (member instanceof ObjectActionSet) {
                final ObjectActionSet actionSet = (ObjectActionSet) member;
                for (final ObjectAction contributedAction : actionSet.getActions()) {
                    if (contributedAction.getId().equals(memberId)) {
                        return contributedAction;
                    }
                }
            }
        }
        throw ScenarioBoundValueException.current(onMemberBinding, "(unknown member)");
    }

    public Perform validatePerform() throws ScenarioBoundValueException {
        final String perform = performBinding.getCurrentCell().getText();
        if (perform == null) {
            throw ScenarioBoundValueException.current(performBinding, "(required)");
        }
        final Perform performCommand = commandByKey.get(perform);
        if (performCommand == null) {
            throw ScenarioBoundValueException.current(performBinding, "(unknown interaction)");
        }
        return performCommand;
    }

    private boolean matchesId(final ObjectMember member, final String memberId) {
        return member.getId().equals(memberId);
    }

    // //////////////////////////////////////////////////////////////////
    // "perform" API
    // //////////////////////////////////////////////////////////////////

    public void performCommand(final ObjectAdapter onAdapter, final String aliasAs, final ObjectMember objectMember, final Perform performCommand, final List<ScenarioCell> argumentStoryCells) throws ScenarioBoundValueException {
        final PerformContext performContext = new PerformContext(this, onAdapter, objectMember, argumentStoryCells);
        try {
            performCommand.perform(performContext);
        } catch (final RuntimeException ex) {
            // handler should have colored in invalid cells.
        }
        aliasResultFromPerformCommand(performCommand, aliasAs);
    }

    private void aliasResultFromPerformCommand(final Perform performCommand, final String aliasAs) throws ScenarioBoundValueException {
        if (StringUtils.isNullOrEmpty(aliasAs)) {
            return;
        }
        final ObjectAdapter resultAdapter = performCommand.getResult();
        if (resultAdapter == null) {
            throw ScenarioBoundValueException.current(onMemberBinding, "(no result)");
        }
        getAliasRegistry().aliasAs(aliasAs, resultAdapter);
    }

    // //////////////////////////////////////////////////////////////////
    //
    // //////////////////////////////////////////////////////////////////

    private String previousOnObject = null;

    /**
     * Not public API
     */
    public void provideDefault(final ScenarioCell storySource, final String resultStr) {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }

    /**
     * Not public API
     */
    public ObjectAdapter getAdapter(final ObjectAdapter contextAdapter, final ObjectSpecification noSpec, final CellBinding contextBinding, final ScenarioCell paramCell) throws ScenarioBoundValueException {

        final String cellText = paramCell.getText();

        // see if can handle as parseable value
        final ParseableFacet parseableFacet = noSpec.getFacet(ParseableFacet.class);
        if (parseableFacet != null) {
            try {
                return parseableFacet.parseTextEntry(contextAdapter, cellText, null);
            } catch (final TextEntryParseException ex) {
                throw ScenarioBoundValueException.arg(contextBinding, paramCell, "(cannot parse '" + cellText + "')");
            } catch (final IllegalArgumentException ex) {
                // REVIEW: isn't what is thrown, but perhaps
                // TextEntryParseException should inherit from
                // IllegalArgumentException?
                throw ScenarioBoundValueException.arg(contextBinding, paramCell, "(cannot parse '" + cellText + "')");
            }
        }

        // otherwise, handle as reference to known object
        final ObjectAdapter adapter = getAliasRegistry().getAliased(cellText);
        if (adapter == null) {
            throw ScenarioBoundValueException.arg(contextBinding, paramCell, "(unknown reference'" + cellText + "')");
        }

        return adapter;
    }

    /**
     * Not public API
     * 
     * <p>
     * Ensures that there are at least enough arguments for the number of
     * parameters required.
     */
    public ObjectAdapter[] getAdapters(final ObjectAdapter onAdapter, final ObjectAction objectAction, final CellBinding onMemberBinding, final List<ScenarioCell> argumentCells) throws ScenarioBoundValueException {
        final List<ObjectActionParameter> parameters = objectAction.getParameters();

        final int parameterCount = parameters.size();
        if (argumentCells.size() < parameterCount) {
            throw ScenarioBoundValueException.current(onMemberBinding, "(action requires " + parameterCount + " arguments)");
        }
        final ObjectAdapter[] adapters = new ObjectAdapter[parameterCount];

        for (int i = 0; i < parameterCount; i++) {
            final ScenarioCell paramCell = argumentCells.get(i);
            final ObjectActionParameter parameter = parameters.get(i);
            adapters[i] = getAdapter(null, parameter.getSpecification(), onMemberBinding, paramCell);
        }
        return adapters;
    }

    /**
     * Not public API
     */
    public ObjectAdapter toAdaptedListOfPojos(final ObjectAdapter[] choiceAdapters) {
        final List<Object> choiceList = new ArrayList<Object>();
        if (choiceAdapters != null) {
            for (final ObjectAdapter adapter : choiceAdapters) {
                choiceList.add(adapter.getObject());
            }
        }
        return getAdapterManager().adapterFor(choiceList);
    }

}
