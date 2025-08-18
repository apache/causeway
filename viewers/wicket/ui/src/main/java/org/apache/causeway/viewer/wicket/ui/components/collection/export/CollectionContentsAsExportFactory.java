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
package org.apache.causeway.viewer.wicket.ui.components.collection.export;

import java.io.File;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.file.Files;

import org.apache.causeway.applib.tabular.TabularExporter;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.core.metamodel.tabular.simple.DataTable;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.model.models.coll.CollectionModel;
import org.apache.causeway.viewer.wicket.ui.CollectionContentsAsFactory;
import org.apache.causeway.viewer.wicket.ui.ComponentFactory;
import org.apache.causeway.viewer.wicket.ui.ComponentFactoryAbstract;
import org.apache.causeway.viewer.wicket.ui.app.registry.ComponentFactoryKey;
import org.apache.causeway.viewer.wicket.ui.components.download.FileDownloadLink;

import lombok.SneakyThrows;

/**
 * {@link ComponentFactory} for {@link DownloadLink}.
 *
 * @since 2.0 {@index}
 */
@org.springframework.stereotype.Component
public class CollectionContentsAsExportFactory
extends ComponentFactoryAbstract
implements CollectionContentsAsFactory {

    private final TabularExporter tabularExporter;

    public CollectionContentsAsExportFactory(final TabularExporter collectionContentsExporter) {
        super(UiComponentType.COLLECTION_CONTENTS_EXPORT, collectionContentsExporter.getClass().getName(), DownloadLink.class);
        this.tabularExporter = collectionContentsExporter;
    }

    @Override
    public ApplicationAdvice appliesTo(final IModel<?> model) {
        return (model instanceof CollectionModel collectionModel)
            ? tabularExporter.appliesTo(collectionModel.getElementType().getCorrespondingClass())
                ? ApplicationAdvice.APPLIES
                : ApplicationAdvice.DOES_NOT_APPLY
            : ApplicationAdvice.DOES_NOT_APPLY;
    }

    @Override
    public Component createComponent(final String id, final IModel<?> model) {
        var collectionModel = (CollectionModel) model;
        var mimeType = tabularExporter.getMimeType();
        var ext = mimeType.proposedFileExtensions().getFirstElseFail();
        var fileName = collectionModel.getName().replaceAll(" ", "") + "." + ext;

        var link = new FileDownloadLink(id,
                mimeType,
                fileName, new FileModel(this, collectionModel, fileName));
        return link;
    }

    @Override
    public IModel<String> getTitleLabel() {
        return Model.of(tabularExporter.getTitleLabel());
    }

    @Override
    public IModel<String> getCssClass() {
        return Model.of(tabularExporter.getCssClass());
    }

    @Override
    public int orderOfAppearanceInUiDropdown() {
        return tabularExporter.orderOfAppearanceInUiDropdown();
    }

    // --

    static class FileModel implements IModel<File> {
        private static final long serialVersionUID = 1L;
        private ComponentFactoryKey key;
        private CollectionModel model;
        private String fileName;

        FileModel(final CollectionContentsAsExportFactory x, final CollectionModel model, final String fileName) {
            this.key = x.key();
            this.model = model;
            this.fileName = fileName;
        }

        @Override @SneakyThrows
        public File getObject() {
            var tempFile = File.createTempFile(CollectionContentsAsExportFactory.class.getCanonicalName(), fileName);
            Try.run(()->
                exporter().export(model.getDataTableModel().export().toTabularSheet(DataTable.AccessMode.USER), tempFile))
            .ifFailure(__->{
                Files.remove(tempFile); // cleanup after sad case
            })
            .ifFailureFail(); // rethrow
            return tempFile;
        }

        private TabularExporter exporter() {
            return ((CollectionContentsAsExportFactory) key.componentFactory()).tabularExporter;
        }

    }

}
