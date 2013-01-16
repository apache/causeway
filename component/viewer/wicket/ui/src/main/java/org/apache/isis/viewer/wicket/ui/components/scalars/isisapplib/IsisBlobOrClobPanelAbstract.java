package org.apache.isis.viewer.wicket.ui.components.scalars.isisapplib;

import java.util.List;

import org.apache.isis.applib.value.NamedWithMimeType;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.Components;
import org.apache.log4j.Logger;
import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.ComponentFeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.IResource;

public abstract class IsisBlobOrClobPanelAbstract<T extends NamedWithMimeType> extends ScalarPanelAbstract {

    private static final long serialVersionUID = 1L;
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(IsisBlobOrClobPanelAbstract.class);
    
    private static final String ID_SCALAR_IF_REGULAR = "scalarIfRegular";
    private static final String ID_SCALAR_IF_REGULAR_UPLOAD = "scalarIfRegularUpload";
    private static final String ID_SCALAR_IF_REGULAR_DOWNLOAD = "scalarIfRegularDownload";
    private static final String ID_SCALAR_IF_REGULAR_CLEAR = "scalarIfRegularClear";
    private static final String ID_SCALAR_NAME = "scalarName";
    private static final String ID_SCALAR_VALUE = "scalarValue";
    private static final String ID_FEEDBACK = "feedback";
    private static final String ID_SCALAR_IF_COMPACT = "scalarIfCompact";
    private static final String ID_SCALAR_IF_COMPACT_DOWNLOAD = "scalarIfCompactDownload";

    protected enum InputFieldVisibility {
            VISIBLE, NOT_VISIBLE;
        }

    @Override
    protected FormComponentLabel addComponentForRegular() {
        final FileUploadField fileUploadField = createFileUploadField(ID_SCALAR_VALUE);
        fileUploadField.setLabel(Model.of(getModel().getName()));
        
        final FormComponentLabel scalarIfRegular = new FormComponentLabel(ID_SCALAR_IF_REGULAR, fileUploadField);
        scalarIfRegular.add(fileUploadField);
    
        final Label scalarUploadLabel = new Label(ID_SCALAR_IF_REGULAR_UPLOAD, "Upload");
        scalarIfRegular.add(scalarUploadLabel);
    
        final Label scalarName = new Label(ID_SCALAR_NAME, getModel().getName());
        scalarIfRegular.add(scalarName);
        
        updateDownloadLink(ID_SCALAR_IF_REGULAR_DOWNLOAD, scalarIfRegular);
        scalarIfRegular.addOrReplace(new ComponentFeedbackPanel(ID_FEEDBACK, fileUploadField));
        
        addOrReplace(scalarIfRegular);
        
        return scalarIfRegular;
    }

    @Override
    protected void renderHead(IHeaderResponse response, Class<?> cls) {
        super.renderHead(response, IsisBlobOrClobPanelAbstract.class); // don't use the subclass
        // also include JQuery
        response.render(JavaScriptHeaderItem.forReference(Application.get().getJavaScriptLibrarySettings()
                .getJQueryReference()));
    }

    @Override
    protected Component addComponentForCompact() {
        final MarkupContainer scalarIfCompact = new WebMarkupContainer(ID_SCALAR_IF_COMPACT);
        updateDownloadLink(ID_SCALAR_IF_COMPACT_DOWNLOAD, scalarIfCompact);
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
        final FileUploadField fileUploadField = new FileUploadField(componentId, new IModel<List<FileUpload>>() {
    
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
        return fileUploadField;
    }

    protected abstract T getBlobOrClobFrom(final List<FileUpload> fileUploads);

    @SuppressWarnings("unchecked")
    private T getBlob(final ScalarModel model) {
        ObjectAdapter adapter = model.getObject();
        return adapter != null? (T) adapter.getObject(): null;
    }

    public IsisBlobOrClobPanelAbstract(String id, ScalarModel scalarModel) {
        super(id, scalarModel);
    }

    private void updateRegularFormComponents(InputFieldVisibility visibility) {
        MarkupContainer formComponent = (MarkupContainer) getComponentForRegular();
        formComponent.get(ID_SCALAR_VALUE).setVisible(visibility == InputFieldVisibility.VISIBLE);
        formComponent.get(ID_SCALAR_IF_REGULAR_UPLOAD).setVisible(visibility == InputFieldVisibility.VISIBLE);
        
        updateClearLink(visibility);
    
        final MarkupContainer downloadLink = updateDownloadLink(ID_SCALAR_IF_REGULAR_DOWNLOAD, formComponent);
        if (downloadLink != null) {
            // the visibility of download link is intentionally 'backwards';
            // if in edit mode then do NOT show
            downloadLink.setVisible(visibility == InputFieldVisibility.NOT_VISIBLE);
        }
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
            }
        };
        ajaxLink.setOutputMarkupId(true);
        formComponent.addOrReplace(ajaxLink);
    
        final T blob = getBlob(getModel());
        formComponent.get(ID_SCALAR_IF_REGULAR_CLEAR).setVisible(blob != null && visibility == InputFieldVisibility.VISIBLE);
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
        final T blob = getBlob(getModel());
        if(blob == null) {
            return null;
        }
        final IResource bar = newResource(blob);
        return new ResourceLink<Object>(id, bar);
    }

    
    /**
     * Mandatory hook method.
     */
    protected abstract IResource newResource(final T namedWithMimeType);

}