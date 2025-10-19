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
package org.apache.causeway.core.metamodel.services.classsubstitutor;

import jakarta.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.graph.tree.TreeAdapter;
import org.apache.causeway.core.metamodel.CausewayModuleCoreMetamodel;

@Component
@Named(CausewayModuleCoreMetamodel.NAMESPACE + ".ClassSubstitutorDefault")
@jakarta.annotation.Priority(PriorityPrecedence.MIDPOINT)
public class ClassSubstitutorDefault extends ClassSubstitutorAbstract {

    public ClassSubstitutorDefault() {
        ignoreCglib();
        ignoreJavassist();
        ignoreApacheCausewayInternals();
        ignoreSpringFramework();
        ignoreJacksonAndGson();
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

    private void ignoreApacheCausewayInternals() {
        ignoreClass("org.apache.causeway.commons.internal.ioc.spring.BeanAdapterSpring");
        ignoreClass(TreeAdapter.class.getName());
    }

    protected void ignoreJacksonAndGson() {
        ignorePackage("com.fasterxml.jackson.");
        ignorePackage("com.google.gson.");
    }

}
