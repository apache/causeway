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

package org.apache.isis.core.metamodel.spec.feature;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;

/**
 * Provides reflective access to a field on a domain object.
 */
public interface ObjectAssociation extends ObjectMember, CurrentHolder {

    /**
     * Get the name for the business key, if one has been specified.
     */
    String getBusinessKeyName();

    /**
     * Return the default for this property.
     */
    ObjectAdapter getDefault(ObjectAdapter adapter);

    /**
     * Set the property to it default references/values.
     */
    public void toDefault(ObjectAdapter target);

    /**
     * Whether there are any choices provided (eg <tt>choicesXxx</tt> supporting
     * method) for the association.
     */
    public boolean hasChoices();
    /**
     * Returns a list of possible references/values for this field, which the
     * user can choose from.
     */
    public ObjectAdapter[] getChoices(ObjectAdapter object);


    /**
     * Whether there are any auto-complete provided (eg <tt>autoCompleteXxx</tt> supporting
     * method) for the association.
     */
    public boolean hasAutoComplete();
    /**
     * Returns a list of possible references/values for this field, which the
     * user can choose from, based on the provided search argument.
     */
    public ObjectAdapter[] getAutoComplete(ObjectAdapter object, String searchArg);

    int getAutoCompleteMinLength();

    /**
     * Returns true if calculated from other data in the object, that is, should
     * not be persisted.
     */
    boolean isNotPersisted();

    /**
     * Returns <code>true</code> if this field on the specified object is deemed
     * to be empty, or has no content.
     */
    boolean isEmpty(ObjectAdapter target);

    /**
     * Determines if this field must be complete before the object is in a valid
     * state
     */
    boolean isMandatory();


}
