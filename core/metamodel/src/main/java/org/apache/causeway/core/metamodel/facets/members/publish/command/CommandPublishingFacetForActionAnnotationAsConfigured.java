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
package org.apache.causeway.core.metamodel.facets.members.publish.command;

import org.apache.causeway.applib.services.inject.ServiceInjector;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;

public abstract class CommandPublishingFacetForActionAnnotationAsConfigured extends CommandPublishingFacetForActionAnnotation {

    static class All extends CommandPublishingFacetForActionAnnotationAsConfigured {
        All(FacetHolder holder, ServiceInjector servicesInjector) {
            super(holder, servicesInjector);
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    static class None extends CommandPublishingFacetForActionAnnotationAsConfigured {
        None(FacetHolder holder, ServiceInjector servicesInjector) {
            super(holder, servicesInjector);
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
    }

    static class IgnoreSafe extends CommandPublishingFacetForActionAnnotationAsConfigured {
        IgnoreSafe(FacetHolder holder, ServiceInjector servicesInjector) {
            super(holder, servicesInjector);
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
    }

    static class IgnoreSafeYetNot extends CommandPublishingFacetForActionAnnotationAsConfigured {
        IgnoreSafeYetNot(FacetHolder holder, ServiceInjector servicesInjector) {
            super(holder, servicesInjector);
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    CommandPublishingFacetForActionAnnotationAsConfigured(
            final FacetHolder holder,
            final ServiceInjector servicesInjector) {
        super(null, holder, servicesInjector);
    }

}
