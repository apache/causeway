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

package org.apache.isis.viewer.wicket.model.models;

import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Maps;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.oid.RootOidDefault;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.common.NoResultsHandler;
import org.apache.isis.viewer.wicket.model.common.SelectionHandler;
import org.apache.isis.viewer.wicket.model.mementos.ActionMemento;
import org.apache.isis.viewer.wicket.model.mementos.ActionParameterMemento;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.mementos.PageParameterNames;
import org.apache.isis.viewer.wicket.model.util.ActionParams;

/**
 * Models an action invocation, either the gathering of arguments for the
 * action's {@link Mode#PARAMETERS parameters}, or the handling of the
 * {@link Mode#RESULTS results} once invoked.
 */
public class ActionModel extends ModelAbstract<ObjectAdapter> {

    private static final long serialVersionUID = 1L;

    /**
     * Whether we are obtaining arguments (eg in a dialog), or displaying the
     * results
     */
    public enum Mode {
        PARAMETERS, RESULTS
    }

    /**
     * How to handle results when only a single result is returned
     */
    public enum SingleResultsMode {
        /**
         * Render a simple link, using "entityLink"
         */
        LINK,
        /**
         * Render the object directly, using "entity"
         */
        INLINE,
        /**
         * Redirect to <tt>EntityPage</tt>.
         */
        REDIRECT,
        /**
         * Select, ie call the provided {@link SelectionHandler}.
         */
        SELECT
    }

    public static ActionModel create(final ObjectAdapterMemento targetAdapter, final ActionMemento action, final Mode mode, final SingleResultsMode singleResultsMode) {
        return new ActionModel(targetAdapter, action, mode, singleResultsMode);
    }

    public static ActionModel createForPersistent(final PageParameters pageParameters) {
        return new ActionModel(pageParameters);
    }

    /**
     * Factory method for creating {@link PageParameters}.
     * 
     * see {@link #ActionModel(PageParameters)}
     */
    public static PageParameters createPageParameters(final ObjectAdapter adapter, final ObjectAction objectAction, final ObjectAdapter contextAdapter, final SingleResultsMode singleResultsMode) {
        final PageParameters pageParameters = EntityModel.createPageParameters(adapter);

        final String actionType = objectAction.getType().name();
        final String actionNameParmsId = determineActionId(objectAction);

        final Mode actionMode = determineActionMode(objectAction, contextAdapter);

        PageParameterNames.ACTION_TYPE.addTo(pageParameters, actionType);
        final ObjectSpecification actionOnTypeSpec = objectAction.getOnType();
        if (actionOnTypeSpec != null) {
            PageParameterNames.ACTION_OWNING_SPEC.addTo(pageParameters, actionOnTypeSpec.getFullIdentifier());
        }

        PageParameterNames.ACTION_NAME_PARMS.addTo(pageParameters, actionNameParmsId);
        PageParameterNames.ACTION_MODE.addTo(pageParameters, actionMode.name());
        PageParameterNames.ACTION_SINGLE_RESULTS_MODE.addTo(pageParameters, singleResultsMode.name());

        addActionParamContextIfPossible(objectAction, contextAdapter, pageParameters);
        return pageParameters;
    }

    private static Mode determineActionMode(final ObjectAction noAction, final ObjectAdapter contextAdapter) {
        final int parameterCount = noAction.getParameterCount();
        if (parameterCount == 0) {
            return Mode.RESULTS;
        }
        if (parameterCount > 1) {
            return Mode.PARAMETERS;
        }
        // no need to prompt for contributed actions (ie if have a context
        // adapter)
        final ObjectActionParameter actionParam = noAction.getParameters().get(0);
        return ActionParams.compatibleWith(contextAdapter, actionParam) ? Mode.RESULTS : Mode.PARAMETERS;
    }


	private static void addActionParamContextIfPossible(final ObjectAction noAction, final ObjectAdapter contextAdapter, final PageParameters pageParameters) {
        if (contextAdapter == null) {
            return;
        }
        int i = 0;
        for (final ObjectActionParameter actionParam : noAction.getParameters()) {
            if (ActionParams.compatibleWith(contextAdapter, actionParam)) {
                final String oidKeyValue = "" + i + "=" + contextAdapter.getOid().enString(getOidMarshaller());
                PageParameterNames.ACTION_PARAM_CONTEXT.addTo(pageParameters, oidKeyValue);
                return;
            }
            i++;
        }
    }

    private static String determineActionId(final ObjectAction noAction) {
        final Identifier identifier = noAction.getIdentifier();
        if (identifier != null) {
            return identifier.toNameParmsIdentityString();
        }
        // fallback (used for action sets)
        return noAction.getId();
    }

    public static Mode determineMode(final ObjectAction action) {
        return action.getParameterCount() > 0 ? Mode.PARAMETERS : Mode.RESULTS;
    }

    private final ObjectAdapterMemento targetAdapterMemento;
    private final ActionMemento actionMemento;
    private Mode actionMode;
    private final SingleResultsMode singleResultsMode;

    private SelectionHandler selectionHandler;
    private NoResultsHandler noResultsHandler;

    /**
     * Lazily populated in {@link #getArgumentModel(ActionParameterMemento)}
     */
    private final Map<Integer, ScalarModel> arguments = Maps.newHashMap();
    private ActionExecutor executor;

    private ActionModel(final PageParameters pageParameters) {
        this(newObjectAdapterMementoFrom(pageParameters), newActionMementoFrom(pageParameters), actionModeFor(pageParameters), singleResultsModeFor(pageParameters));

        setContextArgumentIfPossible(pageParameters);

        // TODO: if #args < param count, then change the actionMode

        setNoResultsHandler(new NoResultsHandler() {
            private static final long serialVersionUID = 1L;

            @Override
            public void onNoResults(final Component context) {
                reset();
                context.setResponsePage(context.getPage());
            }
        });
    }

    private static ActionMemento newActionMementoFrom(final PageParameters pageParameters) {
        return new ActionMemento(actionOwningSpecFor(pageParameters), actionTypeFor(pageParameters), actionNameParmsFor(pageParameters));
    }

    private static SingleResultsMode singleResultsModeFor(final PageParameters pageParameters) {
        return SingleResultsMode.valueOf(PageParameterNames.ACTION_SINGLE_RESULTS_MODE.getFrom(pageParameters));
    }

    private static Mode actionModeFor(final PageParameters pageParameters) {
        return Mode.valueOf(PageParameterNames.ACTION_MODE.getFrom(pageParameters));
    }

    private static String actionNameParmsFor(final PageParameters pageParameters) {
        return PageParameterNames.ACTION_NAME_PARMS.getFrom(pageParameters);
    }

    private static ActionType actionTypeFor(final PageParameters pageParameters) {
        return ActionType.valueOf(PageParameterNames.ACTION_TYPE.getFrom(pageParameters));
    }

    private static ObjectSpecId actionOwningSpecFor(final PageParameters pageParameters) {
        return ObjectSpecId.of(PageParameterNames.ACTION_OWNING_SPEC.getFrom(pageParameters));
    }

    private static ObjectAdapterMemento newObjectAdapterMementoFrom(final PageParameters pageParameters) {
        RootOid oid = oidFor(pageParameters);
        if(oid.isTransient()) {
            //return ObjectAdapterMemento.
            return null;
        } else {
            return ObjectAdapterMemento.createPersistent(oid);
        }
    }

    private static RootOid oidFor(final PageParameters pageParameters) {
        String oidStr = PageParameterNames.OBJECT_OID.getFrom(pageParameters);
        return getOidMarshaller().unmarshal(oidStr, RootOid.class);
    }

    private ActionModel(final ObjectAdapterMemento adapterMemento, final ActionMemento actionMemento, final Mode actionMode, final SingleResultsMode singleResultsMode) {
        this.targetAdapterMemento = adapterMemento;
        this.actionMemento = actionMemento;
        this.actionMode = actionMode;
        this.singleResultsMode = singleResultsMode;
    }

    private boolean setContextArgumentIfPossible(final PageParameters pageParameters) {
        final String paramContext = PageParameterNames.ACTION_PARAM_CONTEXT.getFrom(pageParameters);
        if (paramContext == null) {
            return false;
        }
        final ObjectAction action = actionMemento.getAction();
        final int parameterCount = action.getParameterCount();

        final Map.Entry<Integer, String> mapEntry = parse(paramContext);

        final int paramNum = mapEntry.getKey();
        if (paramNum >= parameterCount) {
            return false;
        }

        RootOid oid;
        try {
            oid = RootOidDefault.deStringEncoded(mapEntry.getValue(), getOidMarshaller());
        } catch (final Exception e) {
            return false;
        }

        final ObjectAdapter argumentAdapter = getAdapterManager().adapterFor(oid);
        if (argumentAdapter == null) {
            return false;
        }

        final ObjectActionParameter actionParam = action.getParameters().get(paramNum);
        final ActionParameterMemento apm = new ActionParameterMemento(actionParam);
        final ScalarModel argumentModel = getArgumentModel(apm);
        argumentModel.setObject(argumentAdapter);

        return true;
    }

    public static Entry<Integer, String> parse(final String paramContext) {
        final Pattern compile = Pattern.compile("([^=]+)=(.+)");
        final Matcher matcher = compile.matcher(paramContext);
        if (!matcher.matches()) {
            return null;
        }

        final int paramNum;
        try {
            paramNum = Integer.parseInt(matcher.group(1));
        } catch (final Exception e) {
            // ignore
            return null;
        }

        final String oidStr;
        try {
            oidStr = matcher.group(2);
        } catch (final Exception e) {
            return null;
        }

        return new Map.Entry<Integer, String>() {

            @Override
            public Integer getKey() {
                return paramNum;
            }

            @Override
            public String getValue() {
                return oidStr;
            }

            @Override
            public String setValue(final String value) {
                return null;
            }
        };
    }

    public ScalarModel getArgumentModel(final ActionParameterMemento apm) {
        ScalarModel scalarModel = arguments.get(apm.getNumber());
        if (scalarModel == null) {
            scalarModel = new ScalarModel(targetAdapterMemento, apm);
            final int number = scalarModel.getParameterMemento().getNumber();
            arguments.put(number, scalarModel);
        }
        return scalarModel;
    }

    public ObjectAdapter getTargetAdapter() {
        return targetAdapterMemento.getObjectAdapter(getConcurrencyChecking());
    }

    protected ConcurrencyChecking getConcurrencyChecking() {
        return actionMemento.getConcurrencyChecking();
    }

    public Mode getActionMode() {
        return actionMode;
    }

    public ActionMemento getActionMemento() {
        return actionMemento;
    }

    @Override
    public ObjectAdapter getObject() {
        detach(); // force re-execute
        final ObjectAdapter result = super.getObject();
        arguments.clear();
        return result;
    }

    @Override
    protected ObjectAdapter load() {
        final ObjectAdapter results = executeAction();
        this.actionMode = Mode.RESULTS;
        return results;
    }

    private ObjectAdapter executeAction() {
        final ObjectAdapter targetAdapter = getTargetAdapter();
        final ObjectAdapter[] arguments = getArgumentsAsArray();
        final ObjectAction action = getActionMemento().getAction();
        final ObjectAdapter results = action.execute(targetAdapter, arguments);
        return results;
    }

    public String getReasonInvalidIfAny() {
        final ObjectAdapter targetAdapter = getTargetAdapter();
        final ObjectAdapter[] proposedArguments = getArgumentsAsArray();
        final ObjectAction objectAction = getActionMemento().getAction();
        final Consent validity = objectAction.isProposedArgumentSetValid(targetAdapter, proposedArguments);
        return validity.isAllowed() ? null : validity.getReason();
    }

    @Override
    public void setObject(final ObjectAdapter object) {
        throw new UnsupportedOperationException("target adapter for ActionModel cannot be changed");
    }

    public ObjectAdapter[] getArgumentsAsArray() {
        final ObjectAction objectAction = getActionMemento().getAction();
        final ObjectAdapter[] arguments = new ObjectAdapter[objectAction.getParameterCount()];
        for (int i = 0; i < arguments.length; i++) {
            final ScalarModel scalarModel = this.arguments.get(i);
            arguments[i] = scalarModel.getObject();
        }
        return arguments;
    }

    /**
     * The {@link SelectionHandler}, if any.
     * 
     * <p>
     * If specified, then
     * {@link EntityCollectionModel#setSelectionHandler(SelectionHandler)
     * propogated} if the results is a {@link EntityCollectionModel collection},
     * or used directly if the results is an {@link EntityModel}.
     */
    public SelectionHandler getSelectionHandler() {
        return selectionHandler;
    }

    public void setSelectionHandler(final SelectionHandler selectionHandler) {
        this.selectionHandler = selectionHandler;
    }

    /**
     * The {@link NoResultsHandler}, if any,
     */
    public NoResultsHandler getNoResultsHandler() {
        return noResultsHandler;
    }

    public void setNoResultsHandler(final NoResultsHandler noResultsHandler) {
        this.noResultsHandler = noResultsHandler;
    }

    public ActionExecutor getExecutor() {
        return executor;
    }

    public void setExecutor(final ActionExecutor executor) {
        this.executor = executor;
    }

    public SingleResultsMode getSingleResultsMode() {
        return singleResultsMode;
    }

    public void reset() {
        this.actionMode = determineMode(actionMemento.getAction());
    }

    
    //////////////////////////////////////////////////
    // Dependencies (from context)
    //////////////////////////////////////////////////
    
    protected static OidMarshaller getOidMarshaller() {
        return IsisContext.getOidMarshaller();
    }


}
