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
package org.apache.isis.extensions.viewer.wicket.pdfjs.ui.components;

import java.util.Objects;

import javax.activation.MimeType;
import javax.inject.Inject;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import org.apache.isis.applib.value.Blob;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.extensions.viewer.wicket.pdfjs.metamodel.facet.PdfJsViewerFacet;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.ComponentFactoryAbstract;
import org.apache.isis.viewer.wicket.ui.ComponentType;

import lombok.val;

@org.springframework.stereotype.Component
public class PdfJsViewerPanelComponentFactory extends ComponentFactoryAbstract {

    private static final long serialVersionUID = 1L;

    @Inject
    public PdfJsViewerPanelComponentFactory() {
        super(ComponentType.SCALAR_NAME_AND_VALUE, PdfJsViewerPanel.class);
    }

    @Override
    public ApplicationAdvice appliesTo(IModel<?> model) {
        if (!(model instanceof ScalarModel)) {
            return ApplicationAdvice.DOES_NOT_APPLY;
        }

        val scalarModel = (ScalarModel) model;
        val facet = scalarModel.getFacet(PdfJsViewerFacet.class);
        if(facet == null) {
            return ApplicationAdvice.DOES_NOT_APPLY;
        }

        val managedObject = scalarModel.getObject();
        val isPdf = isPdf(managedObject);
        return this.appliesIf(isPdf);
    }

    private static boolean isPdf(final ManagedObject objectAdapter) {
        if (objectAdapter == null) {
            return false;
        }
        final Object modelObject = objectAdapter.getPojo();
        if (!(modelObject instanceof Blob)) {
            return false;
        }
        final Blob blob = (Blob) modelObject;
        final MimeType mimeType = blob.getMimeType();
        return Objects.equals("application", mimeType.getPrimaryType()) &&
               Objects.equals("pdf", mimeType.getSubType());
    }

    @Override
    public Component createComponent(String id, IModel<?> model) {
        ScalarModel scalarModel = (ScalarModel) model;
        return new PdfJsViewerPanel(id, scalarModel);
    }
}

