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
package org.apache.isis.applib.services.sessmgmt;

/**
 * Intended primarily for {@link org.apache.isis.applib.fixturescripts.FixtureScript}s that define long-running/large
 * jobs, eg as used for migration; the intention is to allow that work to be broken into separate batches, each in
 * their own session, but all within a single request.
 *
 * <p>
 * 	   
 *     <br>
 *     Care must be taken not to use any objects from one session to the next.  In other words, the service does
 *     <i>not</i> detach any persistent objects in one session and re-attach them in the next.  Also, any objects
 *     created in previous sessions cannot be exposed in the UI.  In practical terms this means that
 *     {@link org.apache.isis.applib.fixturescripts.FixtureScript.ExecutionContext#addResult(FixtureScript, Object)}
 *     must not be called.
 * </p>
 *
 * @see org.apache.isis.applib.services.xactn.TransactionService
 */
// tag::refguide[]
public interface SessionManagementService {

    void nextSession();

}
// end::refguide[]
