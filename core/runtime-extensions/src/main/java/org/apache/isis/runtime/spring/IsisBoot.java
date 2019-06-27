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
package org.apache.isis.runtime.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import org.apache.isis.applib.IsisApplibModule;
import org.apache.isis.config.beans.IsisBeanScanInterceptorForSpring;
import org.apache.isis.metamodel.IsisMetamodelModule;
import org.apache.isis.runtime.IsisRuntimeModule;
import org.apache.isis.runtime.services.IsisRuntimeServicesModule;
import org.apache.isis.wrapper.IsisWrapperModule;

@Configuration 
@ComponentScan(
		basePackageClasses= {
				IsisApplibModule.class,
				IsisMetamodelModule.class,
				IsisRuntimeModule.class,
				IsisRuntimeServicesModule.class,
				IsisWrapperModule.class},
		includeFilters= {
				@Filter(type = FilterType.CUSTOM, classes= {IsisBeanScanInterceptorForSpring.class})
		})
public class IsisBoot implements ApplicationContextAware {

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // just make sure we wait for the context
        // (its passed over to ServiceRegistryDefault)
    }

	
}
