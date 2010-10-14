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


package org.apache.isis.metamodel.adapter.oid.stringable.directly;

import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.encoding.Encodable;

/**
 * An alternative to {@link Encodable}, intended to be used for <tt>Oid</tt>s
 * that can be encoded/decoded from strings.
 * 
 * <p>
 * This is inspired by the DSFA's implementation that uses <tt>CUS|1234567A</tt> as the
 * string representation of their <tt>OStoreOid</tt>, representing a Customer.
 */
public interface DirectlyStringableOid extends Oid {

	String enString();

}
