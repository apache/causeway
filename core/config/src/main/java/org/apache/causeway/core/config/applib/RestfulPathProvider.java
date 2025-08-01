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
package org.apache.causeway.core.config.applib;

import java.util.Optional;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.mixins.rest.Object_openRestApi;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.config.CausewayConfiguration;

/**
 * Exposes the path that the Restful Objects viewer's REST API has been
 * configured for, to the {@link Object_openRestApi} mixin action.
 */
@Component
public record RestfulPathProvider(CausewayConfiguration configuration)
implements Object_openRestApi.RestfulPathProvider {

    @Override
    public Optional<String> getRestfulPath() {
        return _Strings.nonEmpty(configuration.viewer().restfulobjects().basePath());
    }

}
