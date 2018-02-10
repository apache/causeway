package org.apache.isis.core.metamodel.specloader.postprocessor;

import java.util.List;

import org.apache.isis.core.metamodel.progmodel.ObjectSpecificationPostProcessor;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.services.ServicesInjectorAware;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public class PostProcessor {

    private final ProgrammingModel programmingModel;
    private final ServicesInjector servicesInjector;
    // populated at #init
    List<ObjectSpecificationPostProcessor> postProcessors;

    public PostProcessor(final ProgrammingModel programmingModel, final ServicesInjector servicesInjector) {
        this.programmingModel = programmingModel;
        this.servicesInjector = servicesInjector;
    }

    public void init() {
        postProcessors = programmingModel.getPostProcessors();
        for (final ObjectSpecificationPostProcessor postProcessor : postProcessors) {
            if(postProcessor instanceof ServicesInjectorAware) {
                final ServicesInjectorAware servicesInjectorAware = (ServicesInjectorAware) postProcessor;
                servicesInjectorAware.setServicesInjector(servicesInjector);
            }
        }
    }
    public void postProcess(final ObjectSpecification objectSpecification) {

        for (final ObjectSpecificationPostProcessor postProcessor : postProcessors) {
            postProcessor.postProcess(objectSpecification);
        }


    }

}
