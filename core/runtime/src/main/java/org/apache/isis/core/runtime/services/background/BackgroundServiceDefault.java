package org.apache.isis.core.runtime.services.background;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import javax.annotation.PostConstruct;

import com.google.common.collect.Lists;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.background.ActionInvocationMemento;
import org.apache.isis.applib.services.background.BackgroundActionService;
import org.apache.isis.applib.services.background.BackgroundService;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.reifiableaction.ReifiableAction;
import org.apache.isis.applib.services.reifiableaction.ReifiableActionContext;
import org.apache.isis.core.commons.ensure.Ensure;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.lang.ArrayExtensions;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.specloader.classsubstitutor.JavassistEnhanced;
import org.apache.isis.core.metamodel.specloader.specimpl.dflt.ObjectSpecificationDefault;
import org.apache.isis.core.progmodel.facets.actions.invoke.ReifiableActionUtil;
import org.apache.isis.core.runtime.services.memento.MementoServiceDefault;
import org.apache.isis.core.runtime.system.context.IsisContext;

public class BackgroundServiceDefault implements BackgroundService {

    private final MementoServiceDefault mementoService;
    
    public BackgroundServiceDefault() {
        this(new MementoServiceDefault());
    }
    
    BackgroundServiceDefault(MementoServiceDefault mementoService) {
        this.mementoService = mementoService.withNoEncoding();
    }
    
    // //////////////////////////////////////

    
    @Programmatic
    @PostConstruct
    public void init(Map<String,String> props) {
        ensureDependenciesInjected();
    }
    
    private void ensureDependenciesInjected() {
        Ensure.ensureThatState(this.bookmarkService, is(not(nullValue())), "BookmarkService domain service must be configured");
        Ensure.ensureThatState(this.backgroundActionService, is(not(nullValue())), "BackgroundActionService domain service must be configured");
        Ensure.ensureThatState(this.reifiableActionContext, is(not(nullValue())), "ReifiableActionContext domain service must be configured");
    }


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
        } catch (final InstantiationException e) {
            throw new IsisException(e);
        } catch (final IllegalAccessException e) {
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

                final String actionIdentifier = ReifiableActionUtil.actionIdentifierFor(action);
                final String targetClassName = ReifiableActionUtil.targetClassNameFor(targetAdapter);
                final String targetActionName = ReifiableActionUtil.targetActionNameFor(action);
                final String targetArgs = ReifiableActionUtil.argDescriptionFor(action, adaptersFor(args));
                
                final Bookmark domainObjectBookmark = bookmarkService.bookmarkFor(domainObject);

                final List<Class<?>> argTypes = Lists.newArrayList();
                final List<Object> argObjs = Lists.newArrayList();
                ReifiableActionUtil.buildMementoArgLists(mementoService, bookmarkService, proxiedMethod, args, argTypes, argObjs);

                final ReifiableAction reifiableAction = reifiableActionContext.getReifiableAction();
                
                final ActionInvocationMemento aim = 
                        new ActionInvocationMemento(mementoService, 
                                actionIdentifier, 
                                domainObjectBookmark,
                                argTypes,
                                argObjs);
               
                backgroundActionService.schedule(aim, reifiableAction, targetClassName, targetActionName, targetArgs);
                
                return null;
            }

            ObjectAdapter[] adaptersFor(final Object[] args) {
                final AdapterManager adapterManager = getAdapterManager();
                return ReifiableActionUtil.adaptersFor(args, adapterManager);
            }

        };
    }

    @Override
    public ActionInvocationMemento asActionInvocationMemento(Method method, Object domainObject, Object[] args) {
        
        final ObjectSpecificationDefault targetObjSpec = getJavaSpecificationOfOwningClass(method);
        final ObjectMember member = targetObjSpec.getMember(method);
        if(member == null) {
            return null;
        }
        if(!(member instanceof ObjectAction)) {
            return null;
        }

        final ObjectAction action = (ObjectAction) member;
        final String actionIdentifier = ReifiableActionUtil.actionIdentifierFor(action);
        
        final Bookmark domainObjectBookmark = bookmarkService.bookmarkFor(domainObject);

        final List<Class<?>> argTypes = Lists.newArrayList();
        final List<Object> argObjs = Lists.newArrayList();
        ReifiableActionUtil.buildMementoArgLists(mementoService, bookmarkService, method, args, argTypes, argObjs);

        final ActionInvocationMemento aim = 
                new ActionInvocationMemento(mementoService, 
                        actionIdentifier, 
                        domainObjectBookmark,
                        argTypes,
                        argObjs);
       
        return aim;
    }


    @Override
    public ActionInvocationMemento newActionInvocationMemento(String mementoStr) {
        return new ActionInvocationMemento(mementoService, mementoStr);
    }

    // //////////////////////////////////////

    private BookmarkService bookmarkService;
    /**
     * Mandatory service.
     */
    public void injectBookmarkService(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }
    
    private BackgroundActionService backgroundActionService;
    /**
     * Mandatory service.
     */
    public void injectBackgroundActionService(BackgroundActionService backgroundActionService) {
        this.backgroundActionService = backgroundActionService;
    }

    private ReifiableActionContext reifiableActionContext;
    /**
     * Mandatory service.
     */
    public void injectReifiableActionContext(ReifiableActionContext reifiableActionContext) {
        this.reifiableActionContext = reifiableActionContext;
    }
    

    
    // //////////////////////////////////////

    protected SpecificationLoaderSpi getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

    protected AdapterManager getAdapterManager() {
        return IsisContext.getPersistenceSession().getAdapterManager();
    }



}
