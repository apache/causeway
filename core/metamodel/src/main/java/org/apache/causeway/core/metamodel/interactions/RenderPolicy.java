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
package org.apache.causeway.core.metamodel.interactions;

import java.io.Serializable;

import org.apache.causeway.core.config.CausewayConfiguration;

import lombok.NonNull;

/**
 * <h1>Troubleshooting Visibility and Usability</h1>
 * <p>
 * There are many reasons why a given action or property might be hidden or disabled.
 * It might be because of the state of the object means that an action’s preconditions are not met
 * (eg toggle and untoggle), or it might be because of security.
 * <p>
 * Sometimes though the reason why an action or property is hidden is a puzzle;
 * you are pretty sure that it should be shown, and yet for some reason it is not.
 * <p>
 * To help diagnose these issues, there are two configuration properties you can set. They only apply if running in prototype mode:
 * <ol>
 * <li>
 * causeway.prototyping.if-hidden-policy
 * If not specified or is set to HIDE, then the behaviour is as per normal.
 * However, if set to SHOW_AS_DISABLED then instead the action or property will be shown,
 * but disabled with the veto providing some explanation as to why. And, if set to SHOW_AS_DISABLED_WITH_DIAGNOSTICS,
 * then the class name of the metamodel facet that vetoed the visibility is also shown in the tooltip.
 * </li><li>
 * causeway.prototyping.if-disabled-policy
 * If not specified or is set to DISABLED, then the behaviour is as per normal.
 * But if set to SHOW_AS_DISABLED_WITH_DIAGNOSTICS,
 * then the class name of the metamodel facet that vetoed the usability is also shown in the tooltip.
 * </li>
 * </ol>
 */
@lombok.Value
public class RenderPolicy implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Always HIDE and DISABLE.
     */
    public static RenderPolicy forActionParameters() {
        return new RenderPolicy(
                CausewayConfiguration.Prototyping.IfHiddenPolicy.HIDE,
                CausewayConfiguration.Prototyping.IfDisabledPolicy.DISABLE);
    }

    /**
     * If not specified or is set to HIDE, then the behaviour is as per normal.
     * However, if set to SHOW_AS_DISABLED then instead the action or property will be shown,
     * but disabled with the veto providing some explanation as to why. And, if set to SHOW_AS_DISABLED_WITH_DIAGNOSTICS,
     * then the class name of the metamodel facet that vetoed the visibility is also shown in the tooltip.
     */
    private final @NonNull CausewayConfiguration.Prototyping.IfHiddenPolicy ifHiddenPolicy;

    /**
     * If not specified or is set to DISABLED, then the behaviour is as per normal.
     * But if set to SHOW_AS_DISABLED_WITH_DIAGNOSTICS, then the class name of the metamodel facet that vetoed
     * the usability is also shown in the tooltip.
     */
    private final @NonNull CausewayConfiguration.Prototyping.IfDisabledPolicy ifDisabledPolicy;

}
