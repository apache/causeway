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
package demoapp.dom.services.core.wrapperFactory;

import org.apache.isis.applib.annotations.Action;
import org.apache.isis.applib.annotations.ActionLayout;
import org.apache.isis.applib.annotations.MemberSupport;
import org.apache.isis.applib.annotations.SemanticsOf;

import lombok.RequiredArgsConstructor;

//tag::class[]
@Action(
    semantics = SemanticsOf.IDEMPOTENT
)
@ActionLayout(
    named = "Update Property Async"
    , describedAs = "Mixin that Updates 'property async mixin' directly"
    , associateWith = "propertyAsyncMixin"
    , sequence = "2"
)
@RequiredArgsConstructor
public class WrapperFactoryEntity_updatePropertyAsyncMixin {
    // ...
//end::class[]

    private final WrapperFactoryEntity wrapperFactoryEntity;

//tag::class[]
    @MemberSupport public WrapperFactoryEntity act(final String value) {
        wrapperFactoryEntity.setPropertyAsyncMixin(value);
        return wrapperFactoryEntity;
    }
    @MemberSupport public String default0Act() {
        return wrapperFactoryEntity.getPropertyAsyncMixin();
    }
}
//end::class[]
