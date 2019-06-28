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
 * Provides an infrastructure for encoding {@link org.apache.isis.commons.internal.encoding.Encodable}
 * into an {@link org.apache.isis.commons.internal.encoding.DataOutputExtended output stream}
 * or from an {@link org.apache.isis.commons.internal.encoding.DataInputExtended input stream}.
 *
 * <p>
 * This is primarily for remoting (marshalling objects across the wire) but
 * is also used in various other places, including the creation of
 * mementos (to capture state at a point in time).
 */
package org.apache.isis.commons.internal.assertions;