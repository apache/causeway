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

import org.apache.wicket.model.IModel;

//TODO[causeway-viewer-wicket-model-CAUSEWAY-3859] intermediate refactoring helper, perhaps remove later
public class DelegatingModel<T> implements IModel<T> {
    private static final long serialVersionUID = 1L;

    private IModel<T> delegate;

    public DelegatingModel(final IModel<T> modelObject) {
        this.delegate = modelObject;
    }

    @Override
    public final void detach() {
        if(delegate!=null) delegate.detach();
    }

    @Override
    public final void setObject(final T object) {
        delegate.setObject(object);
    }

    @Override
    public final T getObject() {
        return delegate!=null
            ? delegate.getObject()
            : null;
    }

    public final IModel<?> getChainedModel() {
        return delegate;
    }

    @Override
    public String toString() {
        return new StringBuilder("Model:classname=[")
            .append(getClass().getName()).append(']')
            .append(":nestedModel=[").append(delegate).append(']')
            .toString();
    }

    public final Object getInnermostModelOrObject() {
        Object object = delegate;
        while (object instanceof IModel) {
            Object tmp = ((IModel<?>)object).getObject();
            if (tmp == object) break;
            object = tmp;
        }
        return object;
    }
}
