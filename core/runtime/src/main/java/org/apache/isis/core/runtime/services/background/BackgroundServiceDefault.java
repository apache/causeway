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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.background.BackgroundCommandService;
import org.apache.isis.applib.services.background.BackgroundCommandService2;
import org.apache.isis.applib.services.background.BackgroundService2;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.commons.internal._Constants;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.core.commons.lang.ArrayExtensions;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.CommandUtil;
import org.apache.isis.core.metamodel.services.command.CommandDtoServiceInternal;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.classsubstitutor.ProxyEnhanced;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectActionMixedIn;
import org.apache.isis.core.metamodel.specloader.specimpl.dflt.ObjectSpecificationDefault;
import org.apache.isis.core.plugins.codegen.ProxyFactory;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.schema.cmd.v1.CommandDto;

/**
 * Depends on an implementation of {@link org.apache.isis.applib.services.background.BackgroundCommandService} to
 * be configured.
 */
@DomainService(
        nature = NatureOfService.DOMAIN,
        menuOrder = "" + Integer.MAX_VALUE
)
public class BackgroundServiceDefault implements BackgroundService2 {


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
        return specificationLoader.loadSpecification(type);
    }


    // //////////////////////////////////////

    @Programmatic
    @Override
    public <T> T execute(final T domainObject) {
        final Class<T> cls = _Casts.uncheckedCast(domainObject.getClass());
        final InvocationHandler methodHandler = newMethodHandler(domainObject, null);
        return newProxy(cls, null, methodHandler);
    }

    @Override
    public <T> T executeMixin(Class<T> mixinClass, Object mixedIn) {
        final T mixin = factoryService.mixin(mixinClass, mixedIn);
        final InvocationHandler methodHandler = newMethodHandler(mixin, mixedIn);
        return newProxy(mixinClass, mixedIn, methodHandler);
    }

    private <T> T newProxy(
            final Class<T> cls,
            final Object mixedInIfAny,
            final InvocationHandler methodHandler) {

    	final Class<?>[] interfaces = ArrayExtensions.combine(
    			cls.getInterfaces(), 
    			new Class<?>[] { ProxyEnhanced.class }); 
    	
    	final boolean initialize = mixedInIfAny!=null;
    	
    	
        final Class<?>[] constructorArgTypes = initialize ? new Class<?>[] {mixedInIfAny.getClass()} : _Constants.emptyClasses;
        final Object[] constructorArgs = initialize ? new Object[] {mixedInIfAny} : _Constants.emptyObjects;
        
        final ProxyFactory<T> proxyFactory = ProxyFactory.builder(cls)
        		.interfaces(interfaces)
        		.constructorArgTypes(constructorArgTypes)
        		.build();
        
        return initialize 
        		? proxyFactory.createInstance(methodHandler, constructorArgs)  
        		: proxyFactory.createInstance(methodHandler, false)
        		;
    }

    /**
     *
     * @param target - the object that is proxied, either a domain object or a mixin around a domain object
     * @param mixedInIfAny - if target is a mixin, then this is the domain object that is mixed-in to.
     */
    private <T> InvocationHandler newMethodHandler(final T target, final Object mixedInIfAny) {
        return new InvocationHandler() {
            @Override
            public Object invoke(
                    final Object proxied,
                    final Method proxyMethod,
                    final Object[] args) throws Throwable {

                final boolean inheritedFromObject = proxyMethod.getDeclaringClass().equals(Object.class);
                if(inheritedFromObject) {
                    return proxyMethod.invoke(target, args);
                }

                final ObjectSpecificationDefault targetObjSpec = getJavaSpecificationOfOwningClass(proxyMethod);
                final ObjectMember member = targetObjSpec.getMember(proxyMethod);

                if(member == null) {
                    return proxyMethod.invoke(target, args);
                }

                if(!(member instanceof ObjectAction)) {
                    throw new UnsupportedOperationException(
                            "Only actions can be executed in the background "
                                    + "(method " + proxyMethod.getName() + " represents a " + member.getFeatureType().name() + "')");
                }

                ObjectAction action = (ObjectAction) member;

                final Object domainObject;
                if (mixedInIfAny == null) {
                    domainObject = target;
                } else {
                    domainObject = mixedInIfAny;
                    // replace action with the mixedIn action of the domain object itself
                    action = findMixedInAction(action, mixedInIfAny);
                }

                final ObjectAdapter domainObjectAdapter = getAdapterManager().adapterFor(domainObject);
                final String domainObjectClassName = CommandUtil.targetClassNameFor(domainObjectAdapter);

                final String targetActionName = CommandUtil.targetMemberNameFor(action);

                final ObjectAdapter[] argAdapters = adaptersFor(args);
                final String targetArgs = CommandUtil.argDescriptionFor(action, argAdapters);

                final Command command = commandContext.getCommand();

                final BackgroundCommandService2 bcs2 = (BackgroundCommandService2) backgroundCommandService;

                final List<ObjectAdapter> targetList = Collections.singletonList(domainObjectAdapter);
                final CommandDto dto =
                        commandDtoServiceInternal.asCommandDto(targetList, action, argAdapters);

                bcs2.schedule(dto, command, domainObjectClassName, targetActionName, targetArgs);


                return null;
            }

            private ObjectAction findMixedInAction(final ObjectAction action, final Object domainObject) {
                final String actionId = action.getId();
                final ObjectSpecification domainSpec = getAdapterManager().adapterFor(domainObject).getSpecification();
                List<ObjectAction> objectActions = domainSpec.getObjectActions(Contributed.INCLUDED);
                for (ObjectAction objectAction : objectActions) {
                    if(objectAction instanceof ObjectActionMixedIn) {
                        ObjectActionMixedIn objectActionMixedIn = (ObjectActionMixedIn) objectAction;
                        if(objectActionMixedIn.hasMixinAction(action)) {
                            return objectActionMixedIn;
                        }
                    }
                }

                throw new IllegalArgumentException(String.format(
                        "Unable to find mixin action '%s' for %s", actionId, domainSpec.getFullIdentifier()));
            }

            ObjectAdapter[] adaptersFor(final Object[] args) {
                final AdapterManager adapterManager = getAdapterManager();
                return CommandUtil.adaptersFor(args, adapterManager);
            }

        };
    }


    // //////////////////////////////////////

    @javax.inject.Inject
    private BackgroundCommandService backgroundCommandService;

    @javax.inject.Inject
    private CommandDtoServiceInternal commandDtoServiceInternal;

    @javax.inject.Inject
    private CommandContext commandContext;

    @javax.inject.Inject
    private FactoryService factoryService;

    @javax.inject.Inject
    private SpecificationLoader specificationLoader;

    @javax.inject.Inject
    private IsisSessionFactory isisSessionFactory;

    protected AdapterManager getAdapterManager() {
        return isisSessionFactory.getCurrentSession().getPersistenceSession();
    }

}
