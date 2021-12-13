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
package org.apache.isis.viewer.wicket.ui.components.scalars;

import java.io.Serializable;

import org.apache.wicket.util.convert.IConverter;

import org.apache.isis.applib.value.semantics.ValueSemanticsProvider;
import org.apache.isis.core.metamodel.commons.ScalarRepresentation;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeature;
import org.apache.isis.viewer.wicket.model.converter.ConverterBasedOnValueSemantics;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;

import lombok.NonNull;

/**
 * Specialization of {@link ScalarPanelTextFieldAbstract},
 * where the scalar (parameter or property) is a value-type,
 * using conversion that is backed by a {@link ValueSemanticsProvider}.
 */
public abstract class ScalarPanelTextFieldWithValueSemanticsAbstract<T extends Serializable>
extends ScalarPanelTextFieldAbstract<T> {

    private static final long serialVersionUID = 1L;

    protected ScalarPanelTextFieldWithValueSemanticsAbstract(
            final String id,
            final ScalarModel scalarModel,
            final Class<T> type) {
        super(id, scalarModel, type);
    }

    @Override
    protected final IConverter<T> getConverter(
            final @NonNull ObjectFeature propOrParam,
            final @NonNull ScalarRepresentation scalarRepresentation) {
        return new ConverterBasedOnValueSemantics<>(propOrParam, scalarRepresentation);
    }

}
