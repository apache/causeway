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
package org.apache.causeway.core.metamodel.services.tablecol;

import java.util.List;

import javax.annotation.Priority;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.tablecol.TableColumnOrderService;
import org.apache.causeway.core.metamodel.CausewayModuleCoreMetamodel;

/**
 * The default implementation of {@link TableColumnOrderService}.
 * Note though that this implementation has lower priority (later precedence) than
 * {@link TableColumnOrderServiceUsingTxtFile}.
 *
 * @since 1.x {@index}
 *
 * @see TableColumnOrderServiceUsingTxtFile
 */
@Service
@Named(CausewayModuleCoreMetamodel.NAMESPACE + ".TableColumnOrderServiceDefault")
@Priority(PriorityPrecedence.LATE)
@Qualifier("Default")
public class TableColumnOrderServiceDefault implements TableColumnOrderService {

    /**
     * Just returns the <code>propertyIds</code> unchanged.
     *
     * @param parent
     * @param collectionId
     * @param elementType
     * @param associationIds
     */
    @Override
    public List<String> orderParented(
            final Object parent,
            final String collectionId,
            final Class<?> elementType,
            final List<String> associationIds) {
        return associationIds;
    }

    /**
     * Just returns the <code>propertyIds</code> unchanged.
     *
     * @param domainType
     * @param associationIds
     */
    @Override
    public List<String> orderStandalone(
            final Class<?> domainType,
            final List<String> associationIds) {
        return associationIds;
    }
}
