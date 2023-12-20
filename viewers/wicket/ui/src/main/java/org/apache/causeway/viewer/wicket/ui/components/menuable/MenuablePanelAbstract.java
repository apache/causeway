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
package org.apache.causeway.viewer.wicket.ui.components.menuable;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.viewer.wicket.model.links.Menuable;
import org.apache.causeway.viewer.wicket.model.links.MenuablesModel;
import org.apache.causeway.viewer.wicket.ui.panels.PanelAbstract;

/**
 * Common panel for drop-downs and sub-menus.
 */
public abstract class MenuablePanelAbstract
extends PanelAbstract<Can<? extends Menuable>, MenuablesModel> {

    private static final long serialVersionUID = 1L;

    protected MenuablePanelAbstract(final String id, final Can<? extends Menuable> menuables) {
        super(id, new MenuablesModel(menuables));
    }

    public final MenuablesModel menuablesModel() {
        return getModel();
    }

}
