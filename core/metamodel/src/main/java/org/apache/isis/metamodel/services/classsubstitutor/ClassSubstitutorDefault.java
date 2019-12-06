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

package org.apache.isis.metamodel.services.classsubstitutor;

import lombok.extern.log4j.Log4j2;

import javax.inject.Named;

import org.springframework.stereotype.Component;

@Component
@Named("isisMetaModel.ClassSubstitutorDefault")
@Log4j2
public class ClassSubstitutorDefault extends ClassSubstitutorAbstract {

    public ClassSubstitutorDefault() {

        ignoreCglib();
        ignoreJavassist();
        ignoreApacheIsisInternals();
        ignoreSpringFramework();
        ignoreJacksonAndGson();
        skipDataNucleusProxy();

    }

    protected void ignoreCglib() {
        ignoreClass("net.sf.cglib.proxy.Factory");
        ignoreClass("net.sf.cglib.proxy.MethodProxy");
        ignoreClass("net.sf.cglib.proxy.Callback");
    }

    protected void ignoreJavassist() {
        ignoreClass("javassist.util.proxy.ProxyObject");
        ignoreClass("javassist.util.proxy.MethodHandler");
    }

    protected void ignoreSpringFramework() {
        ignoreClass("org.springframework.aop.framework.autoproxy.InfrastructureAdvisorAutoProxyCreator");
        ignorePackage("org.springframework.");
    }

    private void ignoreApacheIsisInternals() {
        // can't ignoring this class ... will result in NPEs...
        // ignoreClass("org.apache.isis.commons.internal.ioc.spring.BeanAdapterSpring");
    }

    protected void ignoreJacksonAndGson() {
        ignorePackage("com.fasterxml.jackson.");
        ignorePackage("com.google.gson.");
    }

    protected void skipDataNucleusProxy() {
        skipProxyPackage("org.datanucleus.");
    }


}
