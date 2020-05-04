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
package org.apache.isis.viewer.wicket.ui.components.scalars.isisapplib;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import javax.activation.MimeType;
import javax.imageio.ImageIO;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.image.resource.ThumbnailImageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.image.resource.BufferedDynamicImageResource;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.IResource;

import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.NamedWithMimeType;
import org.apache.isis.core.metamodel.commons.CloseableExtensions;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract2;
import org.apache.isis.viewer.wicket.ui.components.widgets.bootstrap.FormGroup;
import org.apache.isis.viewer.wicket.ui.util.Components;

import lombok.val;

import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.fileinput.BootstrapFileInputField;

public abstract class IsisBlobOrClobPanelAbstract<T extends NamedWithMimeType> extends ScalarPanelAbstract2 {


    private static final long serialVersionUID = 1L;

    private static final String ID_SCALAR_IF_REGULAR = "scalarIfRegular";
    private static final String ID_SCALAR_IF_REGULAR_DOWNLOAD = "scalarIfRegularDownload";
    private static final String ID_FILE_NAME = "fileName";
    private static final String ID_SCALAR_IF_REGULAR_CLEAR = "scalarIfRegularClear";
    private static final String ID_SCALAR_NAME = "scalarName";
    private static final String ID_SCALAR_VALUE = "scalarValue";
    private static final String ID_IMAGE = "scalarImage";
    private static final String ID_SCALAR_IF_COMPACT = "scalarIfCompact";
    private static final String ID_SCALAR_IF_COMPACT_DOWNLOAD = "scalarIfCompactDownload";

    private Image wicketImage;

    private FileUploadField fileUploadField;
    private Label fileNameLabel;

    protected enum InputFieldVisibility {
        VISIBLE, NOT_VISIBLE
    }
    protected enum InputFieldEditability{
        EDITABLE, NOT_EDITABLE
    }

    @Override
    protected FormGroup createComponentForRegular() {
        fileUploadField = createFileUploadField(ID_SCALAR_VALUE);
        fileUploadField.setLabel(Model.of(getModel().getName()));

        final FormGroup scalarIfRegularFormGroup = new FormGroup(ID_SCALAR_IF_REGULAR, fileUploadField);
        scalarIfRegularFormGroup.add(fileUploadField);

        final Label scalarName = new Label(ID_SCALAR_NAME, getModel().getName());
        scalarIfRegularFormGroup.add(scalarName);


        wicketImage = asWicketImage(ID_IMAGE);
        if(wicketImage != null) {
            wicketImage.setOutputMarkupId(true);
            scalarIfRegularFormGroup.addOrReplace(wicketImage);
        } else {
            Components.permanentlyHide(scalarIfRegularFormGroup, ID_IMAGE);
        }

        updateFileNameLabel(ID_FILE_NAME, scalarIfRegularFormGroup);
        updateDownloadLink(ID_SCALAR_IF_REGULAR_DOWNLOAD, scalarIfRegularFormGroup);

        return scalarIfRegularFormGroup;
    }

    @Override
    protected Component getScalarValueComponent() {
        return fileUploadField;
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
    protected Component createComponentForCompact() {
        final MarkupContainer scalarIfCompact = new WebMarkupContainer(ID_SCALAR_IF_COMPACT);
        MarkupContainer downloadLink = updateDownloadLink(ID_SCALAR_IF_COMPACT_DOWNLOAD, scalarIfCompact);
        if(downloadLink != null) {
            updateFileNameLabel("fileNameIfCompact", downloadLink);
        }

        return scalarIfCompact;
    }


    // //////////////////////////////////////

    private Image asWicketImage(String id) {

        val adapter = getModel().getObject();
        if(adapter == null) {
            return null;
        }

        final Object object = adapter.getPojo();
        if(!(object instanceof Blob)) {
            return null;
        }

        final Blob blob = (Blob)object;
        final MimeType mimeType = blob.getMimeType();
        if(mimeType == null || !mimeType.getPrimaryType().equals("image")) {
            return null;
        }

        final BufferedImage image = asBufferedImage(blob);
        if(image == null) {
            return null;
        }

        final BufferedDynamicImageResource imageResource = new BufferedDynamicImageResource();
        imageResource.setImage(image);
        final ThumbnailImageResource thumbnailImageResource = new ThumbnailImageResource(imageResource, 300);

        return new NonCachingImage(id, thumbnailImageResource);
    }

    private BufferedImage asBufferedImage(final Blob blob) {
        final byte[] bytes = blob.getBytes();
        if(bytes == null) {
            return null;
        }

        final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        try {
            return ImageIO.read(bais);
        } catch (IOException ex) {
            return null;
        } finally {
            CloseableExtensions.closeSafely(bais);
        }
    }


    // //////////////////////////////////////

    @Override
    protected void onInitializeNotEditable() {
        updateRegularFormComponents(InputFieldVisibility.VISIBLE, InputFieldEditability.NOT_EDITABLE, null, null);
    }

    @Override
    protected void onInitializeReadonly(final String disableReason) {
        updateRegularFormComponents(InputFieldVisibility.VISIBLE, InputFieldEditability.NOT_EDITABLE, null, null);
    }

    @Override
    protected void onInitializeEditable() {
        updateRegularFormComponents(InputFieldVisibility.VISIBLE, InputFieldEditability.EDITABLE, null, null);
    }

    private FileUploadField createFileUploadField(String componentId) {
        final BootstrapFileInputField fileUploadField = new BootstrapFileInputField(
                componentId, new IModel<List<FileUpload>>() {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public void setObject(final List<FileUpload> fileUploads) {
                        if (fileUploads == null || fileUploads.isEmpty()) {
                            return;
                        }

                        val blob = getBlobOrClobFrom(fileUploads);
                        val objectAdapter = scalarModel.getCommonContext().getPojoToAdapter().apply(blob);
                        getModel().setObject(objectAdapter);
                    }

                    @Override
                    public void detach() {
                    }

                    @Override
                    public List<FileUpload> getObject() {
                        return null;
                    }

                });
        fileUploadField.getConfig().showUpload(false).mainClass("input-group-sm");
        return fileUploadField;
    }

    @Override
    protected void onNotEditable(final String disableReason, final AjaxRequestTarget target) {
        updateRegularFormComponents(InputFieldVisibility.VISIBLE, InputFieldEditability.NOT_EDITABLE, disableReason, target);
    }

    @Override
    protected void onEnabled(final AjaxRequestTarget target) {
        updateRegularFormComponents(InputFieldVisibility.VISIBLE, InputFieldEditability.EDITABLE, null, target);
    }

    protected abstract T getBlobOrClobFrom(final List<FileUpload> fileUploads);

    @SuppressWarnings("unchecked")
    private T getBlobOrClob(final ScalarModel model) {
        val adapter = model.getObject();
        return adapter != null? (T) adapter.getPojo(): null;
    }

    public IsisBlobOrClobPanelAbstract(String id, ScalarModel scalarModel) {
        super(id, scalarModel);
    }

    private void updateRegularFormComponents(
            final InputFieldVisibility visibility,
            final InputFieldEditability editability,
            final String disabledReason,
            final AjaxRequestTarget target) {

        MarkupContainer formComponent = (MarkupContainer) getComponentForRegular();
        sync(formComponent, visibility, editability, disabledReason, target);

        final Component component = formComponent.get(ID_SCALAR_VALUE);
        final InputFieldVisibility editingWidgetVisibility = editability == InputFieldEditability.EDITABLE ?
                InputFieldVisibility.VISIBLE :
                    InputFieldVisibility.NOT_VISIBLE;
        sync(component, editingWidgetVisibility, null, disabledReason, target);

        addAcceptFilterTo(component);
        fileNameLabel = updateFileNameLabel(ID_FILE_NAME, formComponent);

        updateClearLink(editingWidgetVisibility, null, target);

        // the visibility of download link is intentionally 'backwards';
        // if in edit mode then do NOT show
        final MarkupContainer downloadLink = updateDownloadLink(ID_SCALAR_IF_REGULAR_DOWNLOAD, formComponent);
        sync(downloadLink, visibility, editability, disabledReason, target);
        // ditto any image
        sync(wicketImage, visibility, editability, disabledReason, target);
    }

    private void sync(
            final Component component,
            final InputFieldVisibility visibility,
            final InputFieldEditability editability,
            final String disabledReason,
            final AjaxRequestTarget target) {

        if(component == null) {
            return;
        }
        component.setOutputMarkupId(true); // enable ajax link

        if(visibility != null) {
            component.setVisible(visibility == InputFieldVisibility.VISIBLE);
            Components.addToAjaxRequest(target, component);
        }


        if(editability != null) {

            //            // dynamic disablement doesn't yet work, this exception is thrown when form is submitted:
            //            //
            //            // Caused by: java.lang.IllegalStateException: ServletRequest does not contain multipart content.
            //            // One possible solution is to explicitly call Form.setMultipart(true), Wicket tries its best to
            //            // auto-detect multipart forms but there are certain situation where it cannot.
            //
            //            component.setEnabled(editability == InputFieldEditability.EDITABLE);
            //
            //            final AttributeModifier title = new AttributeModifier("title", Model.of(disabledReason != null ? disabledReason : ""));
            //            component.add(title);
            //
            //            if (target != null) {
            //                target.add(component);
            //            }

        }
    }

    private String getAcceptFilter(){
        return scalarModel.getFileAccept();
    }

    private void addAcceptFilterTo(Component component){
        final String filter = getAcceptFilter();
        if(filter==null || filter.isEmpty())
            return; // ignore
        class AcceptAttributeModel extends Model<String> {
            private static final long serialVersionUID = 1L;
            @Override
            public String getObject() {
                return filter;
            }
        }
        component.add(new AttributeModifier("accept", new AcceptAttributeModel()));
    }

    private Label updateFileNameLabel(String idFileName, MarkupContainer formComponent) {
        class FileNameModel extends Model<String> {
            private static final long serialVersionUID = 1L;
            @Override
            public String getObject() {
                T blobOrClob = getBlobOrClobFromModel();
                String fileName = blobOrClob != null? blobOrClob.getName(): "";
                return fileName;
            }
        }
        Label fileNameLabel = new Label(idFileName, new FileNameModel());
        formComponent.addOrReplace(fileNameLabel);
        fileNameLabel.setOutputMarkupId(true);
        return fileNameLabel;
    }

    private void updateClearLink(
            final InputFieldVisibility visibility,
            final InputFieldEditability editability,
            final AjaxRequestTarget target) {

        final MarkupContainer formComponent = (MarkupContainer) getComponentForRegular();
        formComponent.setOutputMarkupId(true); // enable ajax link

        final AjaxLink<Void> ajaxLink = new AjaxLink<Void>(ID_SCALAR_IF_REGULAR_CLEAR){
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                setEnabled(false);
                ScalarModel model = IsisBlobOrClobPanelAbstract.this.getModel();
                model.setObject(null);
                target.add(formComponent);
                target.add(fileNameLabel);
            }
        };
        ajaxLink.setOutputMarkupId(true);
        formComponent.addOrReplace(ajaxLink);

        final T blobOrClob = getBlobOrClobFromModel();
        final Component clearButton = formComponent.get(ID_SCALAR_IF_REGULAR_CLEAR);
        clearButton.setVisible(blobOrClob != null && visibility == InputFieldVisibility.VISIBLE);
        clearButton.setEnabled(blobOrClob != null);

        if(target != null) {
            target.add(formComponent);
            target.add(clearButton);
            target.add(ajaxLink);
        }
    }

    private MarkupContainer updateDownloadLink(String downloadId, MarkupContainer container) {
        val resourceLink = createResourceLink(downloadId);
        if(resourceLink != null) {
            container.addOrReplace(resourceLink);
        } else {
            Components.permanentlyHide(container, downloadId);
        }
        return resourceLink;
    }

    private ResourceLinkVolatile createResourceLink(String id) {
        final T blob = getBlobOrClobFromModel();
        if(blob == null) {
            return null;
        }
        val resource = newResource(blob);
        return new ResourceLinkVolatile(id, resource);
    }

    private T getBlobOrClobFromModel() {
        return getBlobOrClob(getModel());
    }


    /**
     * Mandatory hook method.
     */
    protected abstract IResource newResource(final T namedWithMimeType);


    @Override
    protected String getScalarPanelType() {
        return "isisBlobPanel";
    }


}
