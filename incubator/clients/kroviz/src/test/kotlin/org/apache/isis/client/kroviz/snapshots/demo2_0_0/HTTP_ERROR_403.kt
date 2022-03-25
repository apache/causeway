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
package org.apache.isis.client.kroviz.snapshots.demo2_0_0

import org.apache.isis.client.kroviz.snapshots.Response

object HTTP_ERROR_403 : Response() {
    val invoke = "\$invoke"
    val invokeOnTarget = "\$invokeOnTarget"
    val preprocess = "\$preprocess"
    val doFilter = "\$doFilter"
    val ConnectionHandler = "\$ConnectionHandler"
    val SocketProcessor = "\$SocketProcessor"
    val Worker = "\$Worker"
    val WrappingRunnable = "\$WrappingRunnable"
    val toCallable = "\$toCallable"

    override val url = ""
    override val str = """{
  "httpStatusCode": 403,
  "message": "Search object in prompt (not yet implemented in demo)",
  "detail": {
    "className": "org.apache.isis.viewer.restfulobjects.rendering.RestfulObjectsApplicationException",
    "message": "Search object in prompt (not yet implemented in demo)",
    "element": [
      "org.apache.isis.viewer.restfulobjects.rendering.RestfulObjectsApplicationException.createWithCauseAndMessage(RestfulObjectsApplicationException.java:50)",
      "org.apache.isis.viewer.restfulobjects.rendering.RestfulObjectsApplicationException.createWithMessage(RestfulObjectsApplicationException.java:37)",
      "org.apache.isis.viewer.restfulobjects.viewer.resources.InteractionFailureHandler.onFailure(InteractionFailureHandler.java:53)",
      "org.apache.isis.core.metamodel.interactions.managed.MemberInteraction.getManagedMemberElseThrow(MemberInteraction.java:114)",
      "org.apache.isis.core.metamodel.interactions.managed.ActionInteraction.getManagedActionElseThrow(ActionInteraction.java:155)",
      "org.apache.isis.viewer.restfulobjects.viewer.resources.ObjectAdapterAccessHelper.getObjectActionThatIsVisibleForIntentAndSemanticConstraint(ObjectAdapterAccessHelper.java:64)",
      "org.apache.isis.viewer.restfulobjects.viewer.resources.DomainResourceHelper.actionPrompt(DomainResourceHelper.java:145)",
      "org.apache.isis.viewer.restfulobjects.viewer.resources.DomainObjectResourceServerside.actionPrompt(DomainObjectResourceServerside.java:537)",
      "jdk.internal.reflect.GeneratedMethodAccessor240.invoke(Unknown Source)",
      "java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)",
      "java.base/java.lang.reflect.Method.invoke(Method.java:564)",
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
      "javax.servlet.http.HttpServlet.service(HttpServlet.java:750)",
      "org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:228)",
      "org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:163)",
      "org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:53)",
      "org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:190)",
      "org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:163)",
      "org.apache.isis.viewer.restfulobjects.viewer.webmodule.IsisRestfulObjectsInteractionFilter.lambda$doFilter${'$'}1(IsisRestfulObjectsInteractionFilter.java:386)",
      "org.apache.isis.applib.services.iactnlayer.ThrowingRunnable.lambda$toCallable${'$'}0(ThrowingRunnable.java:44)",
      "org.apache.isis.commons.functional.Result.of(Result.java:58)",
      "org.apache.isis.core.runtimeservices.transaction.TransactionServiceSpring.callTransactional(TransactionServiceSpring.java:104)",
      "org.apache.isis.applib.services.xactn.TransactionalProcessor.callWithinCurrentTransactionElseCreateNew(TransactionalProcessor.java:100)",
      "org.apache.isis.applib.services.xactn.TransactionalProcessor.runWithinCurrentTransactionElseCreateNew(TransactionalProcessor.java:110)",
      "org.apache.isis.viewer.restfulobjects.viewer.webmodule.IsisRestfulObjectsInteractionFilter.lambda$doFilter${'$'}3(IsisRestfulObjectsInteractionFilter.java:385)",
      "org.apache.isis.core.runtimeservices.session.InteractionServiceDefault.run(InteractionServiceDefault.java:265)",
      "org.apache.isis.viewer.restfulobjects.viewer.webmodule.IsisRestfulObjectsInteractionFilter.doFilter(IsisRestfulObjectsInteractionFilter.java:382)",
      "org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:190)",
      "org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:163)",
      "org.apache.isis.core.webapp.modules.logonlog.IsisLogOnExceptionFilter.doFilter(IsisLogOnExceptionFilter.java:60)",
      "org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:190)",
      "org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:163)",
      "org.springframework.web.filter.CorsFilter.doFilterInternal(CorsFilter.java:91)",
      "org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:119)",
      "org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:190)",
      "org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:163)",
      "org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93)",
      "org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:119)",
      "org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:190)",
      "org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:163)",
      "org.springframework.boot.actuate.metrics.web.servlet.WebMvcMetricsFilter.doFilterInternal(WebMvcMetricsFilter.java:96)",
      "org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:119)",
      "org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:190)",
      "org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:163)",
      "org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:201)",
      "org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:119)",
      "org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:190)",
      "org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:163)",
      "org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:202)",
      "org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:97)",
      "org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:542)",
      "org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:143)",
      "org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:92)",
      "org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:78)",
      "org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:357)",
      "org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:382)",
      "org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:65)",
      "org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:893)",
      "org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1723)",
      "org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:49)",
      "java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1130)",
      "java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:630)",
      "org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61)",
      "java.base/java.lang.Thread.run(Thread.java:832)"
    ],
    "causedBy": null
  }
}
"""
}
