/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.runtime.services.background;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.background.ActionInvocationMemento;
import org.apache.isis.applib.services.background.BackgroundCommandService;
import org.apache.isis.applib.services.background.BackgroundCommandService2;
import org.apache.isis.applib.services.background.BackgroundService;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.lang.ArrayExtensions;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.CommandUtil;
import org.apache.isis.core.metamodel.services.command.CommandMementoService;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.specloader.classsubstitutor.JavassistEnhanced;
import org.apache.isis.core.metamodel.specloader.specimpl.dflt.ObjectSpecificationDefault;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.schema.cmd.v1.CommandMementoDto;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

/**
 * Depends on an implementation of {@link org.apache.isis.applib.services.background.BackgroundCommandService} to
 * be configured.
 */
@DomainService(
        nature = NatureOfService.DOMAIN
)
public class BackgroundServiceDefault implements BackgroundService {



    @Programmatic
    @PostConstruct
    public void init(Map<String,String> props) {
    }

    @Programmatic
    @PreDestroy
    public void shutdown() {

    }

    // //////////////////////////////////////


    private ObjectSpecificationDefault getJavaSpecificationOfOwningClass(final Method method) {
        return getJavaSpecification(method.getDeclaringClass());
    }

    private ObjectSpecificationDefault getJavaSpecification(final Class<?> cls) {
        final ObjectSpecification objectSpec = getSpecification(cls);
        if (!(objectSpec instanceof ObjectSpecificationDefault)) {
            throw new UnsupportedOperationException(
                "Only Java is supported "
                + "(specification is '" + objectSpec.getClass().getCanonicalName() + "')");
        }
        return (ObjectSpecificationDefault) objectSpec;
    }

    private ObjectSpecification getSpecification(final Class<?> type) {
        return getSpecificationLoader().loadSpecification(type);
    }


    // //////////////////////////////////////

    @Programmatic
    @Override
    public <T> T execute(final T domainObject) {
        final Class<? extends Object> cls = domainObject.getClass();
        final MethodHandler methodHandler = newMethodHandler(domainObject);
        return newProxy(cls, methodHandler);
    }


    @SuppressWarnings("unchecked")
    private <T> T newProxy(Class<? extends Object> cls, MethodHandler methodHandler) {
        final ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setSuperclass(cls);
        proxyFactory.setInterfaces(ArrayExtensions.combine(cls.getInterfaces(), new Class<?>[] { JavassistEnhanced.class }));

        proxyFactory.setFilter(new MethodFilter() {
            @Override
            public boolean isHandled(final Method m) {
                // ignore finalize()
                return !m.getName().equals("finalize");
            }
        });

        final Class<T> proxySubclass = proxyFactory.createClass();
        try {
            final T newInstance = proxySubclass.newInstance();
            final ProxyObject proxyObject = (ProxyObject) newInstance;
            proxyObject.setHandler(methodHandler);

            return newInstance;
        } catch (final InstantiationException | IllegalAccessException e) {
            throw new IsisException(e);
        }
    }

    private <T> MethodHandler newMethodHandler(final T domainObject) {
        return new MethodHandler() {
            @Override
            public Object invoke(final Object proxied, final Method proxyMethod, final Method proxiedMethod, final Object[] args) throws Throwable {

                final boolean inheritedFromObject = proxyMethod.getDeclaringClass().equals(Object.class);
                if(inheritedFromObject) {
                    return proxyMethod.invoke(domainObject, args);
                }

                final ObjectAdapter targetAdapter = getAdapterManager().adapterFor(domainObject);
                final ObjectSpecificationDefault targetObjSpec = getJavaSpecificationOfOwningClass(proxyMethod);
                final ObjectMember member = targetObjSpec.getMember(proxyMethod);

                if(member == null) {
                    return proxyMethod.invoke(domainObject, args);
                }

                if(!(member instanceof ObjectAction)) {
                    throw new UnsupportedOperationException(
                            "Only actions can be executed in the background "
                                    + "(method " + proxiedMethod.getName() + " represents a " + member.getFeatureType().name() + "')");
                }

                final ObjectAction action = (ObjectAction) member;

                final String targetClassName = CommandUtil.targetClassNameFor(targetAdapter);
                final String targetActionName = CommandUtil.targetActionNameFor(action);
                final ObjectAdapter[] argAdapters = adaptersFor(args);
                final String targetArgs = CommandUtil.argDescriptionFor(action, argAdapters);

                final Command command = commandContext.getCommand();

                if(backgroundCommandService instanceof BackgroundCommandService2) {
                    final BackgroundCommandService2 bcs2 = (BackgroundCommandService2) backgroundCommandService;

                    final CommandMementoDto dto = commandMementoService
                            .asCommandMemento(Collections.singletonList(targetAdapter), action, argAdapters);

                    bcs2.schedule(dto, command, targetClassName, targetActionName, targetArgs);
                    return null;
                }

                // fallback
                final ActionInvocationMemento aim = commandMementoService
                        .asActionInvocationMemento(proxyMethod, domainObject, args);

                backgroundCommandService.schedule(aim, command, targetClassName, targetActionName, targetArgs);
                return null;
            }

            ObjectAdapter[] adaptersFor(final Object[] args) {
                final AdapterManager adapterManager = getAdapterManager();
                return CommandUtil.adaptersFor(args, adapterManager);
            }

        };
    }

    // //////////////////////////////////////

    @Programmatic
    @Override
    public ActionInvocationMemento asActionInvocationMemento(Method method, Object domainObject, Object[] args) {
        throw new RuntimeException("Replaced by CommandMementoService");
    }


    // //////////////////////////////////////

    @javax.inject.Inject
    private BackgroundCommandService backgroundCommandService;

    @javax.inject.Inject
    private CommandMementoService commandMementoService;

    @javax.inject.Inject
    private BookmarkService bookmarkService;

    @javax.inject.Inject
    private CommandContext commandContext;


    // //////////////////////////////////////

    protected SpecificationLoaderSpi getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

    protected AdapterManager getAdapterManager() {
        return IsisContext.getPersistenceSession();
    }

}
