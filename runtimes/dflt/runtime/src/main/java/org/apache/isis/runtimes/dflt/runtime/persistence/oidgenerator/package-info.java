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
 * This interface is used by the {@link org.apache.isis.runtimes.dflt.runtime.persistence.PersistenceSession} and
 * is generally not intended to be implemented directly.  
 * 
 * <p>
 * Used by object store implementations to specify how to manufacture
 * {@link org.apache.isis.core.metamodel.adapter.oid.Oid}s (permanent unique identifiers
 * for each domain object managed by Apache Isis).  For example, an
 * in-memory object store will just use a unique Id, whereas a generator
 * for Hibernate will hook into Hibernate's own identity generators.
 * 
 * <p>
 * Since there is a close dependency between the {@link org.apache.isis.runtimes.dflt.runtime.persistence.PersistenceSession}
 * and the {@link ClassSubstitutor} implementation, it is the job of the {@link org.apache.isis.runtimes.dflt.runtime.persistence.PersistenceMechanismInstaller} to
 * ensure that the correct {@link OidGenerator} is setup.
 * 
 * @see org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.algorithm.PersistAlgorithm
 * @see org.apache.isis.metamodel.specloader.classsubstitutor.classsubstitor.ClassSubstitutor.ClassStrategy
 */
package org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator;