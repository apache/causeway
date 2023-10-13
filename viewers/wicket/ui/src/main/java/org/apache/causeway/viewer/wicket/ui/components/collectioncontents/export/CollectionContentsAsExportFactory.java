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
package org.apache.causeway.viewer.wicket.ui.components.collectioncontents.export;

import java.io.File;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.apache.causeway.viewer.commons.applib.tabular.export.CollectionContentsExporter;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.causeway.viewer.wicket.ui.CollectionContentsAsFactory;
import org.apache.causeway.viewer.wicket.ui.ComponentFactory;
import org.apache.causeway.viewer.wicket.ui.ComponentFactoryAbstract;
import org.apache.causeway.viewer.wicket.ui.app.registry.ComponentFactoryKey;
import org.apache.causeway.viewer.wicket.ui.components.download.FileDownloadLink;

/**
 * {@link ComponentFactory} for {@link DownloadLink}.
 *
 * @since 2.0 {@index}
 */
@org.springframework.stereotype.Component
public class CollectionContentsAsExportFactory
extends ComponentFactoryAbstract
implements CollectionContentsAsFactory {

    private final CollectionContentsExporter collectionContentsExporter;

    public CollectionContentsAsExportFactory(final CollectionContentsExporter collectionContentsExporter) {
        super(UiComponentType.COLLECTION_CONTENTS_EXPORT, collectionContentsExporter.getClass().getName(), DownloadLink.class);
        this.collectionContentsExporter = collectionContentsExporter;
    }

    @Override
    public ApplicationAdvice appliesTo(final IModel<?> model) {
        if(!(model instanceof EntityCollectionModel)) {
            return ApplicationAdvice.DOES_NOT_APPLY;
        }
        return ApplicationAdvice.APPLIES;
    }

    @Override
    public Component createComponent(final String id, final IModel<?> model) {
        var collectionModel = (EntityCollectionModel) model;
        var mimeType = collectionContentsExporter.getMimeType();
        var ext = mimeType.getProposedFileExtensions().getFirstElseFail();
        var fileName = collectionModel.getName().replaceAll(" ", "") + "." + ext;

        var link = new FileDownloadLink(id,
                collectionContentsExporter.getMimeType(),
                fileName, new FileModel(this, collectionModel));
        return link;
    }

    @Override
    public IModel<String> getTitleLabel() {
        return Model.of(collectionContentsExporter.getTitleLabel());
    }

    @Override
    public IModel<String> getCssClass() {
        return Model.of(collectionContentsExporter.getCssClass());
    }

    @Override
    public int orderOfAppearanceInUiDropdown() {
        return collectionContentsExporter.orderOfAppearanceInUiDropdown();
    }

    // --

    static class FileModel implements IModel<File> {
        private static final long serialVersionUID = 1L;
        private ComponentFactoryKey key;
        private EntityCollectionModel model;

        FileModel(final CollectionContentsAsExportFactory x, final EntityCollectionModel model) {
            this.key = x.key();
            this.model = model;
        }

        @Override
        public File getObject() {
            return ((CollectionContentsAsExportFactory) key.resolve(model::getServiceRegistry))
            .collectionContentsExporter.createExportFile(model.getDataTableModel().export());
        }

    }

}
