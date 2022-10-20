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
package org.apache.causeway.viewer.wicket.model.models;

import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;



/**
 * Represents the result of invoking a <tt>void</tt> action.
 */
public class VoidModel extends ModelAbstract<Void> {

    private static final long serialVersionUID = 1L;

    public VoidModel(final MetaModelContext commonContext) {
        super(commonContext);
    }

    @Override
    protected Void load() {
        return null;
    }


    // //////////////////////////////////////

    private ActionModel actionModelHint;
    /**
     * The {@link ActionModelImpl model} of the {@link ObjectAction action}
     * that generated this {@link VoidModel}.
     *
     * @see #setActionHint(ActionModelImpl)
     */
    public ActionModel getActionModelHint() {
        return actionModelHint;
    }
    /**
     * Called by action.
     *
     * @see #getActionModelHint()
     */
    public void setActionHint(final ActionModel actionModelHint) {
        this.actionModelHint = actionModelHint;
    }
}
