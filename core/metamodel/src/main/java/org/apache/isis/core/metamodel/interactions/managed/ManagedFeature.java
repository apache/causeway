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
package org.apache.isis.core.metamodel.interactions.managed;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public interface ManagedFeature {

    Identifier getIdentifier();
    
    /**
     * @return The feature's display name, as rendered with the UI. 
     */
    String getDisplayLabel();
    
    /**
     * @return The specification of the feature's underlying type. 
     * For actions this is the specification of the action's return type. 
     */
    ObjectSpecification getSpecification();
    
    /**
     * @return The feature's underlying type. 
     * For actions this is the action's return type. 
     */
    default Class<?> getCorrespondingClass() {
        return getSpecification().getCorrespondingClass();    
    }

    
}
