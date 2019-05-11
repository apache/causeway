package org.apache.isis.core.metamodel;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.title.TitleService;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.applib.services.xactn.TransactionState;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.cdi._CDI;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.config.internal._Config;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.services.ServiceUtil;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.security.authentication.AuthenticationSession;
import org.apache.isis.core.security.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.security.authentication.manager.AuthenticationManager;
import org.apache.isis.core.security.authorization.manager.AuthorizationManager;

import lombok.Getter;
import lombok.val;

class MetaModelContext_usingCDI implements MetaModelContext {

        @Getter(lazy=true) 
        private final IsisConfiguration configuration = 
                _Config.getConfiguration();

        @Getter(lazy=true) 
        private final ObjectAdapterProvider objectAdapterProvider =
                _CDI.getSingletonElseFail(ObjectAdapterProvider.class);

        @Getter(lazy=true) 
        private final ServiceInjector serviceInjector =
                _CDI.getSingletonElseFail(ServiceInjector.class);

        @Getter(lazy=true) 
        private final ServiceRegistry serviceRegistry =
                _CDI.getSingletonElseFail(ServiceRegistry.class);

        @Getter(lazy=true) 
        private final SpecificationLoader specificationLoader = 
                _CDI.getSingletonElseFail(SpecificationLoader.class);

        @Getter(lazy=true) 
        private final AuthenticationSessionProvider authenticationSessionProvider =
                _CDI.getSingletonElseFail(AuthenticationSessionProvider.class);

        @Getter(lazy=true) 
        private final TranslationService translationService =
                _CDI.getSingletonElseFail(TranslationService.class);

        @Getter(lazy=true) 
        private final AuthorizationManager authorizationManager =
                _CDI.getSingletonElseFail(AuthorizationManager.class); 

        @Getter(lazy=true) 
        private final AuthenticationManager authenticationManager =
                _CDI.getSingletonElseFail(AuthenticationManager.class);

        @Getter(lazy=true) 
        private final TitleService titleService =
                _CDI.getSingletonElseFail(TitleService.class);

//        @Getter(lazy=true) 
//        private final ObjectAdapterService objectAdapterService =
//        _CDI.getSingletonElseFail(ObjectAdapterService.class);

        @Getter(lazy=true) 
        private final RepositoryService repositoryService =
                _CDI.getSingletonElseFail(RepositoryService.class);
        
        @Getter(lazy=true) 
        private final TransactionService transactionService =
                _CDI.getSingletonElseFail(TransactionService.class);
        

        @Override
        public final AuthenticationSession getAuthenticationSession() {
            return getAuthenticationSessionProvider().getAuthenticationSession();
        }

        @Override
        public final ObjectSpecification getSpecification(final Class<?> type) {
            return type != null ? getSpecificationLoader().loadSpecification(type) : null;
        }

        @Override
        public final TransactionState getTransactionState() {
            return getTransactionService().getTransactionState();
        }
        
        // -- SERVICE SUPPORT
        
        @Override
        public Stream<ObjectAdapter> streamServiceAdapters() {
            return serviceAdapters.get().values().stream();
        }
        
        @Override
        public ObjectAdapter lookupServiceAdapterById(final String serviceId) {
            return serviceAdapters.get().get(serviceId);
        }
        
        
        // -- HELPER
        
        private final _Lazy<Map<String, ObjectAdapter>> serviceAdapters = _Lazy.of(this::initServiceAdapters);
        
        private Map<String, ObjectAdapter> initServiceAdapters() {
        	
        	val objectAdapterProvider = getObjectAdapterProvider();
        	
            return getServiceRegistry().streamServices()
            .map(objectAdapterProvider::adapterFor) 
            .peek(serviceAdapter->{
                val oid = serviceAdapter.getOid();
                if(oid.isTransient()) {
                    val msg = "ObjectAdapter for 'Bean' is expected not to be 'transient' " + oid;
                    throw _Exceptions.unrecoverable(msg);
                }
            })
            .collect(Collectors.toMap(ServiceUtil::idOfAdapter, v->v, (o,n)->n, LinkedHashMap::new));
        }

        // -------------------------------------------------------------------------------


    }