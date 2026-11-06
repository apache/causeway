/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.causeway.viewer.wicket.ui.pages.obj;

import static org.mockito.Mockito.*;

import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;

/** Access to the production authorization/admission boundary from the controlled export fixture. */
public final class PrimerViewTracingSupport {
    public static void prime(final Object page, final ManagedObject root, final ServiceRegistry services) {
        var model = mock(UiObjectWkt.class);
        when(model.isVisible()).thenReturn(true);
        DomainObjectPage.primeForRendering(page, model, root, services);
    }
}
