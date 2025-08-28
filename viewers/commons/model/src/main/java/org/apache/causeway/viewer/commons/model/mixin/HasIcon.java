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
package org.apache.causeway.viewer.commons.model.mixin;

import java.util.Objects;
import java.util.function.Consumer;

import org.apache.causeway.applib.annotation.ObjectSupport.IconWhere;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.facets.object.icon.ObjectIcon;
import org.apache.causeway.core.metamodel.facets.object.icon.ObjectIconEmbedded;
import org.apache.causeway.core.metamodel.facets.object.icon.ObjectIconFa;
import org.apache.causeway.core.metamodel.facets.object.icon.ObjectIconUrlBased;

@FunctionalInterface
public interface HasIcon {

    ObjectIcon getIcon(IconWhere iconWhere);

    default void visitIconVariant(
        IconWhere iconWhere,
        Consumer<ObjectIconUrlBased> a,
        Consumer<ObjectIconEmbedded> b,
        Consumer<ObjectIconFa> c) {

        var objectIcon = Objects.requireNonNull(getIcon(iconWhere));
        if(objectIcon instanceof ObjectIconUrlBased urlBased){
            a.accept(urlBased);
        } else if(objectIcon instanceof ObjectIconEmbedded embedded){
            b.accept(embedded);
        } else if(objectIcon instanceof ObjectIconFa fa){
            c.accept(fa);
        } else {
            throw _Exceptions.unmatchedCase(objectIcon);
        }
    }

}
