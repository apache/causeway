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
package org.apache.isis.metamodel.valuetypes;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;

import javax.inject.Named;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.util.schema.CommonDtoUtils;
import org.apache.isis.metamodel.spec.FreeStandingList;
import org.apache.isis.schema.common.v1.ValueType;

@Component
@Named("isisMetaModel.ValueTypeProviderCollections")
@Order(OrderPrecedence.MIDPOINT)
public class ValueTypeProviderForCollections implements ValueTypeProvider {

    @Override
    public Collection<ValueTypeDefinition> definitions() {
        return Collections.unmodifiableList(Arrays.asList(
                    ValueTypeDefinition.collection(List.class),
                    ValueTypeDefinition.collection(Set.class),
                    ValueTypeDefinition.collection(SortedSet.class),
                    ValueTypeDefinition.collection(FreeStandingList.class)
                ));
    }

}
