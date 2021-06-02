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
package org.apache.isis.applib.services.swagger;

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.applib.annotation.DomainObject;

/**
 * The format to generate the representation of the swagger spec.
 *
 * @since 1.x {@index}
 */
@DomainObject(logicalTypeName = IsisModuleApplib.NAMESPACE + ".services.swagger.Format")
public enum Format {
    /**
     * Generate a format in JSON (<code>text/json</code> media type).
     */
    JSON,
    /**
     * Generate a format in YAML (<code>application/yaml</code> media type).
     */
    YAML;

    /**
     * Returns the associated media type for each of the formats.
     *
     * <p>
     * Implementation note: not using subclasses of this enum, otherwise the
     * key in <code>translations.po</code> becomes more complex.
     * </p>
     */
    public String mediaType() {
        if (this == JSON) {
            return "text/json";
        } else {
            return "application/yaml";
        }
    }
}
