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


package org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.accessor;

/**
 * Convert properties to and from a value holder.
 * <p>
 * The naming convention used for classes is &lt;persistent format&gt;&lt;[[NAME]] format&gt;Converter, eg
 * IntegerToWholeNumberConverter converts an Integer from the database to a WholeNumber.
 */
public interface PropertyConverter {

    /**
     * Set the value in the valueHolder, converting from the persistent format.
     */
    public void setValue(final Object valueHolder, final Object value);

    /**
     * Return the value in the valueHolder, converting to the format used to persist.
     */
    public Object getPersistentValue(final Object valueHolder, final boolean isNullable);

    /**
     * Return the java type of persistent objects
     */
    public Class<?> getPersistentType();

    /**
     * Return the Hibernate type of persistent objects
     */
    public String getHibernateType();
}
