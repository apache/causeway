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


package org.apache.isis.extensions.hibernate.objectstore;

import org.apache.isis.metamodel.config.ConfigurationConstants;

public final class HibernateConstants {
    
    private HibernateConstants() {}

    public static final String PROPERTY_PREFIX = ConfigurationConstants.ROOT + "persistence.hibernate.";
    
    public static final String PERSIST_ALGORITHM_KEY = PROPERTY_PREFIX + "persistAlgorithm";
    
    public static final String SAVE_IMMEDIATE_KEY = PROPERTY_PREFIX + "saveImmediate";
    public static final String REMAPPING_KEY = PROPERTY_PREFIX + "remapping";
    
    public static final String HIB_SCHEMA_UPDATE_KEY = PROPERTY_PREFIX + "schema-update";
    public static final String HIB_SCHEMA_EXPORT_KEY = PROPERTY_PREFIX + "schema-export";
    
    public static final String HIB_REGENERATE_KEY = PROPERTY_PREFIX + "regenerate";
    public static final String HIB_AUTO_KEY = PROPERTY_PREFIX + "auto";
    public static final String HIB_ANNOTATIONS_KEY = PROPERTY_PREFIX + "annotations";
    public static final String HBM_EXPORT_KEY = PROPERTY_PREFIX + "hbm-export";
    public static final String HIB_INITIALIZED_KEY = PROPERTY_PREFIX + "initialized";

}


