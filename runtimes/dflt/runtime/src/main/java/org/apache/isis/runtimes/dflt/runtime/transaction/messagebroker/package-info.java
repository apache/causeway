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
 * Message Broker API.
 * 
 * <p>
 * Used to collate messages to send back to the client.
 * 
 * <p>
 * Not generally intended to be implemented; the default implementation in
 * <tt>nof-core</tt> should normally suffice.  However, provides for the
 * opportunity for more exotic remoting mechanisms to send out warning messages,
 * for example JMS.
 * 
 * @see org.apache.isis.runtimes.dflt.runtime.system.transaction.UpdateNotifier
 */
package org.apache.isis.runtimes.dflt.runtime.transaction.messagebroker;