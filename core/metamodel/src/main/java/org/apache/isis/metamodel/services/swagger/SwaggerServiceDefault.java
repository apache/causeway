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
package org.apache.isis.metamodel.services.swagger;

import lombok.extern.log4j.Log4j2;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.swagger.SwaggerService;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.metamodel.services.swagger.internal.SwaggerSpecGenerator;
import org.apache.isis.metamodel.specloader.SpecificationLoader;

import static org.apache.isis.commons.internal.base._Strings.prefix;
import static org.apache.isis.commons.internal.base._With.ifPresentElse;
import static org.apache.isis.commons.internal.resources._Resources.getRestfulPathIfAny;
import static org.apache.isis.commons.internal.resources._Resources.prependContextPathIfPresent;

@Service
@Named("isisMetaModel.swaggerServiceDefault")
@Log4j2
public class SwaggerServiceDefault implements SwaggerService {

    @Inject SpecificationLoader specificationLoader;
    private final SwaggerSpecGenerator swaggerSpecGenerator;

    public SwaggerServiceDefault(SwaggerSpecGenerator swaggerSpecGenerator) {
        this.swaggerSpecGenerator = swaggerSpecGenerator;
    }

    @Override
    public String generateSwaggerSpec(
            final Visibility visibility,
            final Format format) {

        final String swaggerSpec = swaggerSpecGenerator.generate(basePath.get(), visibility, format);
        return swaggerSpec;
    }

    // -- HELPER

    private _Lazy<String> basePath = _Lazy.threadSafe(this::lookupBasePath);

    private String lookupBasePath() {
        final String restfulPath = ifPresentElse(getRestfulPathIfAny(), "undefined");
        return prefix(prependContextPathIfPresent(restfulPath), "/");
    }




}
