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

package org.apache.isis.core.metamodel.facets.propcoll.notpersisted;

import org.apache.isis.core.metamodel.facetapi.Facet;

/**
 * Indicates that a property or a collection shouldn't be persisted.
 *
 * <p>
 * In the standard Apache Isis Programming Model, corresponds to annotating the
 * property or collection with the <tt>@NotPersisted</tt> annotation.
 *
 * <p>
 * Note that being non-persisted does not imply being disabled; see for example:
 *
 * @see http://mail-archives.apache.org/mod_mbox/incubator-isis-dev/201010.mbox/%3C4CB2FA43.7030206@nakedobjects.org%3E
 */
public interface NotPersistedFacet extends Facet {

}
