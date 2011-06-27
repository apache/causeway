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
 * (Apache Isis) Adapter Factory API.
 * 
 * <p>
 * In most scenarios the default implementation is expected to suffice.  However,
 * the API was introduced to allow custom viewers to install more sophisticated
 * adapters, specifically to support observer (MVC) patterns in the UI.
 */
package org.apache.isis.runtimes.dflt.runtime.persistence.adapterfactory;