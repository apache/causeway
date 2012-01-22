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

package org.apache.isis.runtimes.dflt.bytecode.javassist.objectfactory.internal;

import java.lang.reflect.Method;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.lang.ArrayUtils;
import org.apache.isis.core.metamodel.facets.ImperativeFacetUtils;
import org.apache.isis.core.metamodel.facets.ImperativeFacetUtils.ImperativeFacetFlags;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.specloader.specimpl.dflt.ObjectSpecificationDefault;
import org.apache.isis.runtimes.dflt.runtime.bytecode.ObjectResolveAndObjectChangedEnhancerAbstract;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectfactory.ObjectChanger;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectfactory.ObjectResolver;

public class ObjectResolveAndObjectChangedEnhancer extends ObjectResolveAndObjectChangedEnhancerAbstract {

    private MethodHandler methodHandler;

    public ObjectResolveAndObjectChangedEnhancer(final ObjectResolver objectResolver, final ObjectChanger objectChanger, final SpecificationLoader specificationLoader) {
        super(objectResolver, objectChanger, specificationLoader);

        createCallback();
    }

    @Override
    protected void createCallback() {
        this.methodHandler = new MethodHandler() {
            @Override
            public Object invoke(final Object proxied, final Method proxyMethod, final Method proxiedMethod, final Object[] args) throws Throwable {

                final boolean ignore = proxyMethod.getDeclaringClass().equals(Object.class);
                ImperativeFacetFlags flags = null;

                if (!ignore) {
                    final ObjectSpecificationDefault targetObjSpec = getJavaSpecificationOfOwningClass(proxiedMethod);

                    final ObjectMember member = targetObjSpec.getMember(proxiedMethod);
                    flags = ImperativeFacetUtils.getImperativeFacetFlags(member, proxiedMethod);

                    if (flags.impliesResolve()) {
                        objectResolver.resolve(proxied, member.getName());
                    }
                }

                final Object proxiedReturn = proxiedMethod.invoke(proxied, args); // execute
                                                                                  // the
                                                                                  // original
                                                                                  // method.

                if (!ignore && flags.impliesObjectChanged()) {
                    objectChanger.objectChanged(proxied);
                }

                return proxiedReturn;
            }
        };
    }

    @SuppressWarnings("unchecked")
    public <T> T newInstance(final Class<T> cls) {

        final ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setSuperclass(cls);
        proxyFactory.setInterfaces(ArrayUtils.combine(cls.getInterfaces(), new Class<?>[] { JavassistEnhanced.class }));

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
        } catch (final InstantiationException e) {
            throw new IsisException(e);
        } catch (final IllegalAccessException e) {
            throw new IsisException(e);
        }
    }

}
