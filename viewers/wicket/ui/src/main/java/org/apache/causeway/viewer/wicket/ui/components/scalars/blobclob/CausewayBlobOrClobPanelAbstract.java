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
import org.apache.causeway.viewer.wicket.model.models.ScalarModel;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.CompactFragment;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.InputFragment;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ScalarPanelFormFieldAbstract;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.WktTooltips;

import lombok.val;

public abstract class CausewayBlobOrClobPanelAbstract<T extends NamedWithMimeType>
extends ScalarPanelFormFieldAbstract<T> {

    private static final long serialVersionUID = 1L;

    /** Model that maps to either {@link Blob} or {@link Clob} */
    private IModel<T> unwrapped;

    protected CausewayBlobOrClobPanelAbstract(final String id, final ScalarModel scalarModel, final Class<T> type) {
        super(id, scalarModel, type);
        this.unwrapped = scalarModel.unwrapped(type);
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
    protected FormComponent createFormComponent(final String id, final ScalarModel scalarModel) {
        val initialCaption = outputFormatAsString();
        val fileUploadField = Wkt.fileUploadField(id, initialCaption, fileUploadModel());
        addAcceptFilterTo(fileUploadField);
        return fileUploadField;
    }

    // -- OUTPUT FORMAT

    @Override
    protected UiString obtainOutputFormat() {
        val caption = getBlobOrClobFromModel()
                .map(NamedWithMimeType::getName)
                .orElseGet(()->
                    getPlaceholderRenderService()
                    .asText(PlaceholderLiteral.NULL_REPRESENTATION));
        return UiString.text(caption);
    }

    @Override
    protected Component createComponentForOutput(final String id) {
        val link = CompactFragment.LINK
                .createFragment(id, this, scalarValueId->
                    createDownloadLink(scalarValueId, this::outputFormatAsString));
        return link;
    }

    // -- HELPER

    private void addAcceptFilterTo(final Component component){
        Wkt.attributeReplace(component, "accept", scalarModel().getFileAccept());
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
            val linkContainer = Wkt.container(id);
            Wkt.markupAdd(linkContainer, CompactFragment.ID_LINK_LABEL,
                    getPlaceholderRenderService()
                    .asHtml(PlaceholderLiteral.NULL_REPRESENTATION));
            return linkContainer;
        });
    }

    // -- LEGACY

//    private static final String ID_SCALAR_IF_REGULAR_DOWNLOAD = "scalarIfRegularDownload";
//    private static final String ID_FILE_NAME = "fileName";
//    private static final String ID_SCALAR_IF_REGULAR_CLEAR = "scalarIfRegularClear";
//    private static final String ID_IMAGE = "scalarImage";
//    private static final String ID_SCALAR_IF_COMPACT_DOWNLOAD = "scalarIfCompactDownload";
//
//    private Image wicketImage;
//    private Label fileNameLabel;

//  @Override
//  protected void onFormGroupCreated(final FormGroup formGroup) {
//      super.onFormGroupCreated(formGroup);
//      wicketImage = asWicketImage(ID_IMAGE);
//      if(wicketImage != null) {
//          formGroup.addOrReplace(wicketImage);
//      } else {
//          WktComponents.permanentlyHide(formGroup, ID_IMAGE);
//      }
//      createFileNameLabel(ID_FILE_NAME, formGroup);
//      createDownloadLink(ID_SCALAR_IF_REGULAR_DOWNLOAD, formGroup);
//  }

//    private void updateRegularFormComponents(
//            final ScalarRenderMode renderMode,
//            final String disabledReason,
//            final Optional<AjaxRequestTarget> target) {
//
//        final MarkupContainer formComponent = getRegularFrame();
//        setRenderModeOn(formComponent, renderMode, disabledReason, target);
//
//        final Component scalarValueComponent = formComponent.get(ID_SCALAR_VALUE);
//        final ScalarRenderMode editingWidgetVisibility = renderMode.isEditing()
//                ? ScalarRenderMode.EDITING
//                        : ScalarRenderMode.HIDING;
//        setRenderModeOn(scalarValueComponent, editingWidgetVisibility, disabledReason, target);
//
//        addAcceptFilterTo(scalarValueComponent);
//        fileNameLabel = createFileNameLabel(ID_FILE_NAME, formComponent);
//
//        createClearLink(editingWidgetVisibility, target);
//
//        // the visibility of download link is intentionally 'backwards';
//        // if in edit mode then do NOT show
//        final MarkupContainer downloadLink = createDownloadLink(ID_SCALAR_IF_REGULAR_DOWNLOAD, formComponent);
//        setRenderModeOn(downloadLink, renderMode, disabledReason, target);
//        // ditto any image
//        setRenderModeOn(wicketImage, renderMode, disabledReason, target);
//    }

//    private void setRenderModeOn(
//            final @Nullable Component component,
//            final @NonNull  ScalarRenderMode renderMode,
//            final @Nullable String disabledReason,
//            final @NonNull  Optional<AjaxRequestTarget> target) {
//
//        if(component==null) return;
//
//        component.setOutputMarkupId(true); // enable ajax link
//        component.setVisible(renderMode.isVisible());
//        target.ifPresent(ajax->{
//            WktComponents.addToAjaxRequest(ajax, component);
//        });
//    }

//    private Label createFileNameLabel(final String idFileName, final MarkupContainer formComponent) {
//        val fileNameLabel = Wkt.labelAdd(formComponent, idFileName, ()->
//        getBlobOrClobFromModel()
//        .map(NamedWithMimeType::getName)
//        .orElse(""));
//
//        fileNameLabel.setOutputMarkupId(true);
//        return fileNameLabel;
//    }

//    private void createClearLink(
//            final ScalarRenderMode renderMode,
//            final Optional<AjaxRequestTarget> target) {
//
//        final MarkupContainer formComponent = getRegularFrame();
//
//        final AjaxLink<Void> ajaxLink = Wkt.linkAdd(formComponent, ID_SCALAR_IF_REGULAR_CLEAR, ajaxTarget->{
//            setEnabled(false);
//            ScalarModel model = CausewayBlobOrClobPanelAbstract.this.getModel();
//            model.setObject(null);
//            ajaxTarget.add(formComponent);
//            ajaxTarget.add(fileNameLabel);
//        });
//        ajaxLink.setOutputMarkupId(true);
//
//        final Optional<T> blobOrClob = getBlobOrClobFromModel();
//        final Component clearButton = formComponent.get(ID_SCALAR_IF_REGULAR_CLEAR);
//        clearButton.setVisible(blobOrClob.isPresent() && renderMode.isVisible());
//        clearButton.setEnabled(blobOrClob.isPresent());
//
//        target.ifPresent(ajax->{
//            ajax.add(formComponent);
//            ajax.add(clearButton);
//            ajax.add(ajaxLink);
//        });
//    }

//    private MarkupContainer createDownloadLink(final String id, final MarkupContainer parent) {
//        return getBlobOrClobFromModel()
//                .map(this::newResource)
//                .map(resource->Wkt.downloadLinkNoCache(id, resource))
//                .map(peek(downloadLink->{
//                    parent.addOrReplace(downloadLink);
//                    WktTooltips.addTooltip(downloadLink, "download");
//                }))
//                .orElseGet(()->{
//                    WktComponents.permanentlyHide(parent, id);
//                    return null;
//                });
//    }



//    private Image asWicketImage(final String id) {
//        val blob = scalarModel().unwrapped(Blob.class).getObject();
//        return WicketImageUtil.asWicketImage(id, blob).orElse(null);
//    }

}
