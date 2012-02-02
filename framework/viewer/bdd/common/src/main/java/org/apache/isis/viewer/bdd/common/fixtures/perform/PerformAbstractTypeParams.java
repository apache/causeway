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
package org.apache.isis.viewer.bdd.common.fixtures.perform;

import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.ScenarioBoundValueException;
import org.apache.isis.viewer.bdd.common.ScenarioCell;

public abstract class PerformAbstractTypeParams extends PerformAbstract {

    protected static enum Type {
        PROPERTY(true) {
            @Override
            String ensureMemberIsOfType(final ObjectMember m) {
                return m instanceof OneToOneAssociation ? null : "(not a property)";
            }
        },
        COLLECTION(true) {
            @Override
            String ensureMemberIsOfType(final ObjectMember m) {
                return m instanceof OneToManyAssociation ? null : "(not a collection)";
            }
        },
        ACTION(true) {
            @Override
            String ensureMemberIsOfType(final ObjectMember m) {
                return m instanceof ObjectAction ? null : "(not an action)";
            }
        },
        OBJECT(false) {
            @Override
            String ensureMemberIsOfType(final ObjectMember m) {
                return m != null ? "(not required)" : null;
            }
        };
        // public void ensureMemberIsOfType(
        // final NakedObjectMember nakedObjectMember,
        // final StoryCell onMemberCell) throws StoryFailureException {
        // final String msg = ensureMemberIsOfType(nakedObjectMember);
        // if (msg != null) {
        // throw new StoryFailureException(onMemberCell, msg);
        // }
        // }

        abstract String ensureMemberIsOfType(ObjectMember m);

        private final boolean representsMember;

        private Type(final boolean representsMember) {
            this.representsMember = representsMember;
        }

        public boolean representsMember() {
            return representsMember;
        }
    }

    protected static enum NumParameters {
        ZERO, ONE, UNLIMITED;
    }

    private final Type type;
    private final NumParameters numParameters;

    public PerformAbstractTypeParams(final String key, final Type type, final NumParameters numParameters, final Perform.Mode mode) {
        super(key, mode);
        this.type = type;
        this.numParameters = numParameters;
    }

    @Override
    public boolean requiresMember() {
        return type.representsMember();
    }

    /**
     * Can be overridden, but provides basic checking that member is of correct
     * type and, if taking {@link NumParameters#ZERO zero} or
     * {@link NumParameters#ONE one} parameter, that the correct number of
     * actual arguments match.
     */
    @Override
    public void perform(final PerformContext performContext) throws ScenarioBoundValueException {

        final CellBinding onMemberBinding = performContext.getPeer().getOnMemberBinding();
        final ScenarioCell onMemberCell = onMemberBinding.getCurrentCell();

        final String reason = type.ensureMemberIsOfType(performContext.getObjectMember());
        if (reason != null) {
            throw ScenarioBoundValueException.current(onMemberBinding, reason);
        }

        if (type.representsMember()) {

            if (getMode() == Perform.Mode.TEST) {

                performContext.ensureAvailableForDeploymentType(onMemberBinding, onMemberCell);

                performContext.ensureVisible(onMemberBinding, onMemberCell);
                performContext.ensureUsable(onMemberBinding, onMemberCell);
            }

            // validate we have correct number of parameters if zero or one.
            final int numArgs = performContext.getArgumentCells().size();

            // don't do these two checks, because there could be additional
            // cells due to previous or subsequent rows in the table
            // (with the author keeping this 'tidy')

            // if (numParameters == NumParameters.ZERO && length > 0) {
            // ... "(no arguments required)"
            // }
            // if (numParameters == NumParameters.ONE && length > 1) {
            // ... "(need just 1 argument)"
            // }

            // okay to do this check, though
            if (numParameters == NumParameters.ONE && numArgs < 1) {
                throw ScenarioBoundValueException.current(onMemberBinding, "(need one argument)");
            }
        }

        doHandle(performContext);
    }

    /**
     * Hook method that does nothing; should be overridden if
     * {@link #perform(PerformContextForFitNesse) handle(...)} method has not
     * been.
     */
    protected void doHandle(final PerformContext performContext) throws ScenarioBoundValueException {
        // does nothing
    }

    // /**
    // * Convenience; delegates to
    // * {@link Type#ensureMemberIsOfType(NakedObjectMember)} and downcasts.
    // */
    // protected OneToOneAssociation ensureMemberIsProperty(
    // final NakedObjectMember nakedObjectMember,
    // final StoryCell usingMemberCell) throws StoryFailureException {
    // type.ensureMemberIsOfType(nakedObjectMember, usingMemberCell);
    // return (OneToOneAssociation) nakedObjectMember;
    // }
    //
    // /**
    // * Convenience; delegates to
    // * {@link Type#ensureMemberIsOfType(NakedObjectMember)} and downcasts.
    // */
    // protected OneToManyAssociation ensureMemberIsCollection(
    // final NakedObjectMember nakedObjectMember,
    // final StoryCell usingMemberCell) throws StoryFailureException {
    // type.ensureMemberIsOfType(nakedObjectMember, usingMemberCell);
    // return (OneToManyAssociation) nakedObjectMember;
    // }
    //
    // /**
    // * Convenience; delegates to
    // * {@link Type#ensureMemberIsOfType(NakedObjectMember)} and downcasts.
    // */
    // protected NakedObjectAction ensureMemberIsAction(
    // final NakedObjectMember nakedObjectMember,
    // final StoryCell usingMemberCell) throws StoryFailureException {
    // type.ensureMemberIsOfType(nakedObjectMember, usingMemberCell);
    // return (NakedObjectAction) nakedObjectMember;
    // }

}
