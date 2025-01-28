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
package org.apache.causeway.viewer.wicket.model.models;

import org.apache.wicket.model.ChainingModel;
import org.apache.wicket.model.Model;

import org.apache.causeway.core.metamodel.objectmanager.memento.ObjectMemento;

import org.jspecify.annotations.NonNull;

/**
 * For widgets that use a <tt>org.wicketstuff.select2.Select2Choice</tt>;
 * synchronizes the {@link Model} of the <tt>Select2Choice</tt>
 * with the parent {@link UiAttributeWkt}.
 */
//@Log4j2
public class AttributeModelWithSingleChoice
extends ChainingModel<ObjectMemento>
implements
    AttributeModelWithChoice<ObjectMemento> {

    private static final long serialVersionUID = 1L;

    // -- FACTORY

    public static AttributeModelWithSingleChoice chain(final @NonNull UiAttributeWkt attributeModel) {
        return new AttributeModelWithSingleChoice(attributeModel);
    }

    // -- CONSTRUCTION

    private AttributeModelWithSingleChoice(final UiAttributeWkt attributeModel) {
        super(attributeModel); // chaining to attributeModel
    }

    /**
     * chaining idiom: the {@link UiAttributeWkt} we are chained to
     */
    @Override
    public UiAttributeWkt attributeModel() {
        return (UiAttributeWkt) super.getTarget();
    }

    @Override
    public ObjectMemento getObject() {
        return pendingValue().getValue().getValue().getMemento().orElseThrow();
    }

    @Override
    public void setObject(final ObjectMemento memento) {
        pendingValue().getValue().setValue(
                getObjectManager().demementify(memento));
    }

}
