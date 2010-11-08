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

import org.apache.isis.core.metamodel.spec.ObjectSpecification;


/**
 * Enumerates the features that a particular annotation can be applied to.
 * 
 * <p>
 * Modelled after Java 5 <tt>ElementType</tt>.
 * 
 * 
 * <p>
 * TODO: should rationalize this and {@link ObjectSpecification#getResultType()}. Note though that we don't
 * distinguish value properties and reference properties (and we probably shouldn't in
 * {@link ObjectSpecification}, either).
 */
public final class ObjectFeatureType {

    public final static ObjectFeatureType OBJECT = new ObjectFeatureType(0, "Object");
    public final static ObjectFeatureType PROPERTY = new ObjectFeatureType(1, "Property");
    public final static ObjectFeatureType COLLECTION = new ObjectFeatureType(2, "Collection");
    public final static ObjectFeatureType ACTION = new ObjectFeatureType(3, "Action");
    public final static ObjectFeatureType ACTION_PARAMETER = new ObjectFeatureType(4, "Parameter");

    public final static ObjectFeatureType[] COLLECTIONS_ONLY = new ObjectFeatureType[] { COLLECTION };
    public final static ObjectFeatureType[] ACTIONS_ONLY = new ObjectFeatureType[] { ACTION };
    public final static ObjectFeatureType[] PARAMETERS_ONLY = new ObjectFeatureType[] { ACTION_PARAMETER };
    public final static ObjectFeatureType[] ACTIONS_AND_PARAMETERS = new ObjectFeatureType[] { ACTION, ACTION_PARAMETER };
    public final static ObjectFeatureType[] COLLECTIONS_AND_ACTIONS = new ObjectFeatureType[] { COLLECTION, ACTION };
    public final static ObjectFeatureType[] PROPERTIES_AND_PARAMETERS = new ObjectFeatureType[] { PROPERTY,
            ACTION_PARAMETER };
    public final static ObjectFeatureType[] OBJECTS_PROPERTIES_AND_PARAMETERS = new ObjectFeatureType[] { OBJECT,
            PROPERTY, ACTION_PARAMETER };
    public final static ObjectFeatureType[] OBJECTS_AND_PROPERTIES = new ObjectFeatureType[] { OBJECT, PROPERTY };
    public final static ObjectFeatureType[] PROPERTIES_ONLY = new ObjectFeatureType[] { PROPERTY };
    public final static ObjectFeatureType[] OBJECTS_ONLY = new ObjectFeatureType[] { OBJECT };
    public final static ObjectFeatureType[] OBJECTS_PROPERTIES_AND_COLLECTIONS = new ObjectFeatureType[] { OBJECT,
            PROPERTY, COLLECTION };
    public final static ObjectFeatureType[] PROPERTIES_AND_COLLECTIONS = new ObjectFeatureType[] { PROPERTY, COLLECTION };
    public final static ObjectFeatureType[] PROPERTIES_COLLECTIONS_AND_ACTIONS = new ObjectFeatureType[] { PROPERTY,
            COLLECTION, ACTION };
    public final static ObjectFeatureType[] EVERYTHING_BUT_PARAMETERS = new ObjectFeatureType[] { OBJECT, PROPERTY,
            COLLECTION, ACTION };
    public final static ObjectFeatureType[] EVERYTHING = new ObjectFeatureType[] { OBJECT, PROPERTY, COLLECTION,
            ACTION, ACTION_PARAMETER };

    private final int num;
    private final String name;

    private ObjectFeatureType(final int num, final String nameInCode) {
        this.num = num;
        this.name = nameInCode;
    }

    public int getNum() {
        return num;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

}
