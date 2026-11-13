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
package org.apache.causeway.client.kroviz.snapshots.simpleapp1_16_0

import org.apache.causeway.client.kroviz.snapshots.Response

object HTTP_ERROR_405: Response() {
    val ReadCallback = "\$ReadCallback"
    val CachedChain = "\$CachedChain"

    override val url = ""
    override val str = """{
    "httpStatusCode": 405,
    "message": "Method not allowed; action 'create' does not have safe semantics",
    "detail": {
        "className": "org.apache.causeway.viewer.restfulobjects.rendering.RestfulObjectsApplicationException",
        "message": "Method not allowed; action 'create' does not have safe semantics",
        "element": [
            "org.apache.causeway.viewer.restfulobjects.rendering.RestfulObjectsApplicationException.createWithCauseAndMessage(RestfulObjectsApplicationException.java:39)",
            "org.apache.causeway.viewer.restfulobjects.rendering.RestfulObjectsApplicationException.createWithMessage(RestfulObjectsApplicationException.java:31)",
            "org.apache.causeway.viewer.restfulobjects.server.resources.DomainResourceHelper.invokeActionQueryOnly(DomainResourceHelper.java:311)",
            "org.apache.causeway.viewer.restfulobjects.server.resources.DomainServiceResourceServerside.invokeActionQueryOnly(DomainServiceResourceServerside.java:215)",
            "sun.reflect.GeneratedMethodAccessor179.invoke(Unknown Source)",
            "sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)",
            "java.lang.reflect.Method.invoke(Method.java:498)",
            "org.jboss.resteasy.core.MethodInjectorImpl.invoke(MethodInjectorImpl.java:140)",
            "org.jboss.resteasy.core.ResourceMethodInvoker.invokeOnTarget(ResourceMethodInvoker.java:295)",
            "org.jboss.resteasy.core.ResourceMethodInvoker.invoke(ResourceMethodInvoker.java:249)",
            "org.jboss.resteasy.core.ResourceMethodInvoker.invoke(ResourceMethodInvoker.java:236)",
            "org.jboss.resteasy.core.SynchronousDispatcher.invoke(SynchronousDispatcher.java:406)",
            "org.jboss.resteasy.core.SynchronousDispatcher.invoke(SynchronousDispatcher.java:213)",
            "org.jboss.resteasy.plugins.server.servlet.ServletContainerDispatcher.service(ServletContainerDispatcher.java:228)",
            "org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher.service(HttpServletDispatcher.java:56)",
            "org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher.service(HttpServletDispatcher.java:51)",
            "javax.servlet.http.HttpServlet.service(HttpServlet.java:790)",
            "org.eclipse.jetty.servlet.ServletHolder.handle(ServletHolder.java:821)",
            "org.eclipse.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1685)",
            "org.apache.causeway.viewer.restfulobjects.server.webapp.CausewayTransactionFilterForRestfulObjects.doFilter(CausewayTransactionFilterForRestfulObjects.java:50)",
            "org.eclipse.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1668)",
            "org.apache.causeway.core.webapp.CausewaySessionFilter.doFilter(CausewaySessionFilter.java:341)",
            "org.eclipse.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1668)",
            "org.apache.causeway.core.webapp.diagnostics.CausewayLogOnExceptionFilter.doFilter(CausewayLogOnExceptionFilter.java:52)",
            "org.eclipse.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1668)",
            "org.apache.shiro.web.servlet.AbstractShiroFilter.executeChain(AbstractShiroFilter.java:449)",
            "org.apache.shiro.web.servlet.AbstractShiroFilter${'$'}1.call(AbstractShiroFilter.java:365)",
            "org.apache.shiro.subject.support.SubjectCallable.doCall(SubjectCallable.java:90)",
            "org.apache.shiro.subject.support.SubjectCallable.call(SubjectCallable.java:83)",
            "org.apache.shiro.subject.support.DelegatingSubject.execute(DelegatingSubject.java:383)",
            "org.apache.shiro.web.servlet.AbstractShiroFilter.doFilterInternal(AbstractShiroFilter.java:362)",
            "org.apache.shiro.web.servlet.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:125)",
            "org.eclipse.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1668)",
            "org.eclipse.jetty.servlets.CrossOriginFilter.handle(CrossOriginFilter.java:308)",
            "org.eclipse.jetty.servlets.CrossOriginFilter.doFilter(CrossOriginFilter.java:262)",
            "org.eclipse.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1668)",
            "org.eclipse.jetty.servlet.ServletHandler.doHandle(ServletHandler.java:581)",
            "org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:143)",
            "org.eclipse.jetty.security.SecurityHandler.handle(SecurityHandler.java:548)",
            "org.eclipse.jetty.server.session.SessionHandler.doHandle(SessionHandler.java:226)",
            "org.eclipse.jetty.server.handler.ContextHandler.doHandle(ContextHandler.java:1158)",
            "org.eclipse.jetty.servlet.ServletHandler.doScope(ServletHandler.java:511)",
            "org.eclipse.jetty.server.session.SessionHandler.doScope(SessionHandler.java:185)",
            "org.eclipse.jetty.server.handler.ContextHandler.doScope(ContextHandler.java:1090)",
            "org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:141)",
            "org.eclipse.jetty.server.handler.ContextHandlerCollection.handle(ContextHandlerCollection.java:213)",
            "org.eclipse.jetty.server.handler.HandlerCollection.handle(HandlerCollection.java:109)",
            "org.eclipse.jetty.server.handler.HandlerWrapper.handle(HandlerWrapper.java:119)",
            "org.eclipse.jetty.server.Server.handle(Server.java:517)",
            "org.eclipse.jetty.server.HttpChannel.handle(HttpChannel.java:306)",
            "org.eclipse.jetty.server.HttpConnection.onFillable(HttpConnection.java:242)",
            "org.eclipse.jetty.io.AbstractConnection$ReadCallback.succeeded(AbstractConnection.java:261)",
            "org.eclipse.jetty.io.FillInterest.fillable(FillInterest.java:95)",
            "org.eclipse.jetty.io.SelectChannelEndPoint${'$'}2.run(SelectChannelEndPoint.java:75)",
            "org.eclipse.jetty.util.thread.strategy.ExecuteProduceConsume.produceAndRun(ExecuteProduceConsume.java:213)",
            "org.eclipse.jetty.util.thread.strategy.ExecuteProduceConsume.run(ExecuteProduceConsume.java:147)",
            "org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:654)",
            "org.eclipse.jetty.util.thread.QueuedThreadPool${'$'}3.run(QueuedThreadPool.java:572)",
            "java.lang.Thread.run(Thread.java:748)"
        ],
        "causedBy": null
    }
}
"""
}
