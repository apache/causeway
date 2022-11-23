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
package org.apache.causeway.viewer.restfulobjects.rendering.service.swagger.internal;

import java.util.Optional;

import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.IntegerSchema;

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
enum Caching {
    /**
     * No cache header.
     */
    TRANSACTIONAL {
        @Override
        Optional<Header> header() {
            return Optional.empty();
        }
    },
    USER_INFO {
        @Override
        Optional<Header> header() {
            return Optional.of(new Header()
                    .schema(new IntegerSchema()._default(3600)));
        }
    },
    NON_EXPIRING {
        @Override
        Optional<Header> header() {
            return Optional.of(new Header()
                    .description(RoSpec.CACHE_CONTROL.fqSection())
                    .schema(new IntegerSchema()._default(86400)));
        }
    };

    abstract Optional<Header> header();
}
