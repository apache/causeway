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
package org.apache.isis.viewer.wicket.model.models.interaction.prop;

import java.util.Optional;
import java.util.stream.Stream;

import org.apache.wicket.model.ChainingModel;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.interactions.managed.ManagedProperty;
import org.apache.isis.core.metamodel.interactions.managed.PropertyInteraction;
import org.apache.isis.core.metamodel.interactions.managed.PropertyNegotiationModel;
import org.apache.isis.viewer.wicket.model.models.interaction.BookmarkedObjectWkt;
import org.apache.isis.viewer.wicket.model.models.interaction.HasBookmarkedOwnerAbstract;

/**
 * The parent (container) model of multiple <i>property models</i> which implement
 * {@link ChainingModel}.
 * <pre>
 * IModel[PropertyInteraction] ... setNumber(ComplexNumber complexNumber)
 * |
 * +-- PropertyUiModel ... bound to complexNumber (PropertyNegotiationModel)
 * </pre>
 *
 * @implSpec the state of pending parameters PropertyNegotiationModel is held transient,
 * that means it does not survive a serialization/de-serialization cycle; instead
 * is recreated with property defaults
 *
 * @see ChainingModel
 */
public class PropertyInteractionWkt
extends HasBookmarkedOwnerAbstract<PropertyInteraction> {

    private static final long serialVersionUID = 1L;

    private final String memberId;
    private final Where where;
    private Can<PropertyUiModelWkt> childModels;

    public PropertyInteractionWkt(
            final BookmarkedObjectWkt bookmarkedObject,
            final String memberId,
            final Where where) {

        super(bookmarkedObject);
        this.memberId = memberId;
        this.where = where;
    }

    @Override
    protected PropertyInteraction load() {

        // restore the lazy field - don't evaluate yet
        propertyNegotiationModel =
                _Lazy.threadSafe(()->propertyInteraction().startPropertyNegotiation());

        return PropertyInteraction.wrap(
                ManagedProperty.lookupProperty(getBookmarkedOwner(), memberId, where)
                .orElseThrow(()->_Exceptions.noSuchElement("property '%s' in %s",
                        memberId,
                        getBookmarkedOwner().getSpecification())));
    }

    @Override
    public void detach() {
        super.detach();
        propertyNegotiationModel.clear();
    }

    public final PropertyInteraction propertyInteraction() {
        return getObject();
    }

    // -- LAZY BINDING

    public Stream<PropertyUiModelWkt> streamPropertyUiModels() {
        if(childModels==null) {
            final int tupleIndex = 0;
            childModels = Can.ofSingleton(new PropertyUiModelWkt(this, tupleIndex));
        }
        return childModels.stream();
    }

    // -- PROPERTY NEGOTIATION WITH MEMOIZATION (TRANSIENT)

    private transient _Lazy<Optional<PropertyNegotiationModel>> propertyNegotiationModel;

    public final PropertyNegotiationModel propertyNegotiationModel() {
        _Assert.assertTrue(this.isAttached(), "model is not attached");
        return propertyNegotiationModel.get()
                .orElseThrow(()->_Exceptions.noSuchElement(memberId));
    }

    public void resetPropertyToDefault() {
        propertyNegotiationModel.clear();
    }


}
