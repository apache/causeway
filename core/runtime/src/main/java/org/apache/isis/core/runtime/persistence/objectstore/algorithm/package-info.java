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
 * This interface is used by the {@link org.apache.isis.core.runtime.system.persistence.PersistenceSession} and
 * is generally not intended to be implemented directly.  
 * 
 * <p>
 * The {@link PersistAlgorithm} defines how persistence-by-reachability is enacted.  This only
 * applies to the <tt>ObjectStorePersistor</tt> implementation, but has been brought up into
 * <tt>architecture</tt> module because it is very much a peer of the other helper objects
 * that influence the {@link org.apache.isis.core.runtime.system.persistence.PersistenceSession}'s behaviour, such
 * as {@link ClassSubstitutor} and {@link org.apache.isis.core.runtime.system.persistence.OidGenerator}. 
 * 
 * <p>
 * Since there is a close dependency between the {@link org.apache.isis.core.runtime.system.persistence.PersistenceSession}
 * and the {@link PersistAlgorithm} implementation, it is the job of the {@link org.apache.isis.core.runtime.installerregistry.installerapi.PersistenceMechanismInstaller} to
 * ensure that the correct {@link PersistAlgorithm} is setup.
 * 
 * @see org.apache.isis.metamodel.specloader.classsubstitutor.classsubstitor.ClassSubstitutor.ClassStrategy
 * @see org.apache.isis.core.runtime.system.persistence.OidGenerator
 */
package org.apache.isis.core.runtime.persistence.objectstore.algorithm;