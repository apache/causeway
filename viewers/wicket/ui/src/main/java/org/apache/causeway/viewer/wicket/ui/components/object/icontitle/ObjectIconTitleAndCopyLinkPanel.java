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
package org.apache.causeway.viewer.wicket.ui.components.object.icontitle;

import org.apache.wicket.MarkupContainer;

import org.apache.causeway.applib.annotation.ObjectSupport.IconSize;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;
import org.apache.causeway.viewer.wicket.ui.components.widgets.zclip.ZeroClipboardPanel;

/**
 * An extension of {@link org.apache.causeway.viewer.wicket.ui.components.object.icontitle.ObjectIconAndTitlePanel}
 * that additionally has a link allowing to copy the URL to the shown entity
 */
class ObjectIconTitleAndCopyLinkPanel extends ObjectIconAndTitlePanel {

    private static final long serialVersionUID = 1L;

    private static final String ID_COPY_LINK = "copyLink";

    public ObjectIconTitleAndCopyLinkPanel(final String id, final IconSize iconSize, final UiObjectWkt objectModel) {
        super(id, iconSize, objectModel);
    }

    @Override
    protected void onLinkWrapperCreated(final MarkupContainer linkWrapper) {
        linkWrapper.add(new ZeroClipboardPanel(ID_COPY_LINK, getModel()));
    }
}
