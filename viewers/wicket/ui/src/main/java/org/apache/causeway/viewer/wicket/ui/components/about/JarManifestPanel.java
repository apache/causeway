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
package org.apache.causeway.viewer.wicket.ui.components.about;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;

import org.apache.causeway.viewer.wicket.ui.panels.PanelUtil;

public class JarManifestPanel extends Panel {

    private static final long serialVersionUID = 1L;

    private static final String ID_MANIFEST_ATTRIBUTES = "manifestAttributes";

    private static final String ID_MANIFEST_ATTRIBUTE = "manifestAttribute";
    private static final String ID_LINE = "manifestAttributeLine";

    public JarManifestPanel(String id, JarManifestModel manifestModel) {
        super(id, manifestModel);

        final MarkupContainer container = new WebMarkupContainer(ID_MANIFEST_ATTRIBUTES);
        container.add(
                new JarManifestListView(ID_MANIFEST_ATTRIBUTE, JarManifestPanel.ID_LINE, manifestModel.getDetail()));
        add(container);
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        PanelUtil.renderHead(response, this.getClass());
    }

}
