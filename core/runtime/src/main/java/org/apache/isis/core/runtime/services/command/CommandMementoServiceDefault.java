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
import java.util.UUID;

import javax.annotation.PostConstruct;

import com.google.common.collect.Lists;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.background.ActionInvocationMemento;
import org.apache.isis.applib.services.background.BackgroundCommandService;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.CommandUtil;
import org.apache.isis.core.metamodel.services.command.CommandMementoService;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.specimpl.dflt.ObjectSpecificationDefault;
import org.apache.isis.core.runtime.services.memento.MementoServiceDefault;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.schema.cmd.v1.ActionDto;
import org.apache.isis.schema.cmd.v1.CommandMementoDto;
import org.apache.isis.schema.cmd.v1.ParamDto;
import org.apache.isis.schema.utils.CommandMementoDtoUtils;

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

    @Deprecated
    @Programmatic
    @Override
    public ActionInvocationMemento asActionInvocationMemento(
            final Method method,
            final Object domainObject,
            final Object[] args) {
        
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

    @Override
    public CommandMementoDto asCommandMemento(
            final List<ObjectAdapter> targetAdapters,
            final ObjectAction objectAction,
            final ObjectAdapter[] argAdapters) {

        final CommandMementoDto dto = new CommandMementoDto();
        dto.setMajorVersion("1");
        dto.setMinorVersion("0");

        for (ObjectAdapter targetAdapter : targetAdapters) {
            final RootOid rootOid = (RootOid) targetAdapter.getOid();
            final Bookmark bookmark = rootOid.asBookmark();
            dto.getTargets().add(bookmark.toOidDto());
        }

        final ActionDto actionDto = new ActionDto();
        dto.setAction(actionDto);

        addActionArgs(objectAction, actionDto, argAdapters);

        dto.setTransactionId(UUID.randomUUID().toString());
        return dto;
    }

    @Override
    public CommandMementoDto asCommandMemento(
            final ObjectAdapter targetAdapter,
            final OneToOneAssociation association,
            final ObjectAdapter valueAdapterOrNull) {

        // TODO.  introduce a choice for aim vs pmm, in the cmd.xsd

        throw new RuntimeException("not yet implemented");
    }

    @Override
    public void addActionArgs(
            final ObjectAction objectAction,
            final ActionDto actionDto,
            final ObjectAdapter[] argAdapters) {
        final String actionIdentifier = CommandUtil.actionIdentifierFor(objectAction);
        actionDto.setActionIdentifier(actionIdentifier);

        List<ObjectActionParameter> actionParameters = objectAction.getParameters();
        for (int paramNum = 0; paramNum < actionParameters.size(); paramNum++) {
            final ObjectActionParameter actionParameter = actionParameters.get(paramNum);
            final String parameterName = actionParameter.getName();
            final Class<?> paramType = actionParameter.getSpecification().getCorrespondingClass();
            final ObjectAdapter argAdapter = argAdapters[paramNum];
            final Object arg = argAdapter != null? argAdapter.getObject(): null;
            final List<ParamDto> parameters = actionDto.getParameters();
            CommandMementoDtoUtils.addParamArg(
                    parameters, parameterName, paramType, arg, bookmarkService);
        }
    }

    // //////////////////////////////////////


    @javax.inject.Inject
    private BookmarkService bookmarkService;


    // //////////////////////////////////////

    protected SpecificationLoaderSpi getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

    protected AdapterManager getAdapterManager() {
        return IsisContext.getPersistenceSession();
    }



}
