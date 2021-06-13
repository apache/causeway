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
package org.apache.isis.applib.services.jaxb;

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Value;
import org.apache.isis.applib.mixins.dto.Dto_downloadXsd;

/**
 * Controls whether, when generating {@link JaxbService#toXsd(Object, IsisSchemas) XML schemas},
 * any of the common Isis schemas (in the namespace <code>http://org.apache.isis.schema</code>) should be included
 * or just ignored (and therefore don't appear in the returned map).
 *
 * <p>
 * The practical benefit of this is that for many DTOs there will only be one other
 * schema, that of the DTO itself.  The {@link Dto_downloadXsd} mixin uses this to return that single XSD,
 * rather than generating a ZIP of two schemas (the Isis schema and the one for the DTO), as it would otherwise;
 * far more convenient when debugging and so on.  The Isis schemas can always be
 * <a href="http://isis.apache.org/schema">downloaded</a> from the Isis website.
 * </p>
 */
@Value(logicalTypeName = IsisModuleApplib.NAMESPACE + ".services.jaxb.IsisSchemas")
public enum IsisSchemas {
    INCLUDE,
    IGNORE;

    /**
     * Implementation note: not using subclasses, otherwise the key in translations.po becomes more complex.
     */
    public boolean shouldIgnore(final String namespaceUri) {
        if (this == INCLUDE) {
            return false;
        } else {
            return namespaceUri.matches(".*isis\\.apache\\.org.*");
        }
    }
}
