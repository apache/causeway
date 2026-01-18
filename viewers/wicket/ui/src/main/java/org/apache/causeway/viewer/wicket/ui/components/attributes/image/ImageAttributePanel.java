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
package org.apache.causeway.viewer.wicket.ui.components.attributes.image;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.model.IModel;

import org.apache.causeway.applib.services.render.PlaceholderRenderService.PlaceholderLiteral;
import org.apache.causeway.viewer.commons.model.components.UiString;
import org.apache.causeway.viewer.wicket.model.models.FileUploadModels;
import org.apache.causeway.viewer.wicket.model.models.UiAttributeWkt;
import org.apache.causeway.viewer.wicket.ui.components.attributes.AttributeFragmentFactory.InputFragment;
import org.apache.causeway.viewer.wicket.ui.components.attributes.AttributePanelWithFormField;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

/**
 * Panel for rendering scalars of type {@link BufferedImage}.
 */
class ImageAttributePanel
extends AttributePanelWithFormField<BufferedImage> {

    private static final long serialVersionUID = 1L;

    public ImageAttributePanel(final String id, final UiAttributeWkt attributeModel) {
        super(id, attributeModel, BufferedImage.class);
    }

    protected IModel<List<FileUpload>> fileUploadModel() {
        return FileUploadModels.image(attributeModel());
    }

    // -- INPUT FORMAT

    @Override
    protected Optional<InputFragment> getInputFragmentType() {
        return Optional.of(InputFragment.FILE);
    }

    // generic type mismatch; no issue as long as we don't use conversion
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected FormComponent createFormComponent(final String id, final UiAttributeWkt attributeModel) {
        var initialCaption = outputFormatAsString();
        var fileUploadField = Wkt.fileUploadField(id, initialCaption, getWicketViewerSettings(), fileUploadModel());
        addAcceptFilterTo(fileUploadField);
        return fileUploadField;
    }

    // -- OUTPUT FORMAT

    @Override
    protected UiString obtainOutputFormat() {
        var caption = pojoOptional()
                .map(buffImg->"Image")
                .orElseGet(()->
                    getPlaceholderRenderService()
                    .asText(PlaceholderLiteral.NULL_REPRESENTATION));
        return UiString.text(caption);
    }

    @Override
    protected Component createComponentForOutput(final String id) {
        return new ImagePanel(id, attributeModel());
    }

    // -- HELPER

    private void addAcceptFilterTo(final Component component){
        Wkt.attributeReplace(component, "accept", attributeModel().getFileAccept());
    }

    private Optional<BufferedImage> pojoOptional() {
        return Optional.ofNullable(attributeModel().unwrapAs(type));
    }

}
