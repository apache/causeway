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
package org.apache.isis.extensions.viewer.wicket.exceldownload.ui.components;

import java.io.File;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import org.apache.isis.viewer.common.model.components.ComponentType;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.ui.CollectionContentsAsFactory;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.ComponentFactoryAbstract;

/**
 * {@link ComponentFactory} for {@link CollectionContentsAsExcel}.
 *
 * @since 2.0 {@index}
 */
@org.springframework.stereotype.Component
public class CollectionContentsAsExcelFactory
extends ComponentFactoryAbstract
implements CollectionContentsAsFactory {

    private static final long serialVersionUID = 1L;

    private static final String NAME = "excel";

    public CollectionContentsAsExcelFactory() {
        super(ComponentType.COLLECTION_CONTENTS_EXPORT, NAME);
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
        final EntityCollectionModel collectionModel = (EntityCollectionModel) model;
        return createDownloadLink(id, collectionModel);
    }

    @Override
    public IModel<String> getTitleLabel() {
        return Model.of("Excel");
    }

    @Override
    public IModel<String> getCssClass() {
        return Model.of("fa fa-file-excel");
    }

    // -EXP

    private DownloadLink createDownloadLink(final String id, final EntityCollectionModel model) {
        final LoadableDetachableModel<File> fileModel = ExcelFileModel.of(model);
        final String xlsxFileName = xlsxFileNameFor(model);
        final DownloadLink link = new ExcelFileDownloadLink(id, fileModel, xlsxFileName);
        return link;
    }

    private static String xlsxFileNameFor(final EntityCollectionModel model) {
        return model.getName().replaceAll(" ", "") + ".xlsx";
    }
}
