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
package org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable;

import java.util.Locale;

import org.apache.isis.commons.internal.base._Timing;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.viewer.wicket.model.common.CommonContextUtils;

import lombok.val;

/**
 * Responsibility: produce additional info when in prototyping mode 
 * eg. render/response timing
 * <p>
 * currently used by the framework's NavigationToolbars to add a 
 * 'took seconds' label to bottom of data tables 
 * 
 * @since 2.0
 */
class PrototypingMessageProvider {

    private static IsisAppCommonContext commonContext = null;

    public static String getTookTimingMessageModel() {
        return isPrototyping()
                ? getTookTimingMessage()
                        : "";
    }

    // -- HELPER

    private static IsisAppCommonContext commonContext() {
        return commonContext = CommonContextUtils.computeIfAbsent(commonContext);
    }
    
    private static boolean isPrototyping() {
        return commonContext().getSystemEnvironment().isPrototyping();
    }

    private static String getTookTimingMessage() {

        final StringBuilder tookTimingMessage = new StringBuilder();

        commonContext().getIsisInteractionTracker().currentInteraction()
        .ifPresent(interaction->{
            val stopWatch = _Timing.atSystemNanos(interaction.getLifecycleStartedAtSystemNanos());    
            tookTimingMessage.append(String.format(Locale.US, "... took %.2f seconds", stopWatch.getSeconds()));
        });

        return tookTimingMessage.toString();
    }

}
