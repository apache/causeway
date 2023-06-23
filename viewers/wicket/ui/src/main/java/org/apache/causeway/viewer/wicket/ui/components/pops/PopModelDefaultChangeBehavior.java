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
package org.apache.causeway.viewer.wicket.ui.components.pops;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;

class PopModelDefaultChangeBehavior extends AjaxFormComponentUpdatingBehavior {
    private static final long serialVersionUID = 1L;

    private final PopPanelAbstract popPanel;

    PopModelDefaultChangeBehavior(final PopPanelAbstract popPanel) {
        super("change");
        this.popPanel = popPanel;
    }

    @Override
    protected void onUpdate(final AjaxRequestTarget target) {
        popPanel.getPopModelChangeDispatcher().notifyUpdate(target);
    }

    @Override
    protected void onError(final AjaxRequestTarget target, final RuntimeException e) {
        super.onError(target, e);
        popPanel.getPopModelChangeDispatcher().notifyError(target);
    }

}