package org.apache.isis.persistence.jpa.metamodel;

import java.lang.reflect.Method;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceUnitUtil;

import org.springframework.data.jpa.repository.JpaContext;

import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.query.QueryFindAllInstances;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.repository.EntityState;
import org.apache.isis.applib.services.urlencoding.UrlEncodingService;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.memento._Mementos;
import org.apache.isis.commons.internal.memento._Mementos.SerializingAdapter;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

public class JpaEntityFacetFactory extends FacetFactoryAbstract {

    public JpaEntityFacetFactory() {
        super(ImmutableEnumSet.of(FeatureType.OBJECT));
    }

    @Override
    public void process(ProcessClassContext processClassContext) {
        val cls = processClassContext.getCls();

        val entityAnnotation = Annotations.getAnnotation(cls, Entity.class);
        if (entityAnnotation == null) {
            return;
        }
        
        val facetHolder = processClassContext.getFacetHolder();
        val serviceRegistry = super.getMetaModelContext().getServiceRegistry();
        val jpaEntityFacet = new JpaEntityFacet(facetHolder, cls, serviceRegistry);
            
        addFacet(jpaEntityFacet);
    }
    
    // -- 
    
    public static class JpaEntityFacet
    extends FacetAbstract
    implements EntityFacet {

        private final Class<?> entityClass;
        private final ServiceRegistry serviceRegistry;
        
        protected JpaEntityFacet(
                final FacetHolder holder,
                final Class<?> entityClass, 
                final @NonNull ServiceRegistry serviceRegistry) {
            
            super(EntityFacet.class, holder);
            this.entityClass = entityClass;
            this.serviceRegistry = serviceRegistry;
        }
        
        @Override public boolean isDerived() { return false;}
        @Override public boolean isFallback() { return false;}
        @Override public boolean alwaysReplace() { return true;}
        
        // -- ENTITY FACET 

        @Override
        public String identifierFor(ObjectSpecification spec, Object pojo) {

            if(pojo==null) {
                throw _Exceptions.illegalArgument(
                        "The persistence layer cannot identify a pojo that is null (given type %s)",
                        spec.getCorrespondingClass().getName());
            }
            
            if(!spec.isEntity()) {
                throw _Exceptions.illegalArgument(
                        "The persistence layer does not recognize given type %s",
                        pojo.getClass().getName());
            }

            val entityManager = getEntityManager();
            val persistenceUnitUtil = getPersistenceUnitUtil(entityManager);
            val primaryKey = persistenceUnitUtil.getIdentifier(pojo);
            
            if(primaryKey==null) {
                throw _Exceptions.illegalArgument(
                        "The persistence layer does not recognize given object of type %s, "
                        + "meaning the object has no identifier that associates it with the persistence layer. "
                        + "(most likely, because the object is detached, eg. was not persisted after being new-ed up)", 
                        pojo.getClass().getName());
            }
            
            return getObjectIdSerializer().stringify(primaryKey);

        }

        @Override
        public ManagedObject fetchByIdentifier(ObjectSpecification spec, String identifier) {
            
            val primaryKey = getObjectIdSerializer().parse(identifier);
            val entityManager = getEntityManager();
            val entity = entityManager.find(entityClass, primaryKey);
            
            return ManagedObject.of(spec, entity);
        }

        @Override
        public Can<ManagedObject> fetchByQuery(ObjectSpecification spec, Query<?> query) {
            
            if(!(query instanceof QueryFindAllInstances)) {
                throw _Exceptions.notImplemented();
            }
            
            val queryFindAllInstances = (QueryFindAllInstances<?>) query;
            val queryEntityType = queryFindAllInstances.getResultType();
            
            // guard against misuse
            if(!entityClass.isAssignableFrom(queryEntityType)) {
                throw _Exceptions.unexpectedCodeReach();
            }
            
            val entityManager = getEntityManager();
            
            val typedQuery = entityManager
                    .createQuery("SELECT t FROM " + entityClass.getSimpleName() + " t", entityClass);
            
            final int startPosition = Math.toIntExact(queryFindAllInstances.getStart());
            final int maxResult = Math.toIntExact(queryFindAllInstances.getCount());
            typedQuery.setFirstResult(startPosition);
            typedQuery.setMaxResults(maxResult);
            
            return Can.ofStream(
                typedQuery.getResultStream()
                .map(entity->ManagedObject.of(spec, entity)));
        }

        @Override
        public void persist(ObjectSpecification spec, Object pojo) {
            if(pojo==null) {
                return; // nothing to do
            }
            
            // guard against misuse
            if(!entityClass.isAssignableFrom(pojo.getClass())) {
                throw _Exceptions.unexpectedCodeReach();
            }
            
            val entityManager = getEntityManager();
            entityManager.persist(pojo);
        }

        @Override
        public void refresh(Object pojo) {
            if(pojo==null) {
                return; // nothing to do
            }
            
            // guard against misuse
            if(!entityClass.isAssignableFrom(pojo.getClass())) {
                throw _Exceptions.unexpectedCodeReach();
            }
            
            val entityManager = getEntityManager();
            entityManager.refresh(pojo);
        }

        @Override
        public void delete(ObjectSpecification spec, Object pojo) {
            
            if(pojo==null) {
                return; // nothing to do
            }
            
            // guard against misuse
            if(!entityClass.isAssignableFrom(pojo.getClass())) {
                throw _Exceptions.unexpectedCodeReach();
            }
            
            val entityManager = getEntityManager();
            entityManager.remove(pojo);
        }

        @Override
        public EntityState getEntityState(Object pojo) {
            
            if(pojo==null) {
                return EntityState.NOT_PERSISTABLE;
            }
            
            // guard against misuse
            if(!entityClass.isAssignableFrom(pojo.getClass())) {
                //throw _Exceptions.unexpectedCodeReach();
                return EntityState.NOT_PERSISTABLE;
            }
            
            val entityManager = getEntityManager();
            val persistenceUnitUtil = getPersistenceUnitUtil(entityManager);
            
            if(persistenceUnitUtil.isLoaded(pojo)) {
                return EntityState.PERSISTABLE_ATTACHED;
            }
            //TODO how to determine whether deleted? (even relevant?)
//            if(isDeleted) {
//                return EntityState.PERSISTABLE_DESTROYED;
//            }
            return EntityState.PERSISTABLE_DETACHED;
        }

        @Override
        public boolean isProxyEnhancement(Method method) {
            return false;
        }

        @Override
        public <T> T detach(T pojo) {
            getEntityManager().detach(pojo);
            return pojo;
        }
        
        // -- DEPENDENCIES
        
        protected JpaContext getJpaContext() {
            return serviceRegistry.lookupServiceElseFail(JpaContext.class);
        }
        
        protected EntityManager getEntityManager() {
            return getJpaContext().getEntityManagerByManagedType(entityClass);
        }
        
        protected PersistenceUnitUtil getPersistenceUnitUtil(EntityManager entityManager) {
            return entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
        }
        
        protected JpaObjectIdSerializer getObjectIdSerializer() {
            val codec = serviceRegistry.lookupServiceElseFail(UrlEncodingService.class);
            val serializer = serviceRegistry.lookupServiceElseFail(SerializingAdapter.class);
            return new JpaObjectIdSerializer(codec, serializer);
        }
        
    }

    @RequiredArgsConstructor
    private static class JpaObjectIdSerializer {
        
        private final UrlEncodingService codec;
        private final SerializingAdapter serializer;
        
        public String stringify(Object id) {
            return newMemento().put("id", id).asString();
        }
        
        public Object parse(String input) {
            if(_Strings.isEmpty(input)) {
                return null;
            }
            return parseMemento(input).get("id", Object.class);
        }
       
        // -- HELPER

        private _Mementos.Memento newMemento(){
            return _Mementos.create(codec, serializer);
        }

        private _Mementos.Memento parseMemento(String input){
            return _Mementos.parse(codec, serializer, input);
        }
        
    }
    


}
