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

package org.apache.isis.legacy.applib.filter;

/**
 * For use by repository implementations to allow a set of objects returned by a
 * back-end objectstore to be filtered before being returned to the caller.
 * 
 * <p>
 * Note that this is different from the pattern or criteria object accepted by
 * some repositories' <tt>findXxx</tt> methods. Such criteria objects are
 * implementation-specific to the configured objectstore and allow it to return
 * an already-filtered set of rows. (For example, a Hibernate-based ObjectStore
 * would accept a representation of a HQL query; an XML-based objectstore might
 * accept an XPath query, etc.)
 * 
 * @deprecated - use java's {@link Predicate} instead.
 */
@Deprecated
public interface Filter<T> {

    /**
     * Whether or not the supplied pojo meets this criteria.
     * 
     * @param pojo
     * @return <tt>true</tt> if this pojo is acceptable, <tt>false</tt>
     *         otherwise.
     */
    public boolean accept(T t);

}
