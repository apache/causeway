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
 * Authentication API.
 * 
 * <p>
 * Concrete implementations are in the <tt>authentication-xxx</tt> modules.
 * <ul>
 * <li>In client/server mode (using <tt>remoting-command</tt> distribution), 
 *     just use the <tt>authentication-proxy</tt> on client side, and 
 *     specify desired authentication implementation on server.
 *     </li>
 * <li>In standalone mode, ignore the proxy authentication use the required 
 *     authentication implementation directly.</li>
 * </ul>
 */
package org.apache.isis.runtime.authentication;