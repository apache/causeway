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
package org.apache.causeway.viewer.wicket.ui.components.scalars.blobclob;

import java.util.List;
import java.util.Optional;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.IResource;

import org.apache.causeway.applib.services.placeholder.PlaceholderRenderService.PlaceholderLiteral;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.Clob;
import org.apache.causeway.applib.value.NamedWithMimeType;
import org.apache.causeway.viewer.commons.model.components.UiString;
import org.apache.causeway.viewer.wicket.model.models.UiAttributeWkt;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.CompactFragment;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.InputFragment;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ScalarPanelFormFieldAbstract;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.WktTooltips;

abstract class CausewayBlobOrClobPanelAbstract<T extends NamedWithMimeType>
extends ScalarPanelFormFieldAbstract<T> {

    private static final long serialVersionUID = 1L;

    /** Model that maps to either {@link Blob} or {@link Clob} */
    private IModel<T> unwrapped;

    protected CausewayBlobOrClobPanelAbstract(final String id, final UiAttributeWkt attributeModel, final Class<T> type) {
        super(id, attributeModel, type);
        this.unwrapped = attributeModel.unwrapped(type);
    }

    protected abstract IModel<List<FileUpload>> fileUploadModel();
    protected abstract IResource newResource(final T namedWithMimeType);

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
        var caption = getBlobOrClobFromModel()
                .map(NamedWithMimeType::getName)
                .orElseGet(()->
                    getPlaceholderRenderService()
                    .asText(PlaceholderLiteral.NULL_REPRESENTATION));
        return UiString.text(caption);
    }

    @Override
    protected Component createComponentForOutput(final String id) {
        var link = CompactFragment.LINK
                .createFragment(id, this, scalarValueId->
                    createDownloadLink(scalarValueId, this::outputFormatAsString));
        return link;
    }

    // -- HELPER

    private void addAcceptFilterTo(final Component component){
        Wkt.attributeReplace(component, "accept", attributeModel().getFileAccept());
    }

    private Optional<T> getBlobOrClobFromModel() {
        return Optional.ofNullable(unwrapped.getObject());
    }

    private Component createDownloadLink(final String id, final IModel<String> labelModel) {
        return getBlobOrClobFromModel()
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
