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
package org.apache.isis.viewer.wicket.model.models;

import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtime.memento.ObjectAdapterMemento;

public interface ObjectAdapterModel extends IModel<ManagedObject> {

    ObjectAdapterMemento getContextAdapterIfAny();
    void setContextAdapterIfAny(ObjectAdapterMemento contextAdapterIfAny);

    EntityModel.RenderingHint getRenderingHint();
    void setRenderingHint(EntityModel.RenderingHint renderingHint);

    PageParameters getPageParametersWithoutUiHints();

    ObjectSpecification getTypeOfSpecification();

    EntityModel.Mode getMode();

    PageParameters getPageParameters();

    boolean isInlinePrompt();
}
