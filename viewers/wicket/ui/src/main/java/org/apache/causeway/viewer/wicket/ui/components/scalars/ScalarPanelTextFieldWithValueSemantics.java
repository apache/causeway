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
package org.apache.causeway.viewer.wicket.ui.components.scalars;

import java.util.Optional;

import org.apache.wicket.util.convert.IConverter;

import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.viewer.wicket.model.models.ScalarModel;

/**
 * Specialization of {@link ScalarPanelTextFieldAbstract},
 * where the scalar (parameter or property) is a value-type,
 * using conversion that is backed by a {@link ValueSemanticsProvider}.
 */
public class ScalarPanelTextFieldWithValueSemantics<T>
extends ScalarPanelTextFieldAbstract<T> {

    private static final long serialVersionUID = 1L;

    public ScalarPanelTextFieldWithValueSemantics(
            final String id,
            final ScalarModel scalarModel,
            final Class<T> type) {
        super(id, scalarModel, type);
    }

    @Override
    protected final Optional<IConverter<T>> converter() {
        return scalarModel().getConverter(type);
    }

}
