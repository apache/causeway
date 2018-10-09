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

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;

/**
 * For custom {@link ScalarPanelTextFieldAbstract}s to use as the {@link Model}
 * of their {@link TextField} (as constructed in {@link ScalarPanelTextFieldAbstract#createTextFieldForRegular(String)}).
 */
public class TextFieldValueModel<T extends Serializable> extends Model<T> {

    private static final long serialVersionUID = 1L;

    public interface ScalarModelProvider {
        ScalarModel getModel();
        ObjectAdapterProvider getObjectAdapterProvider();
    }

    private final ScalarModelProvider scalarModelProvider;

    public TextFieldValueModel(ScalarModelProvider scalarModelProvider) {
        
        System.out.println("!!! TextFieldValueModel: "+scalarModelProvider.getModel().getObjectAsString());
        
        this.scalarModelProvider = scalarModelProvider;
    }

    @Override
    public T getObject() {
        final ScalarModel model = scalarModelProvider.getModel();
        final ObjectAdapter objectAdapter = model.getObject();
        return asT(objectAdapter);
    }

    @SuppressWarnings("unchecked")
    private T asT(final ObjectAdapter objectAdapter) {
        return (T) (objectAdapter != null? objectAdapter.getPojo(): null);
    }

    @Override
    public void setObject(final T object) {
        if (object == null) {
            scalarModelProvider.getModel().setObject(null);
        } else {
            final ObjectAdapter objectAdapter = scalarModelProvider.getObjectAdapterProvider().adapterFor(object);
            scalarModelProvider.getModel().setObject(objectAdapter);
        }
    }


}