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

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.ViewModel;

// tag::refguide[]
public interface SwaggerService {

// end::refguide[]
// tag::refguide-1[]
    enum Visibility {
        /**
         * Specification for use by third-party clients, ie public use.
         *
         * <p>
         * Restricted only to view models ({@link ViewModel} or equivalent) and {@link DomainService} with a nature
         * of {@link NatureOfService#VIEW_REST_ONLY}.
         * </p>
         */
        PUBLIC,
        /**
         * Specification for use only by internally-managed clients, ie private internal use.
         *
         * <p>
         * Includes specifications of domain entities as well as view models.
         * </p>
         */
        PRIVATE,
        /**
         * As {@link #PRIVATE}, also including any prototype actions (where {@link Action#restrictTo()} set to
         * {@link RestrictTo#PROTOTYPING}).
         */
        PRIVATE_WITH_PROTOTYPING;

        public boolean isPublic() {
            return this == PUBLIC;
        }
    }
// end::refguide-1[]
// tag::refguide[]

    enum Format {
        JSON,
        YAML;
// end::refguide[]
        /**
         * Implementation note: not using subclasses, otherwise the key in translations.po becomes more complex.
         */
        public String mediaType() {
            if(this == JSON) {
                return "text/json";
            } else {
                return "application/yaml";
            }
        }
// tag::refguide[]
    }

    String generateSwaggerSpec(final Visibility visibility, final Format format);

}
// end::refguide[]
