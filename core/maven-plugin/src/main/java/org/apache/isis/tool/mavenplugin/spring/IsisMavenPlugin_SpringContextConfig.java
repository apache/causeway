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
package org.apache.isis.tool.mavenplugin.spring;

import javax.inject.Singleton;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.config.beans.WebAppConfigBean;
import org.apache.isis.core.security.IsisSecurityBoot;
import org.apache.isis.runtime.spring.IsisBoot;

/**
 * FIXME[2112] needs to scan the entire class-path, not just "org.apache.isis"  
 * @since 2.0.0
 *
 */
@Configuration
@Import({
    IsisBoot.class, 
    IsisSecurityBoot.class})
@ComponentScan(basePackages = "org.apache.isis")
public class IsisMavenPlugin_SpringContextConfig {

    @Bean @Singleton
    public WebAppConfigBean webAppConfigBean() {
        // just empty        
        return WebAppConfigBean.builder()
                //.menubarsLayoutXml(new ClassPathResource("menubars.layout.xml", this.getClass()))
                .build();
    }
    
}
