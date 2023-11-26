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
package org.apache.causeway.client.kroviz.snapshots.knife

import org.apache.causeway.client.kroviz.snapshots.Response

object HTTP_ERROR_500 : Response() {
    val invoke = "\$invoke"
    val invokeOnTarget = "\$invokeOnTarget"
    val preprocess = "\$preprocess"
    val doFilter = "\$doFilter"
    val ConnectionHandler = "\$ConnectionHandler"
    val SocketProcessor = "\$SocketProcessor"
    val Worker = "\$Worker"
    val WrappingRunnable = "\$WrappingRunnable"
    val Simple = "\$Simple"
    val DomainEventMemberExecutor = "\$DomainEventMemberExecutor"
    val invokeMethodOn = "\$invokeMethodOn"
    val toCallable = "\$toCallable"

    override val url = ""
    override val str = """{
  "httpStatusCode": 500,
  "message": null,
  "detail": {
    "className": "java.lang.NullPointerException",
    "message": null,
    "element": [
      "com.kn.ife.cfg.vm.ReleaseComparisons.create(ReleaseComparisons.java:83)",
      "com.kn.ife.cfg.Configurations.createCurrent(Configurations.java:319)",
      "com.kn.ife.cfg.vm.ReleaseComparisons.createSalogCurrent(ReleaseComparisons.java:50)",
      "java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)",
      "java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)",
      "java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)",
      "java.base/java.lang.reflect.Method.invoke(Method.java:566)",
      "org.apache.causeway.commons.internal.reflection._Reflect.lambda$invokeMethodOn${'$'}11(_Reflect.java:549)",
      "org.apache.causeway.commons.functional.Try.call(Try.java:55)",
      "org.apache.causeway.commons.internal.reflection._Reflect.invokeMethodOn(_Reflect.java:547)",
      "org.apache.causeway.core.metamodel.commons.CanonicalInvoker.invoke(CanonicalInvoker.java:120)",
      "org.apache.causeway.core.metamodel.commons.CanonicalInvoker.invoke(CanonicalInvoker.java:108)",
      "org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetForDomainEventAbstract.invokeMethodElseFromCache(ActionInvocationFacetForDomainEventAbstract.java:162)",
      "org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetForDomainEventAbstract$DomainEventMemberExecutor.execute(ActionInvocationFacetForDomainEventAbstract.java:207)",
      "org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetForDomainEventAbstract$DomainEventMemberExecutor.execute(ActionInvocationFacetForDomainEventAbstract.java:174)",
      "org.apache.causeway.core.interaction.session.CausewayInteraction.executeInternal(CausewayInteraction.java:136)",
      "org.apache.causeway.core.interaction.session.CausewayInteraction.execute(CausewayInteraction.java:105)",
      "org.apache.causeway.core.runtimeservices.executor.MemberExecutorServiceDefault.invokeAction(MemberExecutorServiceDefault.java:153)",
      "org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetForDomainEventAbstract.doInvoke(ActionInvocationFacetForDomainEventAbstract.java:131)",
      "org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetForDomainEventAbstract.lambda$invoke${'$'}1(ActionInvocationFacetForDomainEventAbstract.java:99)",
      "org.apache.causeway.commons.functional.Try.call(Try.java:55)",
      "org.apache.causeway.core.runtimeservices.transaction.TransactionServiceSpring.callTransactional(TransactionServiceSpring.java:108)",
      "org.apache.causeway.applib.services.xactn.TransactionalProcessor.callWithinCurrentTransactionElseCreateNew(TransactionalProcessor.java:100)",
      "org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetForDomainEventAbstract.invoke(ActionInvocationFacetForDomainEventAbstract.java:98)",
      "org.apache.causeway.core.metamodel.specloader.specimpl.ObjectActionDefault.executeInternal(ObjectActionDefault.java:421)",
      "org.apache.causeway.core.metamodel.specloader.specimpl.ObjectActionDefault.execute(ObjectActionDefault.java:409)",
      "org.apache.causeway.core.metamodel.interactions.managed.ManagedAction.invoke(ManagedAction.java:134)",
      "org.apache.causeway.core.metamodel.interactions.managed.ManagedAction.invoke(ManagedAction.java:141)",
      "org.apache.causeway.core.metamodel.interactions.managed.ActionInteraction.invokeWith(ActionInteraction.java:150)",
      "org.apache.causeway.viewer.restfulobjects.viewer.resources._DomainResourceHelper.invokeAction(_DomainResourceHelper.java:285)",
      "org.apache.causeway.viewer.restfulobjects.viewer.resources._DomainResourceHelper.invokeAction(_DomainResourceHelper.java:193)",
      "org.apache.causeway.viewer.restfulobjects.viewer.resources.DomainObjectResourceServerside.invokeAction(DomainObjectResourceServerside.java:770)",
      "java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)",
      "java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)",
      "java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)",
      "java.base/java.lang.reflect.Method.invoke(Method.java:566)",
      "org.jboss.resteasy.core.MethodInjectorImpl.invoke(MethodInjectorImpl.java:170)",
      "org.jboss.resteasy.core.MethodInjectorImpl.invoke(MethodInjectorImpl.java:130)",
      "org.jboss.resteasy.core.ResourceMethodInvoker.internalInvokeOnTarget(ResourceMethodInvoker.java:660)",
      "org.jboss.resteasy.core.ResourceMethodInvoker.invokeOnTargetAfterFilter(ResourceMethodInvoker.java:524)",
      "org.jboss.resteasy.core.ResourceMethodInvoker.lambda$invokeOnTarget${'$'}2(ResourceMethodInvoker.java:474)",
      "org.jboss.resteasy.core.interception.jaxrs.PreMatchContainerRequestContext.filter(PreMatchContainerRequestContext.java:364)",
      "org.jboss.resteasy.core.ResourceMethodInvoker.invokeOnTarget(ResourceMethodInvoker.java:476)",
      "org.jboss.resteasy.core.ResourceMethodInvoker.invoke(ResourceMethodInvoker.java:434)",
      "org.jboss.resteasy.core.ResourceMethodInvoker.invoke(ResourceMethodInvoker.java:408)",
      "org.jboss.resteasy.core.ResourceMethodInvoker.invoke(ResourceMethodInvoker.java:69)",
      "org.jboss.resteasy.core.SynchronousDispatcher.invoke(SynchronousDispatcher.java:492)",
      "org.jboss.resteasy.core.SynchronousDispatcher.lambda$invoke${'$'}4(SynchronousDispatcher.java:261)",
      "org.jboss.resteasy.core.SynchronousDispatcher.lambda$preprocess${'$'}0(SynchronousDispatcher.java:161)",
      "org.jboss.resteasy.core.interception.jaxrs.PreMatchContainerRequestContext.filter(PreMatchContainerRequestContext.java:364)",
      "org.jboss.resteasy.core.SynchronousDispatcher.preprocess(SynchronousDispatcher.java:164)",
      "org.jboss.resteasy.core.SynchronousDispatcher.invoke(SynchronousDispatcher.java:247)",
      "org.jboss.resteasy.plugins.server.servlet.ServletContainerDispatcher.service(ServletContainerDispatcher.java:249)",
      "org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher.service(HttpServletDispatcher.java:60)",
      "org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher.service(HttpServletDispatcher.java:55)",
      "javax.servlet.http.HttpServlet.service(HttpServlet.java:590)",
      "org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:227)",
      "org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:162)",
      "org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:53)",
      "org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:189)",
      "org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:162)",
      "org.apache.causeway.viewer.restfulobjects.viewer.webmodule.CausewayRestfulObjectsInteractionFilter.lambda$doFilter${'$'}1(CausewayRestfulObjectsInteractionFilter.java:387)",
      "org.apache.causeway.commons.functional.ThrowingRunnable.lambda$toCallable${'$'}0(ThrowingRunnable.java:42)",
      "org.apache.causeway.commons.functional.Try.call(Try.java:55)",
      "org.apache.causeway.core.runtimeservices.transaction.TransactionServiceSpring.callTransactional(TransactionServiceSpring.java:108)",
      "org.apache.causeway.applib.services.xactn.TransactionalProcessor.callWithinCurrentTransactionElseCreateNew(TransactionalProcessor.java:100)",
      "org.apache.causeway.applib.services.xactn.TransactionalProcessor.runWithinCurrentTransactionElseCreateNew(TransactionalProcessor.java:110)",
      "org.apache.causeway.viewer.restfulobjects.viewer.webmodule.CausewayRestfulObjectsInteractionFilter.lambda$doFilter${'$'}3(CausewayRestfulObjectsInteractionFilter.java:386)",
      "org.apache.causeway.core.runtimeservices.session.InteractionServiceDefault.runInternal(InteractionServiceDefault.java:329)",
      "org.apache.causeway.core.runtimeservices.session.InteractionServiceDefault.run(InteractionServiceDefault.java:272)",
      "org.apache.causeway.viewer.restfulobjects.viewer.webmodule.CausewayRestfulObjectsInteractionFilter.doFilter(CausewayRestfulObjectsInteractionFilter.java:383)",
      "org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:189)",
      "org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:162)",
      "org.apache.causeway.core.webapp.modules.logonlog.CausewayLogOnExceptionFilter.doFilter(CausewayLogOnExceptionFilter.java:60)",
      "org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:189)",
      "org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:162)",
      "org.springframework.web.filter.CorsFilter.doFilterInternal(CorsFilter.java:91)",
      "org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:117)",
      "org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:189)",
      "org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:162)",
      "org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93)",
      "org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:117)",
      "org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:189)",
      "org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:162)",
      "org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:201)",
      "org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:117)",
      "org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:189)",
      "org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:162)",
      "org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:197)",
      "org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:97)",
      "org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:541)",
      "org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:135)",
      "org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:92)",
      "org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:78)",
      "org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:360)",
      "org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:399)",
      "org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:65)",
      "org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:890)",
      "org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1743)",
      "org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:49)",
      "org.apache.tomcat.util.threads.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1191)",
      "org.apache.tomcat.util.threads.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:659)",
      "org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61)",
      "java.base/java.lang.Thread.run(Thread.java:829)"
    ],
    "causedBy": null
  }
}
"""
}
