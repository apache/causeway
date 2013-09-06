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

package org.apache.isis.viewer.dnd.view.action;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.commons.util.ToString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Allow;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.Veto;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.option.UserActionAbstract;

public abstract class AbstractObjectOption extends UserActionAbstract {

    // REVIEW: confirm this rendering context
    private final Where where = Where.OBJECT_FORMS;

    protected final ObjectAction action;
    protected final ObjectAdapter target;

    protected AbstractObjectOption(final ObjectAction action, final ObjectAdapter target, final String name) {
        super(name);
        this.action = action;
        this.target = target;
    }

    @Override
    public Consent disabled(final View view) {
        final ObjectAdapter adapter = view.getContent().getAdapter();
        if (adapter != null && adapter.isDestroyed()) {
            // TODO: move logic into Facet
            return new Veto("Can't do anything with a destroyed object");
        }
        final Consent usableForUser = action.isUsable(IsisContext.getAuthenticationSession(), target, where);
        if (usableForUser.isVetoed()) {
            return usableForUser;
        }

        final Consent validParameters = checkValid();
        if (validParameters != null && validParameters.isVetoed()) {
            return validParameters;
        }
        final String desc = action.getDescription();
        final String description = getName(view) + (desc.length() == 0 ? "" : ": " + desc);
        // TODO: replace with a Facet
        return new Allow(description);
    }

    protected Consent checkValid() {
        return null;
    }

    @Override
    public String getHelp(final View view) {
        return action.getHelp();
    }

    @Override
    public ActionType getType() {
        return action.getType();
    }

    @Override
    public String toString() {
        return new ToString(this).append("action", action).toString();
    }
}
