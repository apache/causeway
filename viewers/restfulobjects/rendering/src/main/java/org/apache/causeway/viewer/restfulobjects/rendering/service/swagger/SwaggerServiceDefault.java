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
package org.apache.causeway.viewer.restfulobjects.rendering.service.swagger;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.swagger.Format;
import org.apache.causeway.applib.services.swagger.SwaggerService;
import org.apache.causeway.applib.services.swagger.Visibility;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.applib.RestfulPathProvider;
import org.apache.causeway.core.config.viewer.web.WebAppContextPath;
import org.apache.causeway.viewer.restfulobjects.applib.CausewayModuleViewerRestfulObjectsApplib;
import org.apache.causeway.viewer.restfulobjects.rendering.service.swagger.internal.OpenApiSpecGenerator;

/**
 * Default implementation of {@link SwaggerService}.
 *
 * @since 1.x revised for 2.0 {@index}
 */
@Service
@Named(SwaggerServiceDefault.LOGICAL_TYPE_NAME)
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
public class SwaggerServiceDefault implements SwaggerService {

    /**
     * Beware this name uses camelCase rather than the usual PascalCase.
     */
    public static final String LOGICAL_TYPE_NAME =
            CausewayModuleViewerRestfulObjectsApplib.NAMESPACE + ".swaggerServiceDefault";

    private final OpenApiSpecGenerator swaggerSpecGenerator;
    private final String basePath;

    @Inject
    public SwaggerServiceDefault(
            final OpenApiSpecGenerator swaggerSpecGenerator,
            final CausewayConfiguration causewayConfiguration,
            final WebAppContextPath webAppContextPath) {

        this.swaggerSpecGenerator = swaggerSpecGenerator;

        var restfulPathProvider = new RestfulPathProvider(causewayConfiguration);
        var restfulPath = restfulPathProvider.getRestfulPath().orElse("");
        var restfulBase = webAppContextPath.prependContextPath(restfulPath);

        this.basePath = _Strings.suffix(restfulBase, "/");
    }

    @Override
    public String generateSwaggerSpec(
            final Visibility visibility,
            final Format format) {
        return swaggerSpecGenerator.generate(basePath, visibility, format);
    }

}
