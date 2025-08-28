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
package org.apache.causeway.core.metamodel.facets.object.title;

import java.util.function.Predicate;

import org.jspecify.annotations.NonNull;

import org.apache.causeway.commons.internal.functions._Predicates;
import org.apache.causeway.core.metamodel.object.ManagedObject;

public record TitleRenderRequest(
    @NonNull ManagedObject object,
    /**
     * Provide a title for the target object, possibly abbreviated (according to supplied predicate)
     *
     * <p>One reason why the title might be abbreviated is if it is being evaluated in the context
     * of another object.
     * For example as a child object of a parented collection of some parent object.
     * In such a context, the title might be shortened so that it does not needlessly incorporate
     * the title of the parent (context) object.
     */
    @NonNull Predicate<ManagedObject> skipTitlePartEvaluator) {

    public static TitleRenderRequest forObject(final ManagedObject object) {
        return new TitleRenderRequest(object,  _Predicates.alwaysFalse());
    }

}
