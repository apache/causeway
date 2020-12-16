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

package demoapp.webapp.wicket.customview;

import org.apache.isis.viewer.wicket.model.hints.UiHintContainer;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

public class MyEntityPanel extends PanelAbstract<EntityModel>  {


    private static final long serialVersionUID = 1L;


    public MyEntityPanel(
            final String id,
            final EntityModel model,
            final ComponentFactory componentFactory) {
        super(id, model);
    }


    @Override
    public UiHintContainer getUiHintContainer() {
        // disables hinting by this component
        return null;
    }


    /**
     * Build UI only after added to parent.
     */
    @Override
    public void onInitialize() {
        super.onInitialize();
    }

}
