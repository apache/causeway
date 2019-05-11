package org.apache.isis.core.metamodel;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.title.TitleService;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.applib.services.xactn.TransactionState;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.services.events.MetamodelEventService;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.security.authentication.AuthenticationSession;
import org.apache.isis.core.security.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.security.authentication.manager.AuthenticationManager;
import org.apache.isis.core.security.authorization.manager.AuthorizationManager;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.val;

@Builder @Getter
final class MetaModelContext_forTesting implements MetaModelContext {
	
	private IsisConfiguration configuration;

	private ObjectAdapterProvider objectAdapterProvider;

	@Builder.Default
	private ServiceInjector serviceInjector = new ServiceInjector_forTesting();

	@Builder.Default
	private ServiceRegistry serviceRegistry = new ServiceRegistry_forTesting();

	@Builder.Default
    private MetamodelEventService metamodelEventService = 
        MetamodelEventService.builder()
        .build();
	
	private SpecificationLoader specificationLoader;

	private AuthenticationSessionProvider authenticationSessionProvider;

	private TranslationService translationService;

	private AuthenticationSession authenticationSession;

	private AuthorizationManager authorizationManager;

	private AuthenticationManager authenticationManager;

	private TitleService titleService;

	private RepositoryService repositoryService;

	private TransactionService transactionService;

	private TransactionState transactionState;

	private Map<String, ObjectAdapter> serviceAdaptersById;
	
	@Singular
	private List<Object> singletons;

	@Override
	public ObjectSpecification getSpecification(Class<?> type) {
		return specificationLoader.loadSpecification(type);
	}

	@Override
	public Stream<ObjectAdapter> streamServiceAdapters() {

	    if(serviceAdaptersById==null) {
			return Stream.empty();
		}
		return serviceAdaptersById.values().stream();
	}

	@Override
	public ObjectAdapter lookupServiceAdapterById(String serviceId) {
		if(serviceAdaptersById==null) {
			return null;
		}
		return serviceAdaptersById.get(serviceId);
	}
	
    public Stream<Object> streamSingletons() {
        
        val fields = _Lists.of(
                configuration,
                objectAdapterProvider,
                serviceInjector,
                serviceRegistry,
                metamodelEventService,
                specificationLoader,
                authenticationSessionProvider,
                translationService,
                authenticationSession,
                authorizationManager,
                authenticationManager,
                titleService,
                repositoryService,
                transactionService,
                transactionState);
        
        return Stream.concat(fields.stream(), getSingletons().stream())
                .filter(_NullSafe::isPresent);
    }
	
}