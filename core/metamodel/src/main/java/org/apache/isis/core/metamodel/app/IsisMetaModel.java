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

package org.apache.isis.core.metamodel.app;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.Lists;

import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.facetdecorator.FacetDecorator;
import org.apache.isis.core.metamodel.layoutmetadata.LayoutMetadataReader;
import org.apache.isis.core.metamodel.layoutmetadata.json.LayoutMetadataReaderFromJson;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.runtimecontext.noruntime.RuntimeContextNoRuntime;
import org.apache.isis.core.metamodel.services.ServicesInjectorDefault;
import org.apache.isis.core.metamodel.services.ServicesInjectorSpi;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.specloader.ObjectReflectorDefault;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailures;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatState;
import static org.hamcrest.CoreMatchers.is;

/**
 * Facade for the entire Isis metamodel and supporting components.
 * 
 * <p>
 * Currently this is <i>not</i> used by Isis proper, but is available for use by integration tests.
 * The intention is to factor it into <tt>IsisSystem</tt>.
 */
public class IsisMetaModel implements ApplicationScopedComponent {


    private static enum State {
        NOT_INITIALIZED, INITIALIZED, SHUTDOWN;
    }

    private State state = State.NOT_INITIALIZED;

    private final ServicesInjectorSpi servicesInjector;

    private ObjectReflectorDefault specificationLoader;
    private RuntimeContext runtimeContext;

    private IsisConfigurationDefault configuration;
    private ProgrammingModel programmingModel;
    private Set<FacetDecorator> facetDecorators;
    private MetaModelValidatorComposite metaModelValidator;

    private ValidationFailures validationFailures;

    public IsisMetaModel(
            final ProgrammingModel programmingModel,
            final List<Object> services) {
        this(programmingModel, services.toArray());
    }
    
    public IsisMetaModel(
            final ProgrammingModel programmingModel,
            final Object... services) {

        this.programmingModel = programmingModel;

        final List<Object> serviceList = Lists.newArrayList();
        serviceList.addAll(Arrays.asList(services));
        this.servicesInjector = new ServicesInjectorDefault(serviceList);

        this.configuration = new IsisConfigurationDefault();

        this.facetDecorators = new TreeSet<>();

        this.metaModelValidator = new MetaModelValidatorComposite();
        this.programmingModel.refineMetaModelValidator(metaModelValidator, configuration);

        final DeploymentCategory deploymentCategory = DeploymentCategory.PRODUCTION;
        final List<LayoutMetadataReader> layoutMetadataReaders =
                Lists.<LayoutMetadataReader>newArrayList(new LayoutMetadataReaderFromJson());

        this.specificationLoader = new ObjectReflectorDefault(
                deploymentCategory, configuration,
                this.programmingModel, facetDecorators,
                metaModelValidator, layoutMetadataReaders, servicesInjector);

        this.runtimeContext = new RuntimeContextNoRuntime(
                deploymentCategory, configuration, servicesInjector, specificationLoader);

    }

    public ServicesInjectorSpi getServicesInjector() {
        return servicesInjector;
    }

    // ///////////////////////////////////////////////////////
    // init, shutdown
    // ///////////////////////////////////////////////////////

    public void init() {
        ensureNotInitialized();

        specificationLoader.initialize(runtimeContext);

        for (final Object service : servicesInjector.getRegisteredServices()) {
            final ObjectSpecification serviceSpec = specificationLoader.loadSpecification(service.getClass());
            serviceSpec.markAsService();
        }

        validationFailures = specificationLoader.validate();

        state = State.INITIALIZED;
    }
    
    public ValidationFailures getValidationFailures() {
        return validationFailures;
    }

    public void shutdown() {
        ensureInitialized();
        state = State.SHUTDOWN;
    }


    /**
     * Available once {@link #init() initialized}.
     */
    public SpecificationLoaderSpi getSpecificationLoader() {
        return specificationLoader;
    }

    //region > State management

    private State ensureNotInitialized() {
        return ensureThatState(state, is(State.NOT_INITIALIZED));
    }

    private State ensureInitialized() {
        return ensureThatState(state, is(State.INITIALIZED));
    }

    //endregion

}
