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

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;

import com.google.common.collect.Maps;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.stringable.OidStringifier;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionType;
import org.apache.isis.viewer.wicket.model.common.NoResultsHandler;
import org.apache.isis.viewer.wicket.model.common.SelectionHandler;
import org.apache.isis.viewer.wicket.model.mementos.ActionMemento;
import org.apache.isis.viewer.wicket.model.mementos.ActionParameterMemento;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.mementos.PageParameterNames;
import org.apache.isis.viewer.wicket.model.mementos.SpecMemento;
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

    /**
     * Factory; for use directly.
     */
    public static ActionModel create(ObjectAdapterMemento targetAdapter,
            ActionMemento action, Mode mode, SingleResultsMode singleResultsMode) {
        return new ActionModel(targetAdapter, action, mode, singleResultsMode);
    }

    /**
     * Factory; for use by {@link BookmarkablePageLink}s.
     */
    public static ActionModel createForPersistent(
            PageParameters pageParameters, OidStringifier oidStringifier) {
        return new ActionModel(pageParameters, oidStringifier);
    }

    /**
     * Factory method for creating {@link PageParameters}.
     * 
     * see {@link #ActionModel(PageParameters)}
     */
    public static PageParameters createPageParameters(
            final ObjectAdapter adapter, final ObjectAction noAction,
            OidStringifier oidStringifier, ObjectAdapter contextAdapter,
            SingleResultsMode singleResultsMode) {
        PageParameters pageParameters = EntityModel.createPageParameters(
                adapter, oidStringifier);

        String actionType = noAction.getType().name();
        String actionNameParmsId = determineActionId(noAction);

        Mode actionMode = determineActionMode(noAction, contextAdapter);

        PageParameterNames.ACTION_TYPE.addTo(pageParameters, actionType);
        ObjectSpecification actionOnTypeSpec = noAction.getOnType();
        if (actionOnTypeSpec != null) {
            PageParameterNames.ACTION_OWNING_SPEC.addTo(pageParameters,
                    actionOnTypeSpec.getFullName());
        }
        PageParameterNames.ACTION_NAME_PARMS.addTo(pageParameters,
                actionNameParmsId);
        PageParameterNames.ACTION_MODE.addTo(pageParameters, actionMode.name());
        PageParameterNames.ACTION_SINGLE_RESULTS_MODE.addTo(pageParameters,
                singleResultsMode.name());

        addActionParamContextIfPossible(noAction, oidStringifier,
                contextAdapter, pageParameters);
        return pageParameters;
    }

    private static Mode determineActionMode(final ObjectAction noAction,
    		ObjectAdapter contextAdapter) {
        final int parameterCount = noAction.getParameterCount();
        if (parameterCount == 0) {
            return Mode.RESULTS;
        }
        if (parameterCount > 1) {
            return Mode.PARAMETERS;
        }
        // no need to prompt for contributed actions (ie if have a context
        // adapter)
        ObjectActionParameter actionParam = noAction.getParameters().get(0);
        return ActionParams.compatibleWith(contextAdapter, actionParam) ? Mode.RESULTS
                : Mode.PARAMETERS;
    }

    private static void addActionParamContextIfPossible(
            final ObjectAction noAction, OidStringifier oidStringifier,
            ObjectAdapter contextAdapter, PageParameters pageParameters) {
        if (contextAdapter == null) {
            return;
        }
        int i = 0;
        for (ObjectActionParameter actionParam : noAction.getParameters()) {
            if (ActionParams.compatibleWith(contextAdapter, actionParam)) {
                final String oidStr = oidStringifier.enString(contextAdapter
                        .getOid());
                final String oidKeyValue = "" + i + "=" + oidStr;
                PageParameterNames.ACTION_PARAM_CONTEXT.addTo(pageParameters,
                        oidKeyValue);
                return;
            }
            i++;
        }
    }

    private static String determineActionId(final ObjectAction noAction) {
        Identifier identifier = noAction.getIdentifier();
        if (identifier != null) {
            return identifier.toNameParmsIdentityString();
        }
        // fallback (used for action sets)
        return noAction.getId();
    }

    public static Mode determineMode(final ObjectAction action) {
        return action.getParameterCount() > 0 ? Mode.PARAMETERS : Mode.RESULTS;
    }

    private ObjectAdapterMemento targetAdapterMemento;
    private ActionMemento actionMemento;
    private Mode actionMode;
    private SingleResultsMode singleResultsMode;

    private SelectionHandler selectionHandler;
    private NoResultsHandler noResultsHandler;

    /**
     * Lazily populated in {@link #getArgumentModel(ActionParameterMemento)}
     */
    private Map<Integer, ScalarModel> arguments = Maps.newHashMap();
    private ActionExecutor executor;

    private ActionModel(PageParameters pageParameters,
            OidStringifier oidStringifier) {
        this(newObjectAdapterMementoFrom(pageParameters, oidStringifier),
                newActionMementoFrom(pageParameters),
                actionModeFor(pageParameters),
                singleResultsModeFor(pageParameters));

        setContextArgumentIfPossible(pageParameters, oidStringifier);

        // TODO: if #args < param count, then change the actionMode

        setNoResultsHandler(new NoResultsHandler() {
            private static final long serialVersionUID = 1L;

            @Override
            public void onNoResults(Component context) {
                reset();
                context.setResponsePage(context.getPage());
            }
        });
    }

    private static ActionMemento newActionMementoFrom(
            PageParameters pageParameters) {
        return new ActionMemento(actionOwningSpecFor(pageParameters),
                actionTypeFor(pageParameters),
                actionNameParmsFor(pageParameters));
    }

    private static SingleResultsMode singleResultsModeFor(
            PageParameters pageParameters) {
        return SingleResultsMode
                .valueOf(PageParameterNames.ACTION_SINGLE_RESULTS_MODE
                        .getFrom(pageParameters));
    }

    private static Mode actionModeFor(PageParameters pageParameters) {
        return Mode.valueOf(PageParameterNames.ACTION_MODE
                .getFrom(pageParameters));
    }

    private static String actionNameParmsFor(PageParameters pageParameters) {
        return PageParameterNames.ACTION_NAME_PARMS.getFrom(pageParameters);
    }

    private static ObjectActionType actionTypeFor(
            PageParameters pageParameters) {
        return ObjectActionType.valueOf(PageParameterNames.ACTION_TYPE
                .getFrom(pageParameters));
    }

    private static SpecMemento actionOwningSpecFor(PageParameters pageParameters) {
        return SpecMemento.representing(PageParameterNames.ACTION_OWNING_SPEC
                .getFrom(pageParameters));
    }

    private static ObjectAdapterMemento newObjectAdapterMementoFrom(
            PageParameters pageParameters, OidStringifier oidStringifier) {
        return ObjectAdapterMemento.createPersistent(oidFor(pageParameters,
                oidStringifier), objectSpecFor(pageParameters));
    }

    private static SpecMemento objectSpecFor(PageParameters pageParameters) {
        return SpecMemento.representing(PageParameterNames.OBJECT_SPEC
                .getFrom(pageParameters));
    }

    private static Oid oidFor(PageParameters pageParameters,
            OidStringifier oidStringifier) {
        return oidStringifier.deString(PageParameterNames.OBJECT_OID
                .getFrom(pageParameters));
    }

    private ActionModel(final ObjectAdapterMemento adapterMemento,
            final ActionMemento actionMemento, final Mode actionMode,
            final SingleResultsMode singleResultsMode) {
        this.targetAdapterMemento = adapterMemento;
        this.actionMemento = actionMemento;
        this.actionMode = actionMode;
        this.singleResultsMode = singleResultsMode;
    }

    private boolean setContextArgumentIfPossible(PageParameters pageParameters,
            OidStringifier oidStringifier) {
        String paramContext = PageParameterNames.ACTION_PARAM_CONTEXT
                .getFrom(pageParameters);
        if (paramContext == null) {
            return false;
        }
        ObjectAction action = actionMemento.getAction();
        int parameterCount = action.getParameterCount();

        Map.Entry<Integer, String> mapEntry = parse(paramContext);

        Oid oid;
        int paramNum = mapEntry.getKey();
        if (paramNum >= parameterCount) {
            return false;
        }

        try {
            oid = oidStringifier.deString(mapEntry.getValue());
        } catch (Exception e) {
            return false;
        }

        ObjectAdapter argumentAdapter = getAdapterManager().getAdapterFor(oid);
        if (argumentAdapter == null) {
            return false;
        }

        ObjectActionParameter actionParam = action.getParameters().get(paramNum);
        ActionParameterMemento apm = new ActionParameterMemento(actionParam);
        ScalarModel argumentModel = getArgumentModel(apm);
        argumentModel.setObject(argumentAdapter);

        return true;
    }

    public static Entry<Integer, String> parse(String paramContext) {
        Pattern compile = Pattern.compile("([^=]+)=(.+)");
        Matcher matcher = compile.matcher(paramContext);
        if (!matcher.matches()) {
            return null;
        }

        final int paramNum;
        try {
            paramNum = Integer.parseInt(matcher.group(1));
        } catch (Exception e) {
            // ignore
            return null;
        }

        final String oidStr;
        try {
            oidStr = matcher.group(2);
        } catch (Exception e) {
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
            public String setValue(String value) {
                return null;
            }
        };
    }

    public ScalarModel getArgumentModel(ActionParameterMemento apm) {
        ScalarModel scalarModel = arguments.get(apm.getNumber());
        if (scalarModel == null) {
            scalarModel = new ScalarModel(targetAdapterMemento, apm);
            int number = scalarModel.getParameterMemento().getNumber();
            arguments.put(number, scalarModel);
        }
        return scalarModel;
    }

    public ObjectAdapter getTargetAdapter() {
        return targetAdapterMemento.getObjectAdapter();
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
        ObjectAdapter result = super.getObject();
        arguments.clear();
        return result;
    }

    @Override
    protected ObjectAdapter load() {
    	ObjectAdapter results = executeAction();
        this.actionMode = Mode.RESULTS;
        return results;
    }

    private ObjectAdapter executeAction() {
    	ObjectAdapter targetAdapter = getTargetAdapter();
    	ObjectAdapter[] arguments = getArgumentsAsArray();
        ObjectAction action = getActionMemento().getAction();
        ObjectAdapter results = action.execute(targetAdapter, arguments);
        return results;
    }

    public String getReasonInvalidIfAny() {
    	ObjectAdapter targetAdapter = getTargetAdapter();
    	ObjectAdapter[] proposedArguments = getArgumentsAsArray();
        ObjectAction objectAction = getActionMemento().getAction();
        Consent validity = objectAction.isProposedArgumentSetValid(
                targetAdapter, proposedArguments);
        return validity.isAllowed() ? null : validity.getReason();
    }

    @Override
    public void setObject(ObjectAdapter object) {
        throw new UnsupportedOperationException(
                "target adapter for ActionModel cannot be changed");
    }

    public ObjectAdapter[] getArgumentsAsArray() {
        ObjectAction objectAction = getActionMemento().getAction();
        ObjectAdapter[] arguments = new ObjectAdapter[objectAction
                .getParameterCount()];
        for (int i = 0; i < arguments.length; i++) {
            ScalarModel scalarModel = this.arguments.get(i);
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

    public void setSelectionHandler(SelectionHandler selectionHandler) {
        this.selectionHandler = selectionHandler;
    }

    /**
     * The {@link NoResultsHandler}, if any,
     */
    public NoResultsHandler getNoResultsHandler() {
        return noResultsHandler;
    }

    public void setNoResultsHandler(NoResultsHandler noResultsHandler) {
        this.noResultsHandler = noResultsHandler;
    }

    public ActionExecutor getExecutor() {
        return executor;
    }

    public void setExecutor(ActionExecutor executor) {
        this.executor = executor;
    }

    public SingleResultsMode getSingleResultsMode() {
        return singleResultsMode;
    }

    public void reset() {
        this.actionMode = determineMode(actionMemento.getAction());
    }

}
