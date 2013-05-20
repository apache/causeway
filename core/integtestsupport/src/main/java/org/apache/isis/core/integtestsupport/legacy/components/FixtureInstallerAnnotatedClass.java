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

package org.apache.isis.core.integtestsupport.legacy.components;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.core.integtestsupport.legacy.Fixture;
import org.apache.isis.core.integtestsupport.legacy.Fixtures;
import org.apache.isis.core.runtime.fixtures.FixturesInstallerAbstract;
import org.apache.isis.core.runtime.fixtures.FixturesInstallerDelegate;

public class FixtureInstallerAnnotatedClass extends FixturesInstallerAbstract {

    private final List<Object> fixtures = new ArrayList<Object>();

    /**
     * @see #addFixturesAnnotatedOn(Class)
     */
    public FixtureInstallerAnnotatedClass() {
        super("annotated");
    }

    // ///////////////////////////////////////////
    // Hook method
    // ///////////////////////////////////////////

    /**
     * Just copies the fixtures added using
     * {@link #addFixturesAnnotatedOn(Class)} into the delegate.
     */
    @Override
    protected void addFixturesTo(final FixturesInstallerDelegate delegate) {
        for (final Object fixture : fixtures) {
            delegate.addFixture(fixture);
        }
    }

    // ///////////////////////////////////////////
    // addFixturesAnnotatedOn (not API)
    // ///////////////////////////////////////////

    /**
     * Should be called prior to installing; typically called immediately after
     * instantiation.
     * 
     * <p>
     * Note: an alternative design would be to have a 1-arg constructor, but the
     * convention for installers is to make them no-arg.
     */
    public void addFixturesAnnotatedOn(final Class<?> javaClass) throws InstantiationException, IllegalAccessException {
        final Fixtures fixturesAnnotation = javaClass.getAnnotation(Fixtures.class);
        if (fixturesAnnotation != null) {
            final Fixture[] fixtureAnnotations = fixturesAnnotation.value();
            for (final Fixture fixtureAnnotation : fixtureAnnotations) {
                addFixtureRepresentedBy(fixtureAnnotation, fixtures);
            }
        }

        final Fixture fixtureAnnotation = javaClass.getAnnotation(Fixture.class);
        if (fixtureAnnotation != null) {
            addFixtureRepresentedBy(fixtureAnnotation, fixtures);
        }
    }

    private void addFixtureRepresentedBy(final Fixture fixtureAnnotation, final List<Object> fixtures) throws InstantiationException, IllegalAccessException {
        final Class<?> fixtureClass = fixtureAnnotation.value();
        fixtures.add(fixtureClass.newInstance());
    }

}
