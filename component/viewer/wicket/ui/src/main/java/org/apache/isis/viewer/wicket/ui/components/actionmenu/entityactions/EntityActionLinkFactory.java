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

package org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions;

import org.apache.wicket.markup.html.link.AbstractLink;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.*;
import org.apache.isis.viewer.wicket.ui.components.widgets.linkandlabel.ActionLinkFactoryAbstract;

public final class EntityActionLinkFactory extends ActionLinkFactoryAbstract {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private final EntityModel entityModel;

    public EntityActionLinkFactory(final EntityModel entityModel) {
        this.entityModel = entityModel;
    }

    @Override
    public LinkAndLabel newLink(
            final ObjectAdapterMemento adapterMemento,
            final ObjectAction action,
            final String linkId) {

        final ObjectAdapter objectAdapter = adapterMemento.getObjectAdapter(ConcurrencyChecking.NO_CHECK);
        
        final Boolean persistent = objectAdapter.representsPersistent();
        if (!persistent) {
            throw new IllegalArgumentException("Object '" + objectAdapter.titleString(null) + "' is not persistent.");
        }

        // check visibility and whether enabled
        final AuthenticationSession session = getAuthenticationSession();
        
        final Consent visibility = action.isVisible(session, objectAdapter, Where.OBJECT_FORMS);
        if (visibility.isVetoed()) {
            return null;
        }

        
        final AbstractLink link = newLink(linkId, objectAdapter, action);
        
        final Consent usability = action.isUsable(session, objectAdapter, Where.OBJECT_FORMS);
        final String disabledReasonIfAny = usability.getReason();
        if(disabledReasonIfAny != null) {
            link.setEnabled(false);
        }

        return newLinkAndLabel(objectAdapter, action, link, disabledReasonIfAny);
    }

    

    // ///////////////////////////////////////////////////////////////////
    // Dependencies (from IsisContext)
    // ///////////////////////////////////////////////////////////////////

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    protected AuthenticationSession getAuthenticationSession() {
        return IsisContext.getAuthenticationSession();
    }

}