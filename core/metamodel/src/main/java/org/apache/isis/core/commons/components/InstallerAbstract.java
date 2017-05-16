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

package org.apache.isis.core.commons.components;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import org.apache.isis.core.commons.config.IsisConfiguration;

public abstract class InstallerAbstract implements Installer {

    //region > constructor, fields

    private final String name;
    private final IsisConfiguration isisConfiguration;

    /**
     * Subclasses should pass in the type defined as a constant in the
     * subinterface of Installer.
     * 
     * <p>
     * For example, <tt>PersistenceMechanismInstaller</tt> has a constant
     * <tt>PersistenceMechanismInstaller#TYPE</tt>. Any implementation of
     * <tt>PersistenceMechanismInstaller</tt> should pass this constant value up
     * to this constructor.
     */
    public InstallerAbstract(
            final String name,
            final IsisConfiguration isisConfiguration) {
        this.name = name;
        this.isisConfiguration = isisConfiguration;
    }


    @Override
    public String getName() {
        return name;
    }

    public IsisConfiguration getConfiguration() {
        return isisConfiguration;
    }
    //endregion

    //region > init, shutdown

    /**
     * Default implementation does nothing.
     */
    public void init() {
        // no-op implementation, subclasses may override!
    }

    /**
     * Default implementation does nothing.
     */
    public void shutdown() {
        // no-op implementation, subclasses may override!
    }

    //endregion

    //region > helpers (for subclasses)

    /**
     * Helper for subclasses implementing {@link #getTypes()}.
     */
    protected static List<Class<?>> listOf(final Class<?>... classes) {
        return Collections.unmodifiableList(Lists.<Class<?>> newArrayList(classes));
    }

    /**
     * Helper for subclasses implementing {@link #getTypes()}.
     */
    protected static List<Class<?>> listOf(final List<Class<?>> classList, final Class<?>... classes) {
        final List<Class<?>> arrayList = Lists.<Class<?>> newArrayList(classes);
        arrayList.addAll(0, classList);
        return Collections.unmodifiableList(arrayList);
    }

    //endregion

}
