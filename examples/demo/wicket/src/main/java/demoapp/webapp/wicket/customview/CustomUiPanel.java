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

import org.apache.wicket.markup.html.link.InlineFrame;

import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.viewer.wicket.model.hints.UiHintContainer;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

import lombok.val;

import demoapp.dom.ui.custom.vm.CustomUiVm;

public class CustomUiPanel extends PanelAbstract<EntityModel>  {


    private static final long serialVersionUID = 1L;


    public CustomUiPanel(
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
        buildGui();
    }

    private void buildGui() {
        val managedObject = (ManagedObject) getModelObject();
        val customUiVm = (CustomUiVm) managedObject.getPojo();

        val iframe = new InlineFrame("iframe", getPage()) {

            @Override
            protected CharSequence getURL() {
                val boundingBox = customUiVm.getBoundingBox();
                val url = boundingBox.toUrl();
                return String.format(
                        "https://www.openstreetmap.org/export/embed.html?bbox=%s&layer=%s"
                        , url
                        , "mapnik");
            }

        };

        addOrReplace(iframe);
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();
        buildGui();
    }
}
