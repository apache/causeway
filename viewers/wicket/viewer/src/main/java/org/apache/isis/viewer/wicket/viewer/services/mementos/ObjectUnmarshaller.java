package org.apache.isis.viewer.wicket.viewer.services.mementos;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.internal.assertions._Assert;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.ParentedOid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.objectmanager.create.ObjectCreator;
import org.apache.isis.core.metamodel.objectmanager.load.ObjectLoader;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.persistence.adapter.PojoAdapter;
import org.apache.isis.core.runtime.session.IsisSession;

import static org.apache.isis.core.commons.internal.functions._Predicates.not;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Converts serializable {@link Data} back to a {@link ManagedObject}. 
 * 
 * @since 2.0
 */
@RequiredArgsConstructor
@Log4j2
final class ObjectUnmarshaller {

    private final ObjectManager objectManager;
    @Getter private final SpecificationLoader specificationLoader; 
    
    ManagedObject recreateObject(Data data) {
        if (data == null) {
            return null;
        }
        val spec = specificationLoader.lookupBySpecIdElseLoad(data.getObjectSpecId());
        val oid = data.getOid();
        return recreateObject(spec, oid, data);
    }
    
    ManagedObject adapterForListOfPojos(List<Object> listOfPojos) {
        return ManagedObject.of(specificationLoader::loadSpecification, listOfPojos);
    }
    
    // -- HELPER
    
    private ManagedObject recreateObject(ObjectSpecification spec, Oid oid, Data data) {
        
        ManagedObject adapter;

        if (spec.isParentedOrFreeCollection()) {

            final Supplier<Object> emptyCollectionPojoFactory = 
                    ()->instantiateAndInjectServices(spec);

            final Object collectionPojo = populateCollection(
                    emptyCollectionPojoFactory, 
                    spec, 
                    (CollectionData) data);
            
            
            final ParentedOid collectionOid = (ParentedOid) oid;
            adapter = PojoAdapter.of(
                    collectionPojo, collectionOid,
                    IsisSession.currentOrElseNull());

        } else {
            _Assert.assertTrue("oid must be a RootOid representing an object because spec is not a collection and cannot be a value", oid instanceof RootOid);
            RootOid typedOid = (RootOid) oid;
            // recreate an adapter for the original OID
            adapter = adapterForOid(typedOid);

            updateObject(adapter, data);
        }

        if (log.isDebugEnabled()) {
            log.debug("recreated object {}", oid);
        }
        return adapter;
    }
    
    private Object instantiateAndInjectServices(ObjectSpecification spec) {
        
        val objectCreateRequest = ObjectCreator.Request.of(spec);
        return objectManager.createObject(objectCreateRequest);
    }

    private ManagedObject recreateReference(Data data) {
        // handle values
        if (data instanceof StandaloneData) {
            val standaloneData = (StandaloneData) data;
            return standaloneData.getAdapter(this::adapterForPojo, specificationLoader);
        }

        // reference to entity

        Oid oid = data.getOid();
        _Assert.assertTrue("can only create a reference to an entity", oid instanceof RootOid);

        val rootOid = (RootOid) oid;
        val referencedAdapter = adapterForOid(rootOid);

        if (data instanceof ObjectData) {
            if (rootOid.isTransient()) {
                updateObject(referencedAdapter, data);
            }
        }
        return referencedAdapter;
    }



    private void updateObject(final ManagedObject adapter, final Data data) {
        
        val oid = objectManager.identifyObject(adapter);
        
        if (!Objects.equals(oid, data.getOid())) {
            throw new IllegalArgumentException(
                    "This memento can only be used to update the ObjectAdapter with the Oid " + data.getOid() + " but is " + oid);
        }
        if (!(data instanceof ObjectData)) {
            throw new IsisException("Expected ObjectData but got " + data.getClass());
        }

        updateFieldsAndResolveState(adapter, data);

        if (log.isDebugEnabled()) {
            log.debug("object updated {}", oid);
        }
    }

    private Object populateCollection(
            final Supplier<Object> emptyCollectionPojoFactory, 
            final ObjectSpecification collectionSpec, 
            final CollectionData state) {

        final Stream<ManagedObject> initData = state.streamElements()
                .map(this::recreateReference);

        val collectionFacet = collectionSpec.getFacet(CollectionFacet.class);
        return collectionFacet.populatePojo(
                emptyCollectionPojoFactory, collectionSpec, initData, state.getElementCount());
    }

    private void updateFieldsAndResolveState(final ManagedObject adapter, final Data data) {

        val spec = adapter.getSpecification();
        
        boolean dataIsTransient = data.getOid().isTransient();

        if (!dataIsTransient) {
            updateFields(adapter, data);
            
        } else if (dataIsTransient 
                && ManagedObject._entityState(adapter).isDetached()) {
            updateFields(adapter, data);

        } else if (spec.isParented()) {
            // this branch is kind-a wierd, I think it's to handle aggregated adapters.
            updateFields(adapter, data);

        } else {
            final ObjectData od = (ObjectData) data;
            if (od.hasAnyField()) {
                throw _Exceptions.unrecoverableFormatted(
                        "Resolve state (for %s) inconsistent with fact that data exists for fields", 
                        adapter); 
            }
        }
    }

    private void updateFields(final ManagedObject adapter, final Data state) {
        val objectData = (ObjectData) state;
        
        adapter.getSpecification().streamAssociations(Contributed.EXCLUDED)
        .filter(field->{
            if (field.isNotPersisted()) {
                if (field.isOneToManyAssociation()) {
                    return false;
                }
                if (field.containsFacet(PropertyOrCollectionAccessorFacet.class) 
                        && !field.containsFacet(PropertySetterFacet.class)) {
                    
                    log.debug("ignoring not-settable field {}", field.getName());
                    return false;
                }
            }
            return true;
        })
        .forEach(field->updateField(adapter, objectData, field));

    }

    private void updateField(
            final ManagedObject adapter, 
            final ObjectData objectData, 
            final ObjectAssociation objectAssoc) {
        
        final Object fieldData = objectData.getEntry(objectAssoc.getId());

        if (objectAssoc.isOneToManyAssociation()) {
            updateOneToManyAssociation(adapter, (OneToManyAssociation) objectAssoc, (CollectionData) fieldData);

        } else if (objectAssoc.getSpecification().containsFacet(EncodableFacet.class)) {
            final EncodableFacet facet = objectAssoc.getSpecification().getFacet(EncodableFacet.class);
            final ManagedObject value = facet.fromEncodedString((String) fieldData);
            ((OneToOneAssociation) objectAssoc).initAssociation(adapter, value);

        } else if (objectAssoc.isOneToOneAssociation()) {
            updateOneToOneAssociation(adapter, (OneToOneAssociation) objectAssoc, (Data) fieldData);
        }
    }

    private void updateOneToManyAssociation(
            ManagedObject objectAdapter, 
            OneToManyAssociation otma, 
            CollectionData collectionData) {

        val collection = otma.get(objectAdapter, InteractionInitiatedBy.FRAMEWORK);
        
        final Set<ManagedObject> original = CollectionFacet.Utils.streamAdapters(collection)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        final Set<ManagedObject> incoming = collectionData.streamElements()
                .map(this::recreateReference)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        incoming.stream()
        .filter(original::contains)
        .forEach(elementAdapter->{
            if (log.isDebugEnabled()) {
                log.debug("  association {} changed, added {}", otma, ManagedObject._identify(elementAdapter));
            }
            otma.addElement(objectAdapter, elementAdapter, InteractionInitiatedBy.FRAMEWORK);
        });

        original.stream()
        .filter(not(incoming::contains))
        .forEach(elementAdapter->{
            if (log.isDebugEnabled()) {
                log.debug("  association {} changed, removed {}", otma, ManagedObject._identify(elementAdapter));
            }
            otma.removeElement(objectAdapter, elementAdapter, InteractionInitiatedBy.FRAMEWORK);
        });

    }

    private void updateOneToOneAssociation(
            final ManagedObject objectAdapter,
            final OneToOneAssociation otoa, 
            final Data assocData) {
        
        if (assocData == null) {
            otoa.initAssociation(objectAdapter, null);
        } else {
            final ManagedObject ref = recreateReference(assocData);
            if (otoa.get(objectAdapter, InteractionInitiatedBy.FRAMEWORK) != ref) {
                if (log.isDebugEnabled()) {
                    log.debug("  association {} changed to {}", otoa, ManagedObject._identify(ref));
                }
                otoa.initAssociation(objectAdapter, ref);
            }
        }
    }
    
    private ManagedObject adapterForOid(RootOid oid) {
        val spec = specificationLoader.loadSpecification(oid.getObjectSpecId()); 
        val objectLoadRequest = ObjectLoader.Request.of(spec, oid.getIdentifier());
        return objectManager.loadObject(objectLoadRequest);
    }
    
    private ManagedObject adapterForPojo(Object pojo) {
        return objectManager.adapt(pojo);
    }
    
    
}
