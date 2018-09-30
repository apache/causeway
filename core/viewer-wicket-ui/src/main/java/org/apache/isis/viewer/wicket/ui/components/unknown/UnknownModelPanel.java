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

package org.apache.isis.viewer.wicket.ui.components.unknown;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

public class UnknownModelPanel extends PanelAbstract<IModel<?>> {

    private static final long serialVersionUID = 1L;

    public UnknownModelPanel(final String id, IModel<?> model) {
        super(id, model);
        buildGui(id);
    }

    private void buildGui(final String id) {
        addOrReplace(
                new Label("unknown", Model.of(buildMessage())));
    }

    private String buildMessage() {
        final StringBuilder buf = new StringBuilder();
        if(getModel() != null) {
            buildMessageForModel(buf, getModel());
        } else {
            buf.append("??? model is NULL");
        }
        return buf.toString();
    }

    private void buildMessageForModel(StringBuilder buf, IModel<?> model) {
        buf.append(model.getClass().getSimpleName()).append(" ");
        if(model instanceof EntityModel) {
            EntityModel entityModel = (EntityModel) model;
            ObjectAdapter objectAdapter = entityModel.getObject();
            if(objectAdapter != null) {
                if(objectAdapter.getOid().isValue()) {
                    //FIXME[ISIS-1976] should be properly intercepted by another Panel and not fall through to the unknowns                     
                    buf.append("FIXME[ISIS-1976] VALUE '" + objectAdapter.getPojo()+"'");
                } else {
                    buf.append("??? objectAdapter oid: " + objectAdapter.getOid());    
                }
            } else {
                buf.append("??? objectAdapter is NULL");
            }
        }

    }

}
