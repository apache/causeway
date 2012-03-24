package org.apache.isis.runtimes.dflt.testsupport;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.commons.resource.ResourceStreamSourceContextLoaderClassPath;
import org.apache.isis.core.metamodel.facetdecorator.FacetDecorator;
import org.apache.isis.core.metamodel.layout.MemberLayoutArranger;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.specloader.ObjectReflector;
import org.apache.isis.core.metamodel.specloader.ObjectReflectorDefault;
import org.apache.isis.core.metamodel.specloader.classsubstitutor.ClassSubstitutor;
import org.apache.isis.core.metamodel.specloader.collectiontyperegistry.CollectionTypeRegistry;
import org.apache.isis.core.metamodel.specloader.collectiontyperegistry.CollectionTypeRegistryDefault;
import org.apache.isis.core.metamodel.specloader.traverser.SpecificationTraverser;
import org.apache.isis.core.metamodel.specloader.traverser.SpecificationTraverserDefault;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.core.progmodel.layout.dflt.MemberLayoutArrangerDefault;
import org.apache.isis.core.progmodel.metamodelvalidator.dflt.MetaModelValidatorDefault;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authentication.standard.AuthenticationManagerStandard;
import org.apache.isis.core.runtime.authentication.standard.Authenticator;
import org.apache.isis.core.runtime.authorization.AuthorizationManager;
import org.apache.isis.core.runtime.authorization.standard.AuthorizationManagerStandard;
import org.apache.isis.core.runtime.userprofile.UserProfileStore;
import org.apache.isis.progmodels.dflt.ProgrammingModelFacetsJava5;
import org.apache.isis.runtimes.dflt.bytecode.dflt.classsubstitutor.CglibClassSubstitutor;
import org.apache.isis.runtimes.dflt.objectstores.dflt.InMemoryPersistenceMechanismInstaller;
import org.apache.isis.runtimes.dflt.profilestores.dflt.InMemoryUserProfileStore;
import org.apache.isis.runtimes.dflt.runtime.fixtures.FixturesInstaller;
import org.apache.isis.runtimes.dflt.runtime.fixtures.FixturesInstallerFromConfiguration;
import org.apache.isis.runtimes.dflt.runtime.services.ServicesInstallerFromConfiguration;
import org.apache.isis.runtimes.dflt.runtime.system.DeploymentType;
import org.apache.isis.runtimes.dflt.runtime.system.IsisSystemException;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.runtimes.dflt.runtime.systemusinginstallers.IsisSystemAbstract;
import org.apache.isis.runtimes.dflt.runtime.transaction.facetdecorator.standard.StandardTransactionFacetDecorator;
import org.apache.isis.security.dflt.authentication.AuthenticatorDefault;

public class IsisSystemDefault extends IsisSystemAbstract {

    private final IsisConfigurationDefault configuration;
    private final List<Object> servicesIfAny;

    public IsisSystemDefault(Object... servicesIfAny) {
        this(DeploymentType.SERVER, servicesIfAny);
    }

    public IsisSystemDefault(DeploymentType deploymentType, Object... servicesIfAny) {
        super(deploymentType);
        this.configuration = new IsisConfigurationDefault(ResourceStreamSourceContextLoaderClassPath.create("config"));
        this.servicesIfAny = copyOfServicesIfProvided(servicesIfAny);
    }

    protected List<Object> copyOfServicesIfProvided(Object... servicesIfAny) {
        return servicesIfAny != null? Collections.unmodifiableList(Lists.newArrayList(servicesIfAny)): null;
    }

    /**
     * Reads <tt>isis.properties</tt> (and other optional property files) from the &quot;config&quot; package on the current classpath.
     */
    @Override
    public IsisConfiguration getConfiguration() {
        return configuration;
    }


    /**
     * Either the services explicitly provided by a constructor, otherwise reads from the configuration.
     */
    @Override
    protected List<Object> obtainServices() {
        if(servicesIfAny != null) {
            return servicesIfAny;
        }
        // else
        final ServicesInstallerFromConfiguration servicesInstaller = new ServicesInstallerFromConfiguration();
        return servicesInstaller.getServices(getDeploymentType());
    }

    /**
     * Install fixtures from configuration.
     */
    @Override
    protected FixturesInstaller obtainFixturesInstaller() throws IsisSystemException {
        final FixturesInstallerFromConfiguration fixturesInstallerFromConfiguration = new FixturesInstallerFromConfiguration();
        fixturesInstallerFromConfiguration.setConfiguration(getConfiguration());
        return fixturesInstallerFromConfiguration;
    }


    /**
     * Java5, with cglib, and only the transaction facet decorators.
     */
    @Override
    protected ObjectReflector obtainReflector(DeploymentType deploymentType) throws IsisSystemException {
        ClassSubstitutor classSubstitutor = new CglibClassSubstitutor();
        CollectionTypeRegistry collectionTypeRegistry = new CollectionTypeRegistryDefault();
        SpecificationTraverser specificationTraverser = new SpecificationTraverserDefault();
        MemberLayoutArranger memberLayoutArranger = new MemberLayoutArrangerDefault();
        ProgrammingModel programmingModel = new ProgrammingModelFacetsJava5();
        Set<FacetDecorator> facetDecorators = Sets.newHashSet((FacetDecorator)new StandardTransactionFacetDecorator(configuration));
        MetaModelValidator metaModelValidator = new MetaModelValidatorDefault();
        return new ObjectReflectorDefault(configuration, classSubstitutor, collectionTypeRegistry, specificationTraverser, memberLayoutArranger, programmingModel, facetDecorators, metaModelValidator);
    }

    /**
     * The standard authentication manager, configured with the default authenticator (allows all requests through).
     */
    @Override
    protected AuthenticationManager obtainAuthenticationManager(DeploymentType deploymentType) throws IsisSystemException {
        final AuthenticationManagerStandard authenticationManager = new AuthenticationManagerStandard(configuration);
        Authenticator authenticator = new AuthenticatorDefault(configuration);
        authenticationManager.addAuthenticator(authenticator);
        return authenticationManager;
    }

    /**
     * The standard authorization manager, allowing all access.
     */
    @Override
    protected AuthorizationManager obtainAuthorizationManager(DeploymentType deploymentType) {
        return new AuthorizationManagerStandard(configuration);
    }

    /**
     * The in-memory user profile store.
     */
    @Override
    protected UserProfileStore obtainUserProfileStore() {
        return new InMemoryUserProfileStore();
    }

    /**
     * The in-memory object store.
     */
    @Override
    protected PersistenceSessionFactory obtainPersistenceSessionFactory(DeploymentType deploymentType) throws IsisSystemException {
        final InMemoryPersistenceMechanismInstaller inMemoryPersistenceMechanismInstaller = new InMemoryPersistenceMechanismInstaller();
        inMemoryPersistenceMechanismInstaller.setConfiguration(getConfiguration());
        return inMemoryPersistenceMechanismInstaller.createPersistenceSessionFactory(deploymentType);
    }
    
}
