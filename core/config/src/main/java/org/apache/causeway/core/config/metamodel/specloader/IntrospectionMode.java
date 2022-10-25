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
package org.apache.causeway.core.config.metamodel.specloader;

import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.core.config.environment.DeploymentType;

import lombok.val;

public enum IntrospectionMode {

    /**
     * Lazy (don't introspect members for most classes unless required),
     * irrespective of the deployment mode.
     */
    LAZY {
        @Override
        protected boolean isFullIntrospect(final DeploymentType deploymentType) {
            return false;
        }
    },

    /**
     * If production deployment mode, then full, otherwise lazy.
     */
    LAZY_UNLESS_PRODUCTION {
        @Override
        protected boolean isFullIntrospect(final DeploymentType deploymentType) {
            return deploymentType.isProduction();
        }
    },

    /**
     * Full introspection, irrespective of deployment mode.
     */
    FULL {
        @Override
        protected boolean isFullIntrospect(final DeploymentType deploymentType) {
            return true;
        }
    };

    protected abstract boolean isFullIntrospect(final DeploymentType deploymentType);

    public static boolean isFullIntrospect(CausewayConfiguration configuration, CausewaySystemEnvironment causewaySystemEnvironment) {
        val introspectionMode = configuration.getCore().getMetaModel().getIntrospector().getMode();
        return introspectionMode.isFullIntrospect(causewaySystemEnvironment.getDeploymentType());
    }

}
