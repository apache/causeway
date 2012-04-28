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
 * Interaction events, corresponding to gestures in the user interface.
 * 
 * <p>
 * The applib does not provide any means of listening to these events directly, and typically
 * domain objects would not be interested in them either.  However, they can be subscribed to
 * using the wrapper progmodel (which effectively provides a drop-in replacement for the
 * {@link org.apache.isis.applib.DomainObjectContainer} that implements the <tt>WrapperFactory</tt>
 * interface).  
 */
package org.apache.isis.applib.events;