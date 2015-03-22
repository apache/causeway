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

import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.fileinput.BootstrapFileInputField;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import javax.activation.MimeType;
import javax.imageio.ImageIO;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.extensions.markup.html.image.resource.ThumbnailImageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.image.resource.BufferedDynamicImageResource;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.IResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.NamedWithMimeType;
import org.apache.isis.core.commons.lang.CloseableExtensions;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions.EntityActionUtil;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract;
import org.apache.isis.viewer.wicket.ui.components.widgets.bootstrap.FormGroup;
import org.apache.isis.viewer.wicket.ui.util.Components;

public abstract class IsisBlobOrClobPanelAbstract<T extends NamedWithMimeType> extends ScalarPanelAbstract {


    private static final long serialVersionUID = 1L;
    
    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(IsisBlobOrClobPanelAbstract.class);
    
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

    @Override
    protected FormGroup addComponentForRegular() {
        fileUploadField = createFileUploadField(ID_SCALAR_VALUE);
        fileUploadField.setLabel(Model.of(getModel().getName()));
        
        final FormGroup labelIfRegular = new FormGroup(ID_SCALAR_IF_REGULAR, fileUploadField);
        labelIfRegular.add(fileUploadField);
    
        final Label scalarName = new Label(ID_SCALAR_NAME, getModel().getName());
        labelIfRegular.add(scalarName);

        // find the links...
        final List<LinkAndLabel> entityActions = EntityActionUtil.getEntityActionLinksForAssociation(this.scalarModel, getDeploymentType());

        addPositioningCssTo(labelIfRegular, entityActions);

        wicketImage = asWicketImage(ID_IMAGE);
        if(wicketImage != null) {
            wicketImage.setOutputMarkupId(true);
            labelIfRegular.addOrReplace(wicketImage);
        } else {
            Components.permanentlyHide(labelIfRegular, ID_IMAGE);
        }
        
        updateFileNameLabel(ID_FILE_NAME, labelIfRegular);
        updateDownloadLink(ID_SCALAR_IF_REGULAR_DOWNLOAD, labelIfRegular);
        
        addOrReplace(labelIfRegular);
        addFeedbackTo(labelIfRegular, fileUploadField);

        // ... add entity links to panel (below and to right)
        addEntityActionLinksBelowAndRight(labelIfRegular, entityActions);

        return labelIfRegular;
    }

    private Image asWicketImage(String id) {
        
        final ObjectAdapter adapter = getModel().getObject();
        if(adapter == null) {
            return null;
        }
        
        final Object object = adapter.getObject();
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
        
        final NonCachingImage wicketImage = new NonCachingImage(id, thumbnailImageResource);
        return wicketImage;
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

    @Override
    protected Component addComponentForCompact() {
        final MarkupContainer scalarIfCompact = new WebMarkupContainer(ID_SCALAR_IF_COMPACT);
        MarkupContainer downloadLink = updateDownloadLink(ID_SCALAR_IF_COMPACT_DOWNLOAD, scalarIfCompact);
        if(downloadLink != null) {
            updateFileNameLabel("fileNameIfCompact", downloadLink);
        }
        addOrReplace(scalarIfCompact);
        return scalarIfCompact;
    }

    protected void onBeforeRenderWhenViewMode() {
        updateRegularFormComponents(InputFieldVisibility.NOT_VISIBLE);
    }

    protected void onBeforeRenderWhenDisabled(final String disableReason) {
        updateRegularFormComponents(InputFieldVisibility.NOT_VISIBLE);
    }

    protected void onBeforeRenderWhenEnabled() {
        updateRegularFormComponents(InputFieldVisibility.VISIBLE);
    }

    private FileUploadField createFileUploadField(String componentId) {
        final BootstrapFileInputField fileUploadField = new BootstrapFileInputField(componentId, new IModel<List<FileUpload>>() {
    
            private static final long serialVersionUID = 1L;
    
            @Override
            public void setObject(final List<FileUpload> fileUploads) {
                if (fileUploads == null || fileUploads.isEmpty()) {
                    return;
                }
                
                final T blob = getBlobOrClobFrom(fileUploads);
                
                final ObjectAdapter adapter = getAdapterManager().adapterFor(blob);
                getModel().setObject(adapter);
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

    protected abstract T getBlobOrClobFrom(final List<FileUpload> fileUploads);

    @SuppressWarnings("unchecked")
    private T getBlobOrClob(final ScalarModel model) {
        ObjectAdapter adapter = model.getObject();
        return adapter != null? (T) adapter.getObject(): null;
    }

    public IsisBlobOrClobPanelAbstract(String id, ScalarModel scalarModel) {
        super(id, scalarModel);
    }

    private void updateRegularFormComponents(final InputFieldVisibility visibility) {
        MarkupContainer formComponent = (MarkupContainer) getComponentForRegular();
        formComponent.get(ID_SCALAR_VALUE).setVisible(visibility == InputFieldVisibility.VISIBLE);

        fileNameLabel = updateFileNameLabel(ID_FILE_NAME, formComponent);

        updateClearLink(visibility);

        // the visibility of download link is intentionally 'backwards';
        // if in edit mode then do NOT show
        final MarkupContainer downloadLink = updateDownloadLink(ID_SCALAR_IF_REGULAR_DOWNLOAD, formComponent);
        if (downloadLink != null) {
            downloadLink.setVisible(visibility == InputFieldVisibility.NOT_VISIBLE);
        }
        // ditto any image
        if(wicketImage != null) {
            wicketImage.setVisible(visibility == InputFieldVisibility.NOT_VISIBLE);
        }
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

    private void updateClearLink(InputFieldVisibility visibility) {
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
        formComponent.get(ID_SCALAR_IF_REGULAR_CLEAR).setVisible(blobOrClob != null && visibility == InputFieldVisibility.VISIBLE);
    }

    private MarkupContainer updateDownloadLink(String downloadId, MarkupContainer container) {
        final ResourceLink<?> resourceLink = createResourceLink(downloadId);
        if(resourceLink != null) {
            container.addOrReplace(resourceLink);
        } else {
            Components.permanentlyHide(container, downloadId);
        }
        return resourceLink;
    }

    private ResourceLink<?> createResourceLink(String id) {
        final T blob = getBlobOrClobFromModel();
        if(blob == null) {
            return null;
        }
        final IResource bar = newResource(blob);
        return new ResourceLink<Object>(id, bar);
    }

    private T getBlobOrClobFromModel() {
        return getBlobOrClob(getModel());
    }

    @Override
    protected void addFormComponentBehavior(Behavior behavior) {
        fileUploadField.add(behavior);
    }

    
    /**
     * Mandatory hook method.
     */
    protected abstract IResource newResource(final T namedWithMimeType);


}
