package org.apache.isis.extensions.unittestsupport.dom.repo;

import java.util.Map;

import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.query.QueryDefault;

public class FinderInteraction {
    public enum FinderMethod {
        FIRST_MATCH,
        ALL_MATCHES,
        ALL_INSTANCES,
        UNIQUE_MATCH
    }
    private QueryDefault<?> queryDefault;
    private FinderInteraction.FinderMethod finderMethod;
    public FinderInteraction(Query<?> query, FinderInteraction.FinderMethod finderMethod) {
        super();
        this.queryDefault = (QueryDefault<?>) query;
        this.finderMethod = finderMethod;
    }
    public QueryDefault<?> getQueryDefault() {
        return queryDefault;
    }
    public FinderInteraction.FinderMethod getFinderMethod() {
        return finderMethod;
    }
    public Class<?> getResultType() {
        return queryDefault.getResultType();
    }
    public String getQueryName() {
        return queryDefault.getQueryName();
    }
    public Map<String, Object> getArgumentsByParameterName() {
        return queryDefault.getArgumentsByParameterName();
    }
    public int numArgs() {
        return queryDefault.getArgumentsByParameterName().size();
    }
}