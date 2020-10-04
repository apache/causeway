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
package demoapp.webapp.jee;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.springframework.boot.SpringApplication;

import lombok.extern.log4j.Log4j2;

@Singleton @Startup // CDI managed
@Log4j2
public class DemoAppSetupCdi {

    @PostConstruct
    private void init() {
        log.info("about to init ...");
        
        System.out.println("ABOUT TO INIT");
        
        SpringApplication.run(new Class[] { DemoAppJee.class }, new String[] {});
        
        //new SpringApplicationBuilder(DemoAppJee.class).run();
    }
    
}
