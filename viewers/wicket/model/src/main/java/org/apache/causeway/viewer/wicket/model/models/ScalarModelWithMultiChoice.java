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

import java.util.ArrayList;
import java.util.stream.Collectors;

import org.apache.wicket.model.ChainingModel;
import org.apache.wicket.model.Model;

import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.objectmanager.memento.ObjectMemento;

import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * For widgets that use a <tt>org.wicketstuff.select2.Select2MultiChoice</tt>;
 * synchronizes the {@link Model} of the <tt>Select2MultiChoice</tt>
 * with the parent {@link ScalarModel}, allowing also for pending values.
 */
@Log4j2
public class ScalarModelWithMultiChoice
extends ChainingModel<ArrayList<ObjectMemento>>
implements
    ScalarModelWithChoice<ArrayList<ObjectMemento>> {

    private static final long serialVersionUID = 1L;

    // -- FACTORY

    public static ScalarModelWithMultiChoice chain(final @NonNull ScalarModel scalarModel) {
        return new ScalarModelWithMultiChoice(scalarModel);
    }

    // -- CONSTRUCTION

    private ScalarModelWithMultiChoice(final ScalarModel scalarModel) {
        super(scalarModel); // chaining to scalarModel
    }

    /**
     * chaining idiom: the {@link ScalarModel} we are chained to
     */
    @Override
    public ScalarModel scalarModel() {
        return (ScalarModel) super.getTarget();
    }

    @Override
    public ArrayList<ObjectMemento> getObject() {

        val packedValue = pendingValue().getValue().getValue();
        val unpackedValues = ManagedObjects.unpack(scalarModel().getScalarTypeSpec(), packedValue);

        log.debug("getObject() as unpackedValue {}", unpackedValues);

        val mementos = unpackedValues.stream()
        .map(ManagedObject::getMementoElseFail)
        .collect(Collectors.toCollection(()->new ArrayList<ObjectMemento>()));

        log.debug("getObject() as unpackedMemento {}", mementos);
        return mementos;
    }

    @Override
    public void setObject(final ArrayList<ObjectMemento> unpackedMemento) {
        log.debug("setObject() as unpackedMemento {}", unpackedMemento);
        val objectFeature = scalarModel().getObjectFeature();
        val packedMemento = ObjectMemento.pack(objectFeature, unpackedMemento);
        pendingValue().getValue().setValue(getObjectManager().demementify(objectFeature, packedMemento));
    }


}