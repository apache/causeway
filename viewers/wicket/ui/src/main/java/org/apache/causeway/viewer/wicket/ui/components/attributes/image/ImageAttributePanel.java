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
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.ByteArrayResource;
import org.apache.wicket.request.resource.IResource;

import org.apache.causeway.applib.services.placeholder.PlaceholderRenderService.PlaceholderLiteral;
import org.apache.causeway.viewer.commons.model.components.UiString;
import org.apache.causeway.viewer.wicket.model.models.FileUploadModels;
import org.apache.causeway.viewer.wicket.model.models.UiAttributeWkt;
import org.apache.causeway.viewer.wicket.ui.components.attributes.AttributeFragmentFactory.CompactFragment;
import org.apache.causeway.viewer.wicket.ui.components.attributes.AttributeFragmentFactory.InputFragment;
import org.apache.causeway.viewer.wicket.ui.components.attributes.AttributePanelWithFormField;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.WktTooltips;

/**
 * Panel for rendering scalars of type {@link BufferedImage}.
 */
class ImageAttributePanel
extends AttributePanelWithFormField<BufferedImage> {

    private static final long serialVersionUID = 1L;

    private IModel<BufferedImage> unwrapped;

    public ImageAttributePanel(final String id, final UiAttributeWkt attributeModel) {
        super(id, attributeModel, BufferedImage.class);
        this.unwrapped = attributeModel.unwrapped(type);
    }

    protected IModel<List<FileUpload>> fileUploadModel() {
        return FileUploadModels.blob(attributeModel());
    }

    protected IResource newResource(final BufferedImage image) {
        var bytes = _ImageUtils.getImageData(image);
        return new ByteArrayResource(_ImageUtils.getMimeTypeFromBytes(bytes), _ImageUtils.getImageData(image), "Image");
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
        var fileUploadField = Wkt.fileUploadField(id, initialCaption, fileUploadModel());
        addAcceptFilterTo(fileUploadField);
        return fileUploadField;
    }

    // -- OUTPUT FORMAT

    @Override
    protected UiString obtainOutputFormat() {
        var caption = pojoOptional()
                .map(_ImageUtils::getName)
                .orElseGet(()->
                    getPlaceholderRenderService()
                    .asText(PlaceholderLiteral.NULL_REPRESENTATION));
        return UiString.text(caption);
    }

    @Override
    protected Component createComponentForOutput(final String id) {
//        return _Image.asWicketImage(id, attributeModel())
//            .map(Component.class::cast)
//            .orElseGet(()->Wkt.label(id, "no image"));
        
        //TODO[causeway-viewer-wicket-ui-CAUSEWAY-3851] don't handle attribute labels twice (here and in ImagePanel)
        return new ImagePanel(id, attributeModel());
        
        //TODO[causeway-viewer-wicket-ui-CAUSEWAY-3851] a download-link is actually useful for images
//        var link = CompactFragment.LINK
//                .createFragment(id, this, scalarValueId->
//                    createDownloadLink(scalarValueId, this::outputFormatAsString));
//        return link;
    }

    // -- HELPER

    private void addAcceptFilterTo(final Component component){
        Wkt.attributeReplace(component, "accept", attributeModel().getFileAccept());
    }

    private Optional<BufferedImage> pojoOptional() {
        return Optional.ofNullable(unwrapped.getObject());
    }

    private Component createDownloadLink(final String id, final IModel<String> labelModel) {
        return pojoOptional()
            .map(this::newResource)
            .map(resource->(MarkupContainer)Wkt.downloadLinkNoCache(id, resource))
            .<Component>map(linkContainer->{
                WktTooltips.addTooltip(linkContainer, translate("Download file"));
                Wkt.labelAdd(linkContainer, CompactFragment.ID_LINK_LABEL, labelModel);
                return linkContainer;
            })
            .orElseGet(()->{
                // represent null reference by a simple markup displaying '(none)'
                var linkContainer = Wkt.container(id);
                Wkt.markupAdd(linkContainer, CompactFragment.ID_LINK_LABEL,
                        getPlaceholderRenderService()
                        .asHtml(PlaceholderLiteral.NULL_REPRESENTATION));
                return linkContainer;
            });
    }

}
