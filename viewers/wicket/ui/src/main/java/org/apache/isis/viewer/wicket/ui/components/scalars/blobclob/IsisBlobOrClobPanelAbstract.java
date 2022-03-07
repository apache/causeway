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
package org.apache.isis.viewer.wicket.ui.components.scalars.blobclob;

import java.util.List;
import java.util.Optional;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.IResource;
import org.springframework.lang.Nullable;

import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.NamedWithMimeType;
import org.apache.isis.core.metamodel.render.ScalarRenderMode;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelFormFieldAbstract;
import org.apache.isis.viewer.wicket.ui.components.scalars.image.WicketImageUtil;
import org.apache.isis.viewer.wicket.ui.components.widgets.bootstrap.FormGroup;
import org.apache.isis.viewer.wicket.ui.util.Components;
import org.apache.isis.viewer.wicket.ui.util.Tooltips;
import org.apache.isis.viewer.wicket.ui.util.Wkt;

import static org.apache.isis.commons.internal.functions._Functions.peek;

import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.fileinput.BootstrapFileInputField;
import lombok.NonNull;
import lombok.val;

public abstract class IsisBlobOrClobPanelAbstract<T extends NamedWithMimeType>
extends ScalarPanelFormFieldAbstract<T> {

    private static final long serialVersionUID = 1L;

    private static final String ID_SCALAR_IF_REGULAR_DOWNLOAD = "scalarIfRegularDownload";
    private static final String ID_FILE_NAME = "fileName";
    private static final String ID_SCALAR_IF_REGULAR_CLEAR = "scalarIfRegularClear";
    private static final String ID_IMAGE = "scalarImage";
    private static final String ID_SCALAR_IF_COMPACT_DOWNLOAD = "scalarIfCompactDownload";

    private Image wicketImage;
    private FileUploadField fileUploadField;
    private Label fileNameLabel;
    private IModel<T> unwrapped;

    protected IsisBlobOrClobPanelAbstract(final String id, final ScalarModel scalarModel, final Class<T> type) {
        super(id, scalarModel, type);
        this.unwrapped = scalarModel.unwrapped(type);
    }

    // generic type mismatch; no issue as long as we don't use conversion
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected FormComponent createFormComponent(final String id, final ScalarModel scalarModel) {
        fileUploadField = createFileUploadField(id);
        return fileUploadField;
    }

    @Override
    protected void onFormGroupCreated(final FormGroup formGroup) {
        super.onFormGroupCreated(formGroup);
        wicketImage = asWicketImage(ID_IMAGE);
        if(wicketImage != null) {
            formGroup.addOrReplace(wicketImage);
        } else {
            Components.permanentlyHide(formGroup, ID_IMAGE);
        }
        createFileNameLabel(ID_FILE_NAME, formGroup);
        createDownloadLink(ID_SCALAR_IF_REGULAR_DOWNLOAD, formGroup);
    }

    // //////////////////////////////////////

    /**
     * Inline prompts are <i>not</i> supported by this component.
     */
    @Override
    protected InlinePromptConfig getInlinePromptConfig() {
        return InlinePromptConfig.notSupported();
    }

    // //////////////////////////////////////

    @Override
    protected MarkupContainer createShallowComponentForRegular() {
        val shallowRegular = super.createShallowComponentForRegular();
        Components.permanentlyHide(shallowRegular,
                ID_IMAGE, ID_SCALAR_NAME, ID_SCALAR_VALUE, ID_FEEDBACK,
                ID_ASSOCIATED_ACTION_LINKS_BELOW, ID_ASSOCIATED_ACTION_LINKS_RIGHT);
        return shallowRegular;
    }

    @Override
    protected Component createComponentForCompact(final String id) {
        final MarkupContainer scalarIfCompact = new WebMarkupContainer(id);
        createDownloadLink(ID_SCALAR_IF_COMPACT_DOWNLOAD, scalarIfCompact);
//        if(downloadLink != null) {
//            updateFileNameLabel(ID_FILE_NAME_IF_COMPACT, downloadLink);
//            Components.permanentlyHide(downloadLink, ID_FILE_NAME_IF_COMPACT);
//        }
        return scalarIfCompact;
    }

    // //////////////////////////////////////

    @Override
    protected void onInitializeNotEditable() {
        updateRegularFormComponents(ScalarRenderMode.VIEWING, null, Optional.empty());
    }

    @Override
    protected void onInitializeReadonly(final String disableReason) {
        updateRegularFormComponents(ScalarRenderMode.VIEWING, null, Optional.empty());
    }

    @Override
    protected void onInitializeEditable() {
        updateRegularFormComponents(ScalarRenderMode.EDITING, null, Optional.empty());
    }

    private FileUploadField createFileUploadField(final String componentId) {
        val fileUploadField = new BootstrapFileInputField(componentId, fileUploadModel());
        fileUploadField.getConfig().showUpload(false).mainClass("input-group-sm");
        return fileUploadField;
    }

    @Override
    protected void onNotEditable(final String disableReason, final Optional<AjaxRequestTarget> target) {
        updateRegularFormComponents(ScalarRenderMode.VIEWING, disableReason, target);
    }

    @Override
    protected void onEditable(final Optional<AjaxRequestTarget> target) {
        updateRegularFormComponents(ScalarRenderMode.VIEWING, null, target);
    }

    protected abstract IModel<List<FileUpload>> fileUploadModel();
    protected abstract IResource newResource(final T namedWithMimeType);

    // -- HELPER

    private void updateRegularFormComponents(
            final ScalarRenderMode renderMode,
            final String disabledReason,
            final Optional<AjaxRequestTarget> target) {

        final MarkupContainer formComponent = getComponentForRegular();
        setRenderModeOn(formComponent, renderMode, disabledReason, target);

        final Component scalarValueComponent = formComponent.get(ID_SCALAR_VALUE);
        final ScalarRenderMode editingWidgetVisibility = renderMode.isEditing()
                ? ScalarRenderMode.EDITING
                : ScalarRenderMode.HIDING;
        setRenderModeOn(scalarValueComponent, editingWidgetVisibility, disabledReason, target);

        addAcceptFilterTo(scalarValueComponent);
        fileNameLabel = createFileNameLabel(ID_FILE_NAME, formComponent);

        createClearLink(editingWidgetVisibility, target);

        // the visibility of download link is intentionally 'backwards';
        // if in edit mode then do NOT show
        final MarkupContainer downloadLink = createDownloadLink(ID_SCALAR_IF_REGULAR_DOWNLOAD, formComponent);
        setRenderModeOn(downloadLink, renderMode, disabledReason, target);
        // ditto any image
        setRenderModeOn(wicketImage, renderMode, disabledReason, target);
    }

    private void setRenderModeOn(
            final @Nullable Component component,
            final @NonNull  ScalarRenderMode renderMode,
            final @Nullable String disabledReason,
            final @NonNull  Optional<AjaxRequestTarget> target) {

        if(component==null) return;

        component.setOutputMarkupId(true); // enable ajax link
        component.setVisible(renderMode.isVisible());
        target.ifPresent(ajax->{
            Components.addToAjaxRequest(ajax, component);
        });

//        // dynamic disablement doesn't yet work, this exception is thrown when form is submitted:
//        //
//        // Caused by: java.lang.IllegalStateException: ServletRequest does not contain multipart content.
//        // One possible solution is to explicitly call Form.setMultipart(true), Wicket tries its best to
//        // auto-detect multipart forms but there are certain situation where it cannot.
//
//        component.setEnabled(editability == InputFieldEditability.EDITABLE);
//
//        final AttributeModifier title = new AttributeModifier("title", Model.of(disabledReason != null ? disabledReason : ""));
//        component.add(title);
//
//        if (target != null) {
//            target.add(component);
//        }

    }

    private void addAcceptFilterTo(final Component component){
        Wkt.attributeReplace(component, "accept", scalarModel().getFileAccept());
    }

    private Label createFileNameLabel(final String idFileName, final MarkupContainer formComponent) {
        val fileNameLabel = Wkt.labelAdd(formComponent, idFileName, ()->
            getBlobOrClobFromModel()
            .map(NamedWithMimeType::getName)
            .orElse(""));

        fileNameLabel.setOutputMarkupId(true);
        return fileNameLabel;
    }

    private void createClearLink(
            final ScalarRenderMode renderMode,
            final Optional<AjaxRequestTarget> target) {

        final MarkupContainer formComponent = getComponentForRegular();

        final AjaxLink<Void> ajaxLink = Wkt.linkAdd(formComponent, ID_SCALAR_IF_REGULAR_CLEAR, ajaxTarget->{
            setEnabled(false);
            ScalarModel model = IsisBlobOrClobPanelAbstract.this.getModel();
            model.setObject(null);
            ajaxTarget.add(formComponent);
            ajaxTarget.add(fileNameLabel);
        });
        ajaxLink.setOutputMarkupId(true);

        final Optional<T> blobOrClob = getBlobOrClobFromModel();
        final Component clearButton = formComponent.get(ID_SCALAR_IF_REGULAR_CLEAR);
        clearButton.setVisible(blobOrClob.isPresent() && renderMode.isVisible());
        clearButton.setEnabled(blobOrClob.isPresent());

        target.ifPresent(ajax->{
            ajax.add(formComponent);
            ajax.add(clearButton);
            ajax.add(ajaxLink);
        });
    }

    private MarkupContainer createDownloadLink(final String id, final MarkupContainer parent) {
        return getBlobOrClobFromModel()
        .map(this::newResource)
        .map(resource->Wkt.downloadLinkNoCache(id, resource))
        .map(peek(downloadLink->{
            parent.addOrReplace(downloadLink);
            Tooltips.addTooltip(downloadLink, "download");
        }))
        .orElseGet(()->{
            Components.permanentlyHide(parent, id);
            return null;
        });
    }

    private Optional<T> getBlobOrClobFromModel() {
        return Optional.ofNullable(unwrapped.getObject());
    }

    private Image asWicketImage(final String id) {
        val blob = scalarModel().unwrapped(Blob.class).getObject();
        return WicketImageUtil.asWicketImage(id, blob).orElse(null);
    }

}
