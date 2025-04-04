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
package org.apache.causeway.core.metamodel.facets.all.i8n.imperative;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.services.i18n.TranslatableString;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.core.metamodel.object.ManagedObject;

public interface HasImperativeText {

    /**
     * Provide text for the target object.
     * <p>
     * Translated or not, based on whether corresponding support method returns
     * {@link TranslatableString} or just {@link String}.
     * <p>
     * eg. title, name, description
     */
    Try<String> text(ManagedObject object);

    @Nullable
    default String textElseNull(final ManagedObject object) {
        return text(object).ifFailureFail().getValue().orElse(null);
    }

}
