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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import com.google.common.collect.Lists;
import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.metamodel.facetdecorator.FacetDecorator;
import org.apache.isis.core.metamodel.layoutmetadata.LayoutMetadataReader;
import org.apache.isis.core.metamodel.layoutmetadata.json.LayoutMetadataReaderFromJson;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.services.ServicesInjectorDefault;
import org.apache.isis.core.metamodel.services.container.DomainObjectContainerDefault;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.specloader.ObjectReflectorDefault;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailures;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatArg;
import static org.apache.isis.core.commons.ensure.Ensure.ensureThatState;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

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

    private final List<Object> services = Lists.newArrayList();

    private State state = State.NOT_INITIALIZED;

    private ObjectReflectorDefault reflector;
    private RuntimeContext runtimeContext;

    private IsisConfiguration configuration;
    private ProgrammingModel programmingModel;
    private Set<FacetDecorator> facetDecorators;
    private MetaModelValidatorComposite metaModelValidator;

    private ValidationFailures validationFailures;

    public IsisMetaModel(final RuntimeContext runtimeContext, ProgrammingModel programmingModel, final List<Object> services) {
        this(runtimeContext, programmingModel, services.toArray());
    }
    
    public IsisMetaModel(
            final RuntimeContext runtimeContext,
            final ProgrammingModel programmingModel,
            final Object... services) {
        this.runtimeContext = runtimeContext;

        this.services.add(new DomainObjectContainerDefault());
        this.services.addAll(Arrays.asList(services));

        this.configuration = new IsisConfigurationDefault();

        this.facetDecorators = new TreeSet<>();
        setProgrammingModelFacets(programmingModel);

        this.metaModelValidator = new MetaModelValidatorComposite();
        programmingModel.refineMetaModelValidator(metaModelValidator, configuration);
    }

    /**
     * The list of classes representing services.
     */
    public List<Object> getServices() {
        return Collections.unmodifiableList(services);
    }

    // ///////////////////////////////////////////////////////
    // init, shutdown
    // ///////////////////////////////////////////////////////

    @Override
    public void init() {
        ensureNotInitialized();

        final List<LayoutMetadataReader> layoutMetadataReaders = Lists.<LayoutMetadataReader>newArrayList(
                new LayoutMetadataReaderFromJson());

        reflector = new ObjectReflectorDefault(configuration, programmingModel, facetDecorators, metaModelValidator, layoutMetadataReaders);

        final ServicesInjectorDefault servicesInjector = new ServicesInjectorDefault();
        servicesInjector.setServices(services);
        reflector.setServiceInjector(servicesInjector);
        
        runtimeContext.injectInto(reflector);
        reflector.injectInto(runtimeContext);

        validationFailures = reflector.initAndValidate();
        runtimeContext.init();

        for (final Object service : services) {
            final ObjectSpecification serviceSpec = reflector.loadSpecification(service.getClass());
            serviceSpec.markAsService();
        }

        state = State.INITIALIZED;
    }
    
    public ValidationFailures getValidationFailures() {
        return validationFailures;
    }

    @Override
    public void shutdown() {
        ensureInitialized();
        state = State.SHUTDOWN;
    }

    // ///////////////////////////////////////////////////////
    // SpecificationLoader
    // ///////////////////////////////////////////////////////

    /**
     * Available once {@link #init() initialized}.
     */
    public SpecificationLoaderSpi getSpecificationLoader() {
        return reflector;
    }


    // ///////////////////////////////////////////////////////
    // DependencyInjector
    // ///////////////////////////////////////////////////////

    /**
     * Available once {@link #init() initialized}.
     */
    public ServicesInjector getDependencyInjector() {
        ensureInitialized();
        return runtimeContext.getServicesInjector();
    }

    // ///////////////////////////////////////////////////////
    // Override defaults
    // ///////////////////////////////////////////////////////

    /**
     * The {@link IsisConfiguration} in force, either defaulted or specified
     * {@link #setConfiguration(IsisConfiguration) explicitly.}
     */
    public IsisConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Optionally specify the {@link IsisConfiguration}.
     * 
     * <p>
     * Call prior to {@link #init()}.
     */
    public void setConfiguration(final IsisConfiguration configuration) {
        ensureNotInitialized();
        ensureThatArg(configuration, is(notNullValue()));
        this.configuration = configuration;
    }

    /**
     * The {@link ProgrammingModel} in force, either defaulted or specified
     * {@link #setProgrammingModelFacets(ProgrammingModel) explicitly}.
     */
    public ProgrammingModel getProgrammingModelFacets() {
        return programmingModel;
    }

    /**
     * Optionally specify the {@link ProgrammingModel}.
     * 
     * <p>
     * Call prior to {@link #init()}.
     */
    public void setProgrammingModelFacets(final ProgrammingModel programmingModel) {
        ensureNotInitialized();
        ensureThatArg(programmingModel, is(notNullValue()));
        this.programmingModel = programmingModel;
    }

    /**
     * The {@link FacetDecorator}s in force, either defaulted or specified
     * {@link #setFacetDecorators(Set) explicitly}.
     */
    public Set<FacetDecorator> getFacetDecorators() {
        return Collections.unmodifiableSet(facetDecorators);
    }

    /**
     * Optionally specify the {@link FacetDecorator}s.
     * 
     * <p>
     * Call prior to {@link #init()}.
     */
    public void setFacetDecorators(final Set<FacetDecorator> facetDecorators) {
        ensureNotInitialized();
        ensureThatArg(facetDecorators, is(notNullValue()));
        this.facetDecorators = facetDecorators;
    }

    /**
     * The {@link MetaModelValidator} in force, either defaulted or specified
     * {@link #setMetaModelValidator(org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite) explicitly}.
     */
    public MetaModelValidatorComposite getMetaModelValidator() {
        return metaModelValidator;
    }

    /**
     * Optionally specify the {@link MetaModelValidator}.
     * @param metaModelValidator
     */
    public void setMetaModelValidator(final MetaModelValidatorComposite metaModelValidator) {
        this.metaModelValidator = metaModelValidator;
    }

    // ///////////////////////////////////////////////////////
    // State management
    // ///////////////////////////////////////////////////////

    private State ensureNotInitialized() {
        return ensureThatState(state, is(State.NOT_INITIALIZED));
    }

    private State ensureInitialized() {
        return ensureThatState(state, is(State.INITIALIZED));
    }

}
