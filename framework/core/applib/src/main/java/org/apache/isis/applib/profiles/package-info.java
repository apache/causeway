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
 * Defines interfaces for domain objects that constitute the framework's
 * view of a &quot;user profile&quot;. 
 * 
 * <p>
 * Each user can hold a single {@link org.apache.isis.applib.profiles.Profile},
 * which in turn can hold option settings (eg preferred colour theme) and 
 * {@link org.apache.isis.applib.profiles.Perspective}s (a particular 
 * arrangement of the user interface; the terminology comes from the Eclipse
 * RCP/IDE platform).
 *  
 * <p>
 * The use and surfacing of these capabilities is dependent on the viewer;
 * most notably the drag-n-drop viewer does support the concept of a user
 * profile, but many others do not. 
 */
package org.apache.isis.applib.profiles;