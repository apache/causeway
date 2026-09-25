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
package org.apache.causeway.viewer.wicket.ui.observation;

/**
 * Page-specific compatibility wrapper for semantic Wicket preparation.
 *
 * @since 2.2
 */
public final class WicketPagePreparationObservation
extends WicketPreparationObservation {

    private static final long serialVersionUID = 1L;

    public WicketPagePreparationObservation(
            final WicketRenderObservationDescriptor descriptor) {
        super(requirePagePreparation(descriptor));
    }

    private static WicketRenderObservationDescriptor requirePagePreparation(
            final WicketRenderObservationDescriptor descriptor) {
        if(descriptor == null
                || descriptor.getRegion()
                        != WicketRenderObservationDescriptor.Region.PAGE_PREPARATION) {
            throw new IllegalArgumentException("Page preparation descriptor required");
        }
        return descriptor;
    }
}
