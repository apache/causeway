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
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.apache.causeway.core.metamodel.object.ManagedObject;

import lombok.NonNull;
import lombok.val;

public class BooleanModel
extends ChainingModel<Boolean> {

    private static final long serialVersionUID = 1L;

    public static BooleanModel forScalarModel(final @NonNull ScalarModel scalarModel) {
        return new BooleanModel(scalarModel);
    }

    final boolean isPrimitive;

    protected BooleanModel(final ScalarModel scalarModel) {
        super(scalarModel);

        val spec = scalarModel().getScalarTypeSpec();
        this.isPrimitive = spec.getFullIdentifier().equals("boolean");
    }

    @Override
    public Boolean getObject() {
        val adapter = scalarModel().getObject();
        final Boolean value = adapter != null
                ? (Boolean) adapter.getPojo()
                : null;
        return (isPrimitive
                    && value==null)
                ? Boolean.FALSE
                : value;
    }

    @Override
    public void setObject(final Boolean value) {
        val adaptedValue = ManagedObject.value(
                scalarModel().getScalarTypeSpec(),
                (value==null
                    && isPrimitive)
                ? Boolean.FALSE
                : value);
        scalarModel().setObject(adaptedValue);
    }

    // -- UTILITY

    /**
     * Translates boolean to text.
     * eg used for checkbox inline edit form fields
     */
    public IModel<String> asStringModel(
            final String notSetLiteral,
            final String trueLiteral,
            final String falseLiteral) {

        return new Model<String>() {
            private static final long serialVersionUID = 1L;

            @Override public String getObject() {
                final Boolean bool = BooleanModel.this.getObject();
                return bool == null
                        ? notSetLiteral // '(not set)'
                        : bool
                            ? trueLiteral // 'yes'
                            : falseLiteral; // 'no'
            }
        };
    }

    // -- HELPER

    protected ScalarModel scalarModel() {
        return (ScalarModel) super.getTarget();
    }

}
