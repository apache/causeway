package org.apache.isis.testdomain.interact;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.apache.isis.core.commons.internal.base._NullSafe;
import org.apache.isis.core.commons.internal.collections._Arrays;
import org.apache.isis.core.metamodel.interactions.managed.ActionInteraction;
import org.apache.isis.core.metamodel.interactions.managed.CollectionInteraction;
import org.apache.isis.core.metamodel.interactions.managed.PropertyInteraction;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.runtime.iactn.IsisInteractionFactory;
import org.apache.isis.testing.integtestsupport.applib.IsisIntegrationTestAbstract;

import lombok.val;

public abstract class InteractionTestAbstract extends IsisIntegrationTestAbstract {
    
    @Inject protected ObjectManager objectManager;
    @Inject protected IsisInteractionFactory interactionFactory;

    protected ActionInteraction startActionInteractionOn(Class<?> type, String actionId) {
        val viewModel = factoryService.viewModel(type);
        val managedObject = objectManager.adapt(viewModel);
        return ActionInteraction.start(managedObject, actionId);
    }
    
    protected PropertyInteraction startPropertyInteractionOn(Class<?> type, String propertyId) {
        val viewModel = factoryService.viewModel(type);
        val managedObject = objectManager.adapt(viewModel);
        return PropertyInteraction.start(managedObject, propertyId);
    }
    
    protected CollectionInteraction startCollectionInteractionOn(Class<?> type, String collectionId) {
        val viewModel = factoryService.viewModel(type);
        val managedObject = objectManager.adapt(viewModel);
        return CollectionInteraction.start(managedObject, collectionId);
    }

    
    protected void assertComponentWiseEquals(Object a, Object b) {
        
        val array1 = _NullSafe.streamAutodetect(a)
            .collect(_Arrays.toArray(Object.class));
        val array2 = _NullSafe.streamAutodetect(b)
                .collect(_Arrays.toArray(Object.class));
        
        assertArrayEquals(array1, array2);
        
    }
    
}
