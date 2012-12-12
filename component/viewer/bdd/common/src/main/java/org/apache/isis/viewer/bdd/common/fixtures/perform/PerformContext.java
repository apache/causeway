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

import java.util.List;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.facets.actions.exploration.ExplorationFacet;
import org.apache.isis.core.metamodel.facets.actions.prototype.PrototypeFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.ScenarioBoundValueException;
import org.apache.isis.viewer.bdd.common.ScenarioCell;
import org.apache.isis.viewer.bdd.common.fixtures.UsingIsisViewerPeer;
import org.apache.isis.viewer.bdd.common.parsers.DateParser;

/**
 * Represents the context for a single {@link Perform} command; in effect, a row
 * of a table.
 */
public class PerformContext {

    // REVIEW: should provide this rendering context, rather than hardcoding.
    // the net effect currently is that class members annotated with 
    // @Hidden(where=Where.ANYWHERE) or @Disabled(where=Where.ANYWHERE) will indeed
    // be hidden/disabled, but will be visible/enabled (perhaps incorrectly) 
    // for any other value for Where
    private final Where where = Where.ANYWHERE;

    private final UsingIsisViewerPeer peer;

    private final ObjectAdapter onAdapter;
    private final ObjectMember objectMember;
    private final List<ScenarioCell> argumentCells;

    public PerformContext(final UsingIsisViewerPeer peer, final ObjectAdapter onAdapter, final ObjectMember objectMember, final List<ScenarioCell> argumentCells) {
        this.onAdapter = onAdapter;
        this.objectMember = objectMember;
        this.peer = peer;
        this.argumentCells = argumentCells;
    }

    public UsingIsisViewerPeer getPeer() {
        return peer;
    }

    public ObjectAdapter getOnAdapter() {
        return onAdapter;
    }

    public ObjectMember getObjectMember() {
        return objectMember;
    }

    public List<ScenarioCell> getArgumentCells() {
        return argumentCells;
    }

    public Consent visibleMemberConsent() {
        return getObjectMember().isVisible(getAuthenticationSession(), getOnAdapter(), where);
    }

    public Consent usableMemberConsent() {
        return getObjectMember().isUsable(getAuthenticationSession(), getOnAdapter(), where);
    }

    public Consent validObjectConsent() {
        final ObjectAdapter onAdapter = getOnAdapter();
        return onAdapter.getSpecification().isValid(onAdapter);
    }

    public void ensureVisible(final CellBinding onMemberBinding, final ScenarioCell onMemberCell) throws ScenarioBoundValueException {
        final Consent visible = objectMember.isVisible(getAuthenticationSession(), getOnAdapter(), where);
        if (visible.isVetoed()) {
            throw ScenarioBoundValueException.current(onMemberBinding, "(not visible)");
        }
    }

    public void ensureUsable(final CellBinding onMemberBinding, final ScenarioCell onMemberCell) throws ScenarioBoundValueException {
        final Consent usable = objectMember.isUsable(getAuthenticationSession(), getOnAdapter(), where);
        if (usable.isVetoed()) {
            throw ScenarioBoundValueException.current(onMemberBinding, "(not usable)");
        }
    }

    public void ensureAvailableForDeploymentType(final CellBinding onMemberBinding, final ScenarioCell onMemberCell) throws ScenarioBoundValueException {
        final DeploymentType deploymentType = this.peer.getDeploymentType();

        final boolean isExploration = objectMember.getFacet(ExplorationFacet.class) != null;
        if (isExploration && !deploymentType.isExploring()) {
            throw ScenarioBoundValueException.current(onMemberBinding, "(not running in exploration mode)");
        }

        final boolean isPrototype = objectMember.getFacet(PrototypeFacet.class) != null;
        if (isPrototype && !deploymentType.isPrototyping()) {
            throw ScenarioBoundValueException.current(onMemberBinding, "(not running in prototype mode)");
        }
    }

    protected AuthenticationSession getAuthenticationSession() {
        return getPeer().getAuthenticationSession();
    }

    public DateParser getDateParser() {
        return peer.getDateParser();
    }

}
