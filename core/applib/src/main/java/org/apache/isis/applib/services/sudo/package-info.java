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

/**
 * The {@link org.apache.isis.applib.services.sudo.SudoService} allows the current user reported by the
 * {@link org.apache.isis.applib.services.user.UserService} to be temporarily changed to some other user. This is
 * useful both for integration testing (eg if testing a workflow system whereby objects are moved from one user to
 * another) and while running fixture scripts (eg setting up objects that would normally require several users to have
 * acted upon the objects).
 *
 * @see <a href="http://isis.apache.org/guides/rgsvc/rgsvc.html#_rgsvc_testing_SudoService">Reference guide</a>
 */
package org.apache.isis.applib.services.sudo;