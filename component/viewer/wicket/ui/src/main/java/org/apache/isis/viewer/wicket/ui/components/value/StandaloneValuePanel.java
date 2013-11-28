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

package org.apache.isis.viewer.wicket.ui.components.value;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.handler.resource.ResourceRequestHandler;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.http.handler.RedirectRequestHandler;
import org.apache.wicket.request.resource.ByteArrayResource;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.resource.StringResourceStream;

import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.wicket.model.models.ValueModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

/**
 * Panel for rendering any value types that do not have their own custom
 * {@link ScalarPanelAbstract panel} to render them.
 */
public class StandaloneValuePanel extends PanelAbstract<ValueModel> {

    private static final long serialVersionUID = 1L;
    private static final String ID_STANDALONE_VALUE = "standaloneValue";

    public StandaloneValuePanel(final String id, final ValueModel valueModel) {
        super(id, valueModel);
        final ObjectAdapter objectAdapter = getModel().getObject();
        final Object value = objectAdapter.getObject();
        final String label;
        
        if(value instanceof Clob) {
            final Clob clob = (Clob) value;
            ResourceStreamRequestHandler handler = 
                new ResourceStreamRequestHandler(new StringResourceStream(clob.getChars(), clob.getMimeType().toString()), clob.getName());
            handler.setContentDisposition(ContentDisposition.ATTACHMENT);
            getRequestCycle().scheduleRequestHandlerAfterCurrent(handler);
            label = "Downloading: " + clob.getName();
        } else if(value instanceof Blob) {
            final Blob blob = (Blob) value;
            ResourceRequestHandler handler = 
                    new ResourceRequestHandler(new ByteArrayResource(blob.getMimeType().toString(), blob.getBytes(), blob.getName()), null);
            getRequestCycle().scheduleRequestHandlerAfterCurrent(handler);
            label = "Downloading: " + blob.getName();
        } else if(value instanceof java.net.URL) {
            java.net.URL url = (java.net.URL) value;
            label = "Downloading: " + objectAdapter.titleString(null);
            IRequestHandler handler = 
                    new RedirectRequestHandler(url.toString());
            getRequestCycle().scheduleRequestHandlerAfterCurrent(handler);
        } else {
            label = objectAdapter.titleString(null);
        }
        add(new Label(ID_STANDALONE_VALUE, label));
    }

}
