/*
 *
 *  Copyright 2012-2014 Eurocommercial Properties NV
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the
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
package webapp.scheduler;

import java.util.List;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

import org.quartz.JobExecutionContext;

import org.apache.isis.applib.clock.Clock;
import org.apache.isis.applib.services.background.ActionInvocationMemento;
import org.apache.isis.applib.services.background.BackgroundService;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.reifiableaction.ReifiableActionContext;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.oid.RootOidDefault;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.objectstore.jdo.applib.service.background.BackgroundActionServiceJdoRepository;
import org.apache.isis.objectstore.jdo.applib.service.reifiableaction.ReifiableActionJdo;

public class BackgroundActionExecutionJob extends AbstractIsisJob {

    protected void doExecute(JobExecutionContext context) {
        final BackgroundActionServiceJdoRepository backgroundActionRepo = getService(BackgroundActionServiceJdoRepository.class);
        final List<ReifiableActionJdo> findBackgroundActionsToStart = backgroundActionRepo.findBackgroundActionsToStart(); 
        for (final ReifiableActionJdo backgroundAction : findBackgroundActionsToStart) {

            try {
                reifiableActionContext.setReifiableAction(backgroundAction);

                backgroundAction.setStartedAt(Clock.getTimeAsJavaSqlTimestamp());
                
                final String memento = backgroundAction.getMemento();
                final ActionInvocationMemento aim = backgroundService.newActionInvocationMemento(memento);
                final String actionId = aim.getActionId();
    
                final Bookmark targetBookmark = aim.getTarget();
                final Object targetObject = bookmarkService.lookup(targetBookmark);
                
                final ObjectAdapter targetAdapter = adapterFor(targetObject);
                final ObjectSpecification specification = targetAdapter.getSpecification();
    
                ObjectAction objectAction = findAction(specification, actionId);
    
                if(objectAction != null) {
                    
                    final ObjectAdapter[] argAdapters = argAdaptersFor(aim);
                    ObjectAdapter resultAdapter = objectAction.execute(targetAdapter, argAdapters);
                    if(resultAdapter != null) {
                        Bookmark resultBookmark = bookmarkService.bookmarkFor(resultAdapter.getObject());
                        backgroundAction.setResult(resultBookmark);
                    }
                }

            } catch (Exception e) {
                backgroundAction.setException(Throwables.getStackTraceAsString(e));
            } finally {
                backgroundAction.setCompletedAt(Clock.getTimeAsJavaSqlTimestamp());
            }

        }
    }

    private ObjectAction findAction(final ObjectSpecification specification, final String actionId) {
        final List<ObjectAction> objectActions = specification.getObjectActions(Contributed.INCLUDED);
        for (final ObjectAction objectAction : objectActions) {
            if(objectAction.getIdentifier().toClassAndNameIdentityString().equals(actionId)) {
                return objectAction;
            }
        }
        return null;
    }

    private ObjectAdapter[] argAdaptersFor(final ActionInvocationMemento aim) throws ClassNotFoundException {
        final int numArgs = aim.getNumArgs();
        final List<ObjectAdapter> argumentAdapters = Lists.newArrayList();
        for(int i=0; i<numArgs; i++) {
            final ObjectAdapter argAdapter = argAdapterFor(aim, i);
            argumentAdapters.add(argAdapter);
        }
        return argumentAdapters.toArray(new ObjectAdapter[]{});
    }

    private ObjectAdapter argAdapterFor(final ActionInvocationMemento aim, int num) throws ClassNotFoundException {
        final Class<?> argType = aim.getArgType(num);
        final Object arg = aim.getArg(num, argType);
        if(arg == null) {
            return null;
        } 
        if(Bookmark.class != argType) {
            return adapterFor(arg);
        } 
        final Bookmark argBookmark = (Bookmark)arg;
        final RootOid rootOid = RootOidDefault.create(argBookmark);
        return getAdapterManager().adapterFor(rootOid);
    }

    private ObjectAdapter adapterFor(final Object targetObject) {
        return getAdapterManager().adapterFor(targetObject);
    }

    // //////////////////////////////////////

    
    protected AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }
    
    // //////////////////////////////////////

    
    @javax.inject.Inject
    private BackgroundService backgroundService;

    @javax.inject.Inject
    private BookmarkService bookmarkService;

    @javax.inject.Inject
    private ReifiableActionContext reifiableActionContext;
}
