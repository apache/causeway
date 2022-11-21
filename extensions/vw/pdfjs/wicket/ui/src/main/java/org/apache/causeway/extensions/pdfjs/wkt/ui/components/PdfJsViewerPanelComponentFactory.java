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
package org.apache.causeway.extensions.pdfjs.wkt.ui.components;

import javax.inject.Inject;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.extensions.pdfjs.metamodel.facet.PdfJsViewerFacet;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.model.models.ScalarModel;
import org.apache.causeway.viewer.wicket.ui.ComponentFactoryAbstract;

import lombok.val;

@org.springframework.stereotype.Component
public class PdfJsViewerPanelComponentFactory extends ComponentFactoryAbstract {

    private static final long serialVersionUID = 1L;

    @Inject
    public PdfJsViewerPanelComponentFactory() {
        super(UiComponentType.SCALAR_NAME_AND_VALUE, PdfJsViewerPanel.class);
    }

    @Override
    public ApplicationAdvice appliesTo(final IModel<?> model) {
        if (!(model instanceof ScalarModel)) {
            return ApplicationAdvice.DOES_NOT_APPLY;
        }

        val scalarModel = (ScalarModel) model;
        if(!scalarModel.getMetaModel().containsFacet(PdfJsViewerFacet.class)) {
            return ApplicationAdvice.DOES_NOT_APPLY;
        }

        return appliesIf(isPdf(scalarModel.getObject()));
    }

    private static boolean isPdf(final ManagedObject objectAdapter) {
        if (objectAdapter == null) {
            return false;
        }
        final Object objectPojo = objectAdapter.getPojo();
        if (!(objectPojo instanceof Blob)) {
            return false;
        }
        final Blob blob = (Blob) objectPojo;
        return CommonMimeType.PDF.matches(blob.getMimeType());
    }

    @Override
    public Component createComponent(final String id, final IModel<?> model) {
        return new PdfJsViewerPanel(id, (ScalarModel) model);
    }
}

