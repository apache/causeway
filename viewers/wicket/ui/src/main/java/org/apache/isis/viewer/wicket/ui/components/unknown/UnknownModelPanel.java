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

import org.apache.wicket.model.IModel;

import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.Wkt;

import lombok.val;

public class UnknownModelPanel
extends PanelAbstract<Object, IModel<Object>> {

    private static final long serialVersionUID = 1L;

    public UnknownModelPanel(final String id, final IModel<?> model) {
        super(id, _Casts.uncheckedCast(model));
        buildGui(id);
    }

    private void buildGui(final String id) {
        Wkt.labelAdd(this, "unknown", buildMessage());
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

    private void buildMessageForModel(final StringBuilder buf, final IModel<?> model) {
        buf.append(model.getClass().getSimpleName()).append(" ");
        if(model instanceof EntityModel) {
            val entityModel = (EntityModel) model;
            val objectAdapter = entityModel.getObject();
            if(objectAdapter != null) {
                buf.append("??? objectAdapter oid: " + ManagedObjects.bookmark(objectAdapter).orElse(null));
            } else {
                buf.append("??? objectAdapter is NULL");
            }
        } else if(model instanceof ScalarModel) {
            val scalarModel = (ScalarModel) model;
            val scalarAdapter = scalarModel.getObject();
            if(ManagedObjects.isSpecified(scalarAdapter)) {
                buf.append(String.format("??? spec=%s, value='%s'",
                        scalarAdapter.getSpecification(), scalarAdapter.getPojo()));
            } else {
                buf.append("??? scalarAdapter is NULL or UNSPECIFIED");
            }
        }

    }

}
