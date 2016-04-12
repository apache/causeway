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
package org.apache.isis.core.runtime.services.command;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import com.google.common.collect.Lists;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.background.ActionInvocationMemento;
import org.apache.isis.applib.services.background.BackgroundCommandService;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.command.CommandMementoService;
import org.apache.isis.core.commons.ensure.Ensure;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.CommandUtil;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.specloader.specimpl.dflt.ObjectSpecificationDefault;
import org.apache.isis.core.runtime.services.memento.MementoServiceDefault;
import org.apache.isis.core.runtime.system.context.IsisContext;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;

/**
 * Depends on an implementation of {@link BackgroundCommandService} to
 * be configured.
 */
@DomainService(
        nature = NatureOfService.DOMAIN
)
public class CommandMementoServiceDefault implements CommandMementoService {

    private final MementoServiceDefault mementoService;

    public CommandMementoServiceDefault() {
        this(new MementoServiceDefault());
    }

    CommandMementoServiceDefault(MementoServiceDefault mementoService) {
        this.mementoService = mementoService.withNoEncoding();
    }
    
    // //////////////////////////////////////

    
    @Programmatic
    @PostConstruct
    public void init(Map<String,String> props) {
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
    public ActionInvocationMemento asActionInvocationMemento(Method method, Object domainObject, Object[] args) {
        
        final ObjectSpecificationDefault targetObjSpec = getJavaSpecificationOfOwningClass(method);
        final ObjectMember member = targetObjSpec.getMember(method);
        if(member == null) {
            return null;
        }

        if(!(member instanceof ObjectAction)) {
            throw new UnsupportedOperationException(String.format(
                    "Method %s does not correspond to an action.", method.getName()));
        }

        final ObjectAction action = (ObjectAction) member;
        final String actionIdentifier = CommandUtil.actionIdentifierFor(action);
        
        final Bookmark domainObjectBookmark = bookmarkService.bookmarkFor(domainObject);

        final List<Class<?>> argTypes = Lists.newArrayList();
        final List<Object> argObjs = Lists.newArrayList();
        CommandUtil.buildMementoArgLists(mementoService, bookmarkService, method, args, argTypes, argObjs);

        final ActionInvocationMemento aim = 
                new ActionInvocationMemento(mementoService, 
                        actionIdentifier, 
                        domainObjectBookmark,
                        argTypes,
                        argObjs);
       
        return aim;
    }


    /**
     * Not API
     */
    ActionInvocationMemento newActionInvocationMemento(String mementoStr) {
        return new ActionInvocationMemento(mementoService, mementoStr);
    }

    // //////////////////////////////////////

    @javax.inject.Inject
    private BookmarkService bookmarkService;


    private void ensureDependenciesInjected() {
        Ensure.ensureThatState(this.bookmarkService, is(not(nullValue())), "BookmarkService domain service must be configured");
    }

    // //////////////////////////////////////

    protected SpecificationLoaderSpi getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

    protected AdapterManager getAdapterManager() {
        return IsisContext.getPersistenceSession();
    }



}
