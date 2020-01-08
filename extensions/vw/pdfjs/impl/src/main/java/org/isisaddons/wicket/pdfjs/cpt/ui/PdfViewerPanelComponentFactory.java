package org.isisaddons.wicket.pdfjs.cpt.ui;

import java.util.Objects;

import javax.activation.MimeType;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import org.apache.isis.applib.value.Blob;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.ComponentFactoryAbstract;
import org.apache.isis.viewer.wicket.ui.ComponentType;

import org.isisaddons.wicket.pdfjs.cpt.applib.PdfJsViewerFacet;

import lombok.val;

@org.springframework.stereotype.Component
public class PdfViewerPanelComponentFactory extends ComponentFactoryAbstract {

    private static final long serialVersionUID = 1L;

    public PdfViewerPanelComponentFactory() {
        super(ComponentType.SCALAR_NAME_AND_VALUE, PdfJsViewerPanel.class);
    }

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

    public Component createComponent(String id, IModel<?> model) {
        ScalarModel scalarModel = (ScalarModel) model;
        return new PdfJsViewerPanel(id, scalarModel);
    }
}

