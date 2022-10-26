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
package org.apache.causeway.core.metamodel.interactions.managed;

import java.util.function.UnaryOperator;

import org.apache.causeway.applib.value.semantics.Parser;
import org.apache.causeway.applib.value.semantics.Renderer;
import org.apache.causeway.commons.binding.Bindable;
import org.apache.causeway.commons.binding.Observable;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import lombok.val;

public interface ManagedValue {

    ObjectSpecification getElementType();

    Bindable<ManagedObject> getValue();

    /** Corresponds to value type semantics as provided by {@link Renderer}. */
    Observable<String> getValueAsTitle();
    /** Corresponds to value type semantics as provided by {@link Renderer}. */
    Observable<String> getValueAsHtml();

    /** Corresponds to whether the value type has a {@link Parser}. */
    boolean isValueAsParsableTextSupported();
    /**
     * Corresponds to value type semantics as provided by {@link Parser}.
     * Value types should have associated parsers/formatters via value semantics,
     * except for composite value types, which might have not.
     */
    Bindable<String> getValueAsParsableText();

    Observable<String> getValidationMessage();
    Bindable<String> getSearchArgument();
    Observable<Can<ManagedObject>> getChoices();

    default void update(final UnaryOperator<ManagedObject> updater) {
        val valueHolder = getValue();
        val oldValue = valueHolder.getValue();
        val newValue = updater.apply(oldValue);
        valueHolder.setValue(newValue);
    }

    /**
     * Clears the pending value to an empty value.
     */
    default void clear() {
        update(oldValue->ManagedObject.empty(getElementType()));
    }

    /**
     * Whether the pending value is present (not absent, null or empty).
     */
    default boolean isPresent() {
        return !ManagedObjects.isNullOrUnspecifiedOrEmpty(getValue().getValue());
    }

}
