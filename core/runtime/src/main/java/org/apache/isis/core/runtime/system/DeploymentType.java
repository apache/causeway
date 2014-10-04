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

import com.google.common.collect.Lists;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.deployment.DeploymentCategoryProvider;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;

/**
 * Whether running on client or server side etc.
 * 
 * <p>
 * Previously this was an <tt>enum</tt>, but it is now a regular class. The
 * change has been made to provide more flexibility in setting up the
 * <tt>IsisContext</tt> lookup.
 * 
 * <p>
 * To use this capability:
 * <ul>
 * <li>Write your new implementation of <tt>IsisContext</tt>, along with a
 * static factory method (cf
 * <tt>IsisContextStatic#createInstance(IsisSessionFactory)</tt>)
 * <li>Create a new subclass of {@link ContextCategory} (also now a regular
 * class rather than an <tt>enum</tt>); this is where your code goes to
 * instantiate your <tt>IsisContext</tt> implementation</li>
 * <li>Create a new subclass of {@link DeploymentType}, passing in the custom
 * {@link ContextCategory} in its constructor</li>
 * <li>In your bootstrap code, instantiate your new {@link DeploymentType}
 * subclass</li>
 * <li>When you run your app, don't forget to specify your custom
 * {@link DeploymentType}, eg using the <tt>--type</tt> command line arg</li>
 * </ul>
 */
public class DeploymentType implements DeploymentCategoryProvider {

    private static List<DeploymentType> deploymentTypes = Lists.newArrayList();

    public static DeploymentType SERVER = new DeploymentType("SERVER", DeploymentCategory.PRODUCTION, ContextCategory.THREADLOCAL);
    public static DeploymentType SERVER_EXPLORATION = new DeploymentType("SERVER_EXPLORATION", DeploymentCategory.EXPLORING, ContextCategory.THREADLOCAL);
    public static DeploymentType SERVER_PROTOTYPE = new DeploymentType("SERVER_PROTOTYPE", DeploymentCategory.PROTOTYPING, ContextCategory.THREADLOCAL);
    public static DeploymentType UNIT_TESTING = new DeploymentType("UNIT_TESTING", DeploymentCategory.PRODUCTION, ContextCategory.STATIC_RELAXED);
    public static DeploymentType UTILITY = new DeploymentType("UTILITY", DeploymentCategory.EXPLORING, ContextCategory.STATIC);

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
    private final ContextCategory contextCategory;

    public DeploymentType(
            final String name, final DeploymentCategory category, final ContextCategory contextCategory) {
        this.deploymentCategory = category;
        this.contextCategory = contextCategory;
        this.name = name;
        deploymentTypes.add(this);
    }

    public DebuggableWithTitle getDebug() {
        return new DebuggableWithTitle() {

            @Override
            public void debugData(final DebugBuilder debug) {
                debug.appendln("Category", deploymentCategory);
                debug.appendln("Context", contextCategory);
                debug.appendln();
                debug.appendln("Name", friendlyName());
                debug.appendln("Should monitor", shouldMonitor());
            }

            @Override
            public String debugTitle() {
                return "Deployment type";
            }
        };
    }

    public void initContext(final IsisSessionFactory sessionFactory) {
        contextCategory.initContext(sessionFactory);
    }

    public boolean shouldMonitor() {
        return (this == SERVER) && isProduction();
    }

    @Override
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