package org.apache.isis.viewer.wicket.viewer;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.services.queryresultscache.QueryResultsCache;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;

class TargetRespondListenerToResetQueryResultCache implements AjaxRequestTarget.ITargetRespondListener {

    private static final Logger LOG = LoggerFactory.getLogger(TargetRespondListenerToResetQueryResultCache.class);

    @Override
    public void onTargetRespond(final AjaxRequestTarget target) {

        if(LOG.isDebugEnabled()) {
            LOG.debug("RESPOND PHASE STARTED: resetting cache");
        }

        final QueryResultsCache queryResultsCache = lookupQueryResultsCache();
        queryResultsCache.resetForNextTransaction();
    }

    private QueryResultsCache lookupQueryResultsCache() {
        return getServicesInjector().lookupService(QueryResultsCache.class);
    }

    private ServicesInjector getServicesInjector() {
        return getSessionFactory().getServicesInjector();
    }

    private IsisSessionFactory getSessionFactory() {
        return IsisContext.getSessionFactory();
    }
}
