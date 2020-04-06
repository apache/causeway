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
package org.apache.isis.viewer.common.model.link;

import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;

import lombok.Getter;

/**
 * 
 * @since Apr 6, 2020
 *
 * @param <T> - link component type, native to the viewer
 */
public class LinkAndLabelUiModel<T> extends LinkAndLabelUiModelAbstract {

    private static final long serialVersionUID = 1L;

    public static <X> LinkAndLabelUiModel<X> newLinkAndLabel(
            final X linkComponent,
            final ManagedObject objectAdapter,
            final ObjectAction objectAction,
            final boolean blobOrClob) {
        return new LinkAndLabelUiModel<>(linkComponent, objectAdapter, objectAction, blobOrClob);
    }

    @Getter private final T linkComponent;

    protected LinkAndLabelUiModel(
            final T linkComponent,
            final ManagedObject objectAdapter,
            final ObjectAction objectAction,
            final boolean blobOrClob) {
        super(objectAdapter, objectAction, blobOrClob);
        this.linkComponent = linkComponent;
    }
    
}
