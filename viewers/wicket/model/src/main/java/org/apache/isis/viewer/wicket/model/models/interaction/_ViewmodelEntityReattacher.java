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
package org.apache.isis.viewer.wicket.model.models.interaction;

import java.util.UUID;
import java.util.function.UnaryOperator;

import org.apache.isis.commons.internal.debug._Debug;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
final class _ViewmodelEntityReattacher {

    public static boolean appliesTo(final ManagedObject entityOrViewmodel) {
        val spec = entityOrViewmodel.getSpecification();
        //FIXME actually we are interested in whether the viewmodel contains entities or not
        if(!spec.getCorrespondingClass()
                .getSimpleName().contains("InventoryJaxb")) {
            return false;
        }
        return false;
    }

    public static ManagedObject reattach(
            final ManagedObject viewmodel,
            final UnaryOperator<ManagedObject> onDetachedEntity) {

        val spec = viewmodel.getSpecification();

        val currentInteractionId = spec.getMetaModelContext().getInteractionProvider().getInteractionId().get();

        // check whether we have done this already for this request-cycle, and if so skip
        if(canSkip(currentInteractionId)){
            return viewmodel;
        }

        _Debug.log("JAXB reattach for %s", currentInteractionId.toString());

//        spec.streamAssociations(MixedIn.EXCLUDED)
//        .forEach(assoc->{
//            if(assoc.isOneToOneAssociation()) {
//                val prop = assoc.get(viewmodel);
//                EntityUtil.reattach(prop);
//                System.err.printf("reattached (prop) %s->%s%n", assoc.getId(), prop);
//            } else {
//                val coll = assoc.get(viewmodel);
//                EntityUtil.reattach(coll);
//                System.err.printf("reattached (coll) %s->%s%n", assoc.getId(), coll);
//            }
//        });

        return onDetachedEntity.apply(viewmodel);
    }

    // -- HELPER

    /**
     * keeps track of the latest interactionId, for which re-attaching was executed;
     * such that we do that only once per interaction;
     * @implNote future work might use the current interaction and set a custom flag active
     */
    private static final ThreadLocal<UUID> THREAD_LOCAL =
            InheritableThreadLocal.withInitial(()->null);

    /**
     * Returns whether we have done this already for this request-cycle.
     */
    private static boolean canSkip(final UUID currentInteractionId) {
        val previousInteractionId = THREAD_LOCAL.get();
        if(previousInteractionId!=null
                && previousInteractionId.equals(currentInteractionId)) {
            // skip
            return true;
        }
        THREAD_LOCAL.set(currentInteractionId);
        return false;
    }

}
