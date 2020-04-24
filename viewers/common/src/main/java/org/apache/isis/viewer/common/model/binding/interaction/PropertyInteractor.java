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
package org.apache.isis.viewer.common.model.binding.interaction;

import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.commons.internal.base._Either;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.common.model.binding.interaction.InteractionResponse.Veto;
import org.apache.isis.viewer.common.model.binding.interaction.ObjectInteractor.AccessIntent;

import lombok.val;

public class PropertyInteractor extends MemberInteractor {

    private final String propertyId;
    private final Where where;
    private final AccessIntent accessIntent;
    
    public PropertyInteractor(
            final ObjectInteractor objectInteractor,
            final String propertyId,
            final Where where,
            final AccessIntent accessIntent) {
        super(objectInteractor);
        this.propertyId = propertyId;
        this.where = where;
        this.accessIntent = accessIntent;
    }

    public _Either<OneToOneAssociation, InteractionResponse> getPropertyThatIsVisibleForIntent() {

        val managedObject = objectInteractor.getManagedObject();
        
        val spec = managedObject.getSpecification();
        val property = spec.getAssociation(propertyId).orElse(null);
        if(property==null || !property.isOneToOneAssociation()) {
            return super.notFound(MemberType.PROPERTY, propertyId, Veto.NOT_FOUND);
        }
        
        return super.memberThatIsVisibleForIntent(
                MemberType.PROPERTY, (OneToOneAssociation)property, where, accessIntent);
    }

    public PropertyInteractor onFailure(@Nullable final BiConsumer<InteractionResponse, String> onFailure) {
        super.onFailure = onFailure;
        return this;
    }

    public InteractionResponse modifyProperty(Function<OneToOneAssociation, ManagedObject> newProperyValueProvider) {
        
        val check = getPropertyThatIsVisibleForIntent();
        if(check.isRight()) {
            return check.rightIfAny();
        }

        val property = check.leftIfAny();
        val proposedNewValue = newProperyValueProvider.apply(property);
        
        return objectInteractor.modifyProperty(property, proposedNewValue);
    }

    

}
