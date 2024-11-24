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
package org.apache.causeway.viewer.wicket.ui.components.collection;

import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.tabular.DataTableInteractive;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.model.models.ActionModel;
import org.apache.causeway.viewer.wicket.model.models.EntityCollectionModelParented;
import org.apache.causeway.viewer.wicket.ui.components.attributes.AttributePanel;
import org.apache.causeway.viewer.wicket.ui.components.collection.selector.CollectionPresentationSelectorPanel;
import org.apache.causeway.viewer.wicket.ui.components.collection.selector.CollectionPresentationSelectorProvider;
import org.apache.causeway.viewer.wicket.ui.panels.PanelAbstract;

import lombok.Getter;
import lombok.Setter;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

/**
 * Panel for rendering entity collection; analogous to (any concrete subclass
 * of) {@link AttributePanel}.
 */
public class CollectionPanel
extends PanelAbstract<DataTableInteractive, EntityCollectionModelParented>
implements
    CollectionPresentationSelectorProvider {

    private static final long serialVersionUID = 1L;

    private static final String ID_FEEDBACK = "feedback";

    @Getter @Setter private CollectionPresentationSelectorPanel selectorDropdownPanel;

    public CollectionPanel(
            final String id,
            final EntityCollectionModelParented collectionModel) {
        super(id, collectionModel);

        var collMetaModel = getModel().getMetaModel();

        var associatedActions = collMetaModel.streamAssociatedActions()
        .map(act->ActionModel.forCollection(act, collectionModel))
        .collect(Can.toCan());

        collectionModel.setLinkAndLabels(associatedActions);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        buildGui();
    }

    private void buildGui() {
        var collectionContents = getComponentFactoryRegistry()
                .addOrReplaceComponent(this, UiComponentType.COLLECTION_CONTENTS, getModel());

        addOrReplace(new NotificationPanel(ID_FEEDBACK, collectionContents,
                new ComponentFeedbackMessageFilter(collectionContents)));

        setOutputMarkupId(true);
    }

}
