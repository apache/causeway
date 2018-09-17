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

package org.apache.isis.core.runtime.system;

import java.util.List;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;

/**
 * Whether running on client or server side etc.
 */
public class DeploymentType {

    private static List<DeploymentType> deploymentTypes = _Lists.newArrayList();

    public static DeploymentType SERVER = new DeploymentType("SERVER", DeploymentCategory.PRODUCTION);
    public static DeploymentType SERVER_EXPLORATION = new DeploymentType("SERVER_EXPLORATION", DeploymentCategory.EXPLORING);
    public static DeploymentType SERVER_PROTOTYPE = new DeploymentType("SERVER_PROTOTYPE", DeploymentCategory.PROTOTYPING);
    public static DeploymentType UNIT_TESTING = new DeploymentType("UNIT_TESTING", DeploymentCategory.PRODUCTION);

    /**
     * Look up {@link DeploymentType} by their {@link #name()}.
     *
     * <p>
     * Can substitute <tt>'-'</tt> instead of <tt>'_'</tt>; for example
     * <tt>server_exploration</tt> will lookup the same as
     * <tt>server-exploration</tt>.
     */
    public static DeploymentType lookup(final String str) {
        final String underscoredStr = str.replace('-', '_').toUpperCase();
        for (final DeploymentType deploymentType : deploymentTypes) {
            if (underscoredStr.equals(deploymentType.name())) {
                return deploymentType;
            }
        }
        throw new IllegalArgumentException(String.format("Unknown deployment type '%s'", str));
    }


    private final String name;
    private final DeploymentCategory deploymentCategory;

    public DeploymentType(
            final String name, final DeploymentCategory category) {
        this.deploymentCategory = category;
        this.name = name;
        deploymentTypes.add(this);
    }

    public DeploymentCategory getDeploymentCategory() {
        return deploymentCategory;
    }

    public boolean isExploring() {
        return deploymentCategory.isExploring();
    }

    public boolean isPrototyping() {
        return deploymentCategory.isPrototyping();
    }

    public boolean isProduction() {
        return deploymentCategory.isProduction();
    }

    public String friendlyName() {
        return nameLowerCase().replace('_', '-');
    }

    public String nameLowerCase() {
        return name().toLowerCase();
    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return name();
    }

}