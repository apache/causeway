package org.apache.isis.extensions.viewer.wicket.pdfjs.impl.metamodel.facet;

import java.lang.reflect.Method;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facetapi.FacetUtil;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.metamodel.progmodel.ProgrammingModel;

import org.apache.isis.extensions.viewer.wicket.pdfjs.impl.applib.annotations.PdfJsViewer;

import lombok.val;


public class PdfJsViewerFacetFromAnnotationFactory extends FacetFactoryAbstract {

    @Component
    public static class Register implements MetaModelRefiner {

        @Override
        public void refineProgrammingModel(ProgrammingModel programmingModel) {
            programmingModel.addFactory(
                    ProgrammingModel.FacetProcessingOrder.Z2_AFTER_FINALLY,
                    PdfJsViewerFacetFromAnnotationFactory.class);
        }
    }
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
