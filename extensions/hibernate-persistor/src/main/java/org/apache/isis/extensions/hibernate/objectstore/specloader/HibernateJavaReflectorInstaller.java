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


package org.apache.isis.extensions.hibernate.objectstore.specloader;

import org.apache.isis.metamodel.config.IsisConfiguration;
import org.apache.isis.metamodel.specloader.classsubstitutor.ClassSubstitutor;
import org.apache.isis.extensions.hibernate.objectstore.persistence.container.HibernateContainer;
import org.apache.isis.extensions.hibernate.objectstore.specloader.classsubstitutor.HibernateClassSubstitutor;
import org.apache.isis.runtime.system.installers.JavaReflectorInstaller;


/**
 * Sets up a {@link HibernateContainer}. 
 */
public class HibernateJavaReflectorInstaller extends JavaReflectorInstaller {

	
	public HibernateJavaReflectorInstaller() {
		super("hibernate");
	}

    @Override
    protected ClassSubstitutor createClassSubstitutor(
    		IsisConfiguration configuration) {
    	return new HibernateClassSubstitutor();
    }
    
}
