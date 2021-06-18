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
package org.apache.isis.core.metamodel.valuetypes;

import java.util.Collection;

import javax.inject.Named;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.graph.SimpleEdge;
import org.apache.isis.applib.graph.tree.TreeNode;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.services.appfeat.ApplicationFeatureDefault;
import org.apache.isis.schema.common.v2.ValueType;

@Component
@Named("isis.metamodel.ValueTypeProviderForBuiltin")
@javax.annotation.Priority(PriorityPrecedence.MIDPOINT)
public class ValueTypeProviderForBuiltin implements ValueTypeProvider {

    @Override
    public Collection<ValueTypeDefinition> definitions() {
        return _Lists.of(

                    ValueTypeDefinition.of(Bookmark.class, ValueType.STRING),

                    // these are not yet part of the schema (do not map onto any value-types there)
                    ValueTypeDefinition.of(SimpleEdge.class, ValueType.STRING),
                    ValueTypeDefinition.of(TreeNode.class, ValueType.STRING),

                    ValueTypeDefinition.of(ApplicationFeatureDefault.class, ValueType.STRING)

                );
    }

}
