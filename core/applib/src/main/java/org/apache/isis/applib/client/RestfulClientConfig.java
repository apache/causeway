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
package org.apache.isis.applib.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * @since 2.0.0-M2
 */
@XmlRootElement(name="restful-client-config")
@XmlAccessorType(XmlAccessType.FIELD)
public class RestfulClientConfig {

    // --
    
    @XmlElement(name="restfulBase") 
    private String restfulBase;
    
    public String getRestfulBase() {
        return restfulBase;
    }
    
    public void setRestfulBase(String restfulBase) {
        this.restfulBase = restfulBase;
    }
    
    // --
    
    @XmlElement(name="useBasicAuth") 
    private boolean useBasicAuth;
    
    public boolean isUseBasicAuth() {
        return useBasicAuth;
    }

    public void setUseBasicAuth(boolean useBasicAuth) {
        this.useBasicAuth = useBasicAuth;
    }
    
    // --

    @XmlElement(name="restfulAuthUser")
    private String restfulAuthUser;
    
    public String getRestfulAuthUser() {
        return restfulAuthUser;
    }
    
    public void setRestfulAuthUser(String restfulAuthUser) {
        this.restfulAuthUser = restfulAuthUser;
    }
    
    // --

    @XmlElement(name="restfulAuthPassword")
    private String restfulAuthPassword;
    
    public String getRestfulAuthPassword() {
        return restfulAuthPassword;
    }
    
    public void setRestfulAuthPassword(String restfulAuthPassword) {
        this.restfulAuthPassword = restfulAuthPassword;
    }
    
    
}
