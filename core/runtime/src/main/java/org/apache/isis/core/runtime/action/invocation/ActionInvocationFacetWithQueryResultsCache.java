package org.apache.isis.core.runtime.action.invocation;

import java.util.concurrent.Callable;

import org.apache.isis.applib.services.queryresultscache.QueryResultsCache;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.DecoratingFacet;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacet;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetAbstract;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.runtime.system.context.IsisContext;

public class ActionInvocationFacetWithQueryResultsCache extends ActionInvocationFacetAbstract implements DecoratingFacet<ActionInvocationFacet> {

    final ActionInvocationFacet underlyingFacet;

    @Override
    public ActionInvocationFacet getDecoratedFacet() {
        return this.underlyingFacet;
    }

    public ActionInvocationFacetWithQueryResultsCache(
            final ActionInvocationFacet underlyingFacet) {
        super(underlyingFacet.getFacetHolder());
        this.underlyingFacet = underlyingFacet;
    }

    @Override
    public ObjectAdapter invoke(
            final ObjectAction owningAction,
            final ObjectAdapter targetAdapter,
            final ObjectAdapter[] argumentAdapters) {

        final ObjectAdapter result = getQueryResultsCache().execute(new Callable<ObjectAdapter>() {

            @Override
            public ObjectAdapter call() throws Exception {
                return ActionInvocationFacetWithQueryResultsCache.this.underlyingFacet.invoke(owningAction, targetAdapter, argumentAdapters);
            }
        }, ActionInvocationFacetWithQueryResultsCache.class, "invoke", owningAction, targetAdapter, targetAdapter);

        return result;
    }

    @Deprecated
    @Override
    public ObjectAdapter invoke(
            final ObjectAdapter targetAdapter,
            final ObjectAdapter[] argumentAdapters) {

        final CommandContext commandContext = getCommandContext();
        final Command command = commandContext != null ? commandContext.getCommand() : null;

        final ObjectAdapter result = getQueryResultsCache().execute(new Callable<ObjectAdapter>() {

            @SuppressWarnings("deprecation")
            @Override
            public ObjectAdapter call() throws Exception {
                return ActionInvocationFacetWithQueryResultsCache.this.underlyingFacet.invoke(targetAdapter, argumentAdapters);
            }
        }, command.getTarget().getClass(), command.getMemberIdentifier(), command.getArguments());

        return result;
    }

    @Override
    public ObjectSpecification getReturnType() {
        return this.underlyingFacet.getReturnType();
    }

    @Override
    public ObjectSpecification getOnType() {
        return this.underlyingFacet.getOnType();
    }

    @Override
    public String toString() {
        return super.toString() + " --> " + this.underlyingFacet.toString();
    }

    // ///////////////////////////////////////////////////////////////
    // Dependencies (from context)
    // ///////////////////////////////////////////////////////////////

    private static QueryResultsCache getQueryResultsCache() {
        return IsisContext.getPersistenceSession().getServiceOrNull(QueryResultsCache.class);
    }

}
