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

package org.apache.isis.applib;

import java.util.List;
import java.util.Map;

import org.apache.isis.applib.fixturescripts.FixtureScript;

/**
 * Specifies the "glob", in other words the set of modules (containing domain services and possibly entities), that
 * together make up the running application.
 *
 * <p>
 * To use, implement this interface and then define as the <tt>isis.globspec</tt> key within the
 * <tt>isis.properties</tt> configuration file, or programmatically.
 * </p>
 *
 * <p>
 * The intention is to centralize, so far as possible, the configuration that previously has been duplicated between
 * the <tt>xxx-integtests</tt> (Maven) module and the <tt>xxx-webapp</tt> integtests.
 * </p>
 *
 * <p>
 * There are a number of subsidiary goals:
 * <ul>
 *     <li>
 *         <p>
 *             by having multiple classes implementing this interface, it allows separate "profiles" of the application
 *             to be run.
 *         </p>
 *         <p>For example, a developer could define a globspec that</p>
 *         <ul>
 *             <li>uses the <tt>bypass</tt> security rather than the full-blown Shiro security</li>
 *             <li>excludes some or all addon modules, eg auditing and security</li>
 *             <li>for a large app whose domain has been broken out into multiple <tt>dom-xxx</tt> (Maven) modules, to
 *                 run subsets of the application</li>
 *         </ul>
 *         <p>This applies to both running the main app and also the integration tests.</p>
 *    </li>
 *    <li>
 *        <p>Allow different integration tests to run with different globspecs.  Normally the running application is
 *           shared (on a thread-local) between integration tests.  What the framework could do is to be intelligent
 *           enough to keep track of the globspec used for each integration test and tear down
 *           the shared state if the "next" test uses a different globspec</p>
 *    </li>
 *    <li>
 *        Speed up bootstrapping by only scanning for classes annotated by
 *        {@link org.apache.isis.applib.annotation.DomainService} and {@link javax.jdo.annotations.PersistenceCapable}
 *        once.
 *    </li>
 *    <li>
 *        Provide a programmatic way to contribute elements of `web.xml`.  This is not yet implemented.
 *    </li>
 *    <li>
 *        Provide a programmatic way to configure Shiro security.  This is not yet implemented.
 *    </li>
 *    <li>
 *        <p>Anticipate the module changes forthcoming in Java 9.  Eventually we see that the globspec class will
 *        become an application manifest, while the list of modules will become Java 9 modules each advertising the
 *        types that they export.</p>
 *    </li>
 * </ul>
 * </p>
 * <p>
 *     <b>Note:</b> at this time the integration tests do not keep track of different globspecs; rather the
 *     globspec used for the first test is used for all subsequent tests.
 * </p>
 */
public interface GlobSpec {

    /**
     * A list of classes, each of which representing the root of one of the modules containing services and possibly
     * entities, which together makes up the running application.
     *
     * <p>
     *     The package of each such class is used as the basis for searching for domain services and registered
     *     entities.  As such it replaces and overrides both the
     *     <tt>isis.services.ServicesInstallerFromAnnotation.packagePrefix</tt> key (usually found in the
     *     <tt>isis.properties</tt> file) and the
     *     <tt>isis.persistor.datanucleus.RegisterEntities.packagePrefix</tt> key (usually found in the
     *     <tt>persistor_datanucleus.properties</tt> file).
     * </p>
     *
     * <p>
     *     If a globspec has been provided then the value of <tt>isis.services-installer</tt> configuration property
     *     will be ignored and the <tt>isis.services</tt> configuration property will also be ignored.
     * </p>
     *
     * <p>
     *     Note: the class implementing this interface will typically include itself in the list of classes, so that any
     *     "global" services (for example an application home page) are also picked up.
     * </p>
     */
    public List<Class<?>> getModules();

    /**
     * If non-null, overrides the value of <tt>isis.authentication</tt> configuration property to specify the
     * authentication mechanism.
     *
     * <p>
     *     Ignored for integration tests (which always uses the 'bypass' mechanism).
     * </p>
     */
    public String getAuthenticationMechanism();

    /**
     * If non-null, overrides the value of <tt>isis.authorization</tt> configuration property.
     *
     * <p>
     *     Ignored for integration tests (which always uses the 'bypass' mechanism).
     * </p>
     */
    public String getAuthorizationMechanism();

    /**
     * If non-null, overrides the value of <tt>isis.fixtures</tt> configuration property.
     */
    public List<Class<? extends FixtureScript>> getFixtures();

    /**
     * Overrides for any other configuration properties.
     */
    public Map<String,String> getConfigurationProperties();

}
