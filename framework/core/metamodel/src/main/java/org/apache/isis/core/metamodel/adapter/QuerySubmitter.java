/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.metamodel.adapter;

import java.util.List;

import org.apache.isis.applib.query.Query;
import org.apache.isis.core.commons.components.Injectable;

public interface QuerySubmitter extends Injectable {

    /**
     * Provided by <tt>PersistenceSession</tt> when used by framework.
     * 
     * <p>
     * Called by <tt>DomainObjectContainerDefault</tt> and also by the choices
     * facets.
     */
    <T> List<ObjectAdapter> allMatchingQuery(Query<T> query);

    /**
     * Provided by <tt>PersistenceSession</tt> when used by framework.
     * 
     * <p>
     * Called by <tt>DomainObjectContainerDefault</tt>.
     */
    public <T> ObjectAdapter firstMatchingQuery(Query<T> query);

}
