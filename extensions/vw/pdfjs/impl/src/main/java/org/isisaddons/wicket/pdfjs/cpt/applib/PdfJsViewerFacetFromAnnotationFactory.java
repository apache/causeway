package org.isisaddons.wicket.pdfjs.cpt.applib;

import java.lang.reflect.Method;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facetapi.FacetUtil;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facets.Annotations;
import org.apache.isis.metamodel.facets.FacetFactoryAbstract;

import lombok.val;


public class PdfJsViewerFacetFromAnnotationFactory extends FacetFactoryAbstract {

    @Inject ServiceInjector serviceInjector;

    public PdfJsViewerFacetFromAnnotationFactory() {
        super(FeatureType.PROPERTIES_AND_ACTIONS);
    }


    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        final FacetHolder holder = processMethodContext.getFacetHolder();
        final Method method = processMethodContext.getMethod();

        final Optional<PdfJsViewer> pdfjsViewerOpt = processMethodContext.synthesizeOnMethod(PdfJsViewer.class);

        pdfjsViewerOpt.ifPresent(
            pdfjsViewer -> {
                val pdfJsViewerFacet = PdfJsViewerFacetFromAnnotation.create(pdfjsViewer, holder);
                serviceInjector.injectServicesInto(pdfJsViewerFacet);
                FacetUtil.addFacet(pdfJsViewerFacet);
            }
        );
    }

    @Override
    public void processParams(ProcessParameterContext processParameterContext) {
        super.processParams(processParameterContext);
    }
}
