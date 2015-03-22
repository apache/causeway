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

package org.apache.isis.viewer.wicket.ui.components.entity.icontitle;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.components.widgets.zclip.ZeroClipboardPanel;

/**
 * An extension of {@link org.apache.isis.viewer.wicket.ui.components.entity.icontitle.EntityIconAndTitlePanel}
 * that additionally has a link allowing to copy the url to the shown entity
 */
public class EntityIconTitleAndCopyLinkPanel extends EntityIconAndTitlePanel {

    private static final long serialVersionUID = 1L;

    private static final String ID_COPY_LINK = "copyLink";

    public EntityIconTitleAndCopyLinkPanel(final String id, final EntityModel entityModel) {
        super(id, entityModel);
    }

    @Override
    protected WebMarkupContainer addOrReplaceLinkWrapper(final EntityModel entityModel) {
        WebMarkupContainer linkWrapper = super.addOrReplaceLinkWrapper(entityModel);

        ZeroClipboardPanel zClipCopyLink = new ZeroClipboardPanel(ID_COPY_LINK, entityModel);
        linkWrapper.add(zClipCopyLink);

        return linkWrapper;
    }
}
