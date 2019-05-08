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

package org.apache.isis.applib.fixtures;

/**
 * Enumerates the different types of {@link InstallableFixture fixture}s
 * supported.
 *
 * @see InstallableFixture#getType()
 */
public enum FixtureType {
    /**
     * A fixture that installs data (either reference data or operational data)
     * into an object store.
     *
     * <p>
     * Some object stores are in-memory only, in which case these will always
     * want fixtures of this type to be installed. However, for object stores
     * that persist the data (such as XML or to an RDBMS), these typically do
     * <i>not</i> want data fixtures run (except possibly for the very first
     * time booted to initially seed them).
     */
    DOMAIN_OBJECTS,
    /**
     * A fixture that does not install data into the object store.
     *
     * <p>
     * Fixtures of this type are always installed. Typical examples are:
     * <ul>
     * <li>composite fixtures that just aggregate other fixtures
     * <li>fixtures that set up the date/time (see {@link DateFixture})
     * <li>fixtures that specify the user to logon as (see {@link LogonFixture}
     * ).
     * </ul>
     */
    OTHER
    
    ;
	
	public boolean isAlwaysInstall() {
		return this == OTHER;
	}
	
	
}