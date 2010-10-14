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


package org.apache.isis.extensions.html.task;

import java.util.List;

import org.apache.isis.commons.debug.DebugString;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.consent.Consent;
import org.apache.isis.metamodel.spec.feature.ObjectAction;
import org.apache.isis.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.metamodel.spec.feature.ParseableEntryActionParameter;
import org.apache.isis.extensions.html.component.Page;
import org.apache.isis.extensions.html.context.Context;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.transaction.messagebroker.MessageBroker;



public final class MethodTask extends Task {
    private final ObjectAction action;

    protected MethodTask(final Context context, final ObjectAdapter target, final ObjectAction action) {
        super(context, action.getName(), action.getDescription(), target, action.getParameterCount());
        this.action = action;

        final ObjectActionParameter[] parameters = action.getParameters();
        final int len = parameters.length;

        for (int i = 0; i < len; i++) {
            names[i] = parameters[i].getName();
            descriptions[i] = parameters[i].getDescription();
            fieldSpecifications[i] = parameters[i].getSpecification();
            optional[i] = parameters[i].isOptional();

            if (parameters[i].getSpecification().isParseable()) {
                final ParseableEntryActionParameter valueParameter = (ParseableEntryActionParameter) parameters[i];
                noLines[i] = valueParameter.getNoLines();
                wraps[i] = valueParameter.canWrap();
                maxLength[i] = valueParameter.getMaximumLength();
                typicalLength[i] = valueParameter.getTypicalLineLength();
            }

        }

        // String[] names = action.getParameterNames();
        // String[] descriptions = action.getParameterDescriptions();
        // ObjectSpecification[] types = action.getParameterTypes();
        final ObjectAdapter[] defaultParameterValues = action.getDefaults(target);
        // boolean[] optional = action.getOptionalParameters();
        for (int i = 0; i < names.length; i++) {
            // this.names[i] = names[i];
            // this.descriptions[i] = descriptions[i];
            // this.fieldSpecifications[i] = types[i];
            // this.optional[i] = optional[i];
            if (defaultParameterValues[i] == null) {
                // TODO use new promptForParameters method instead of all this
                if (action.isContributed()) {
                    initialState[i] = target;
                } else {
                    initialState[i] = null;
                }
            } else {
                initialState[i] = defaultParameterValues[i];
            }
            /*
             * noLines[i] = action.getParameterNoLines()[i]; wraps[i] = action.canParametersWrap()[i];
             * maxLength[i] = action.getParameterMaxLengths()[i]; typicalLength[i] =
             * action.getParameterTypicalLengths()[i];
             */
        }

    }

    @Override
    public void checkForValidity(final Context context) {
        final ObjectAdapter[] parameters = getEntries(context);
        final ObjectAdapter target = getTarget(context);
        final Consent consent = action.isProposedArgumentSetValid(target, parameters);
        error = consent.getReason();
    }

    @Override
    public ObjectAdapter completeTask(final Context context, final Page page) {
        final ObjectAdapter[] parameters = getEntries(context);
        final ObjectAdapter target = getTarget(context);
        final ObjectAdapter result = action.execute(target, parameters);
        final MessageBroker broker = IsisContext.getMessageBroker();
        final List<String> messages = broker.getMessages();
        final List<String> warnings = broker.getWarnings();
        context.setMessagesAndWarnings(messages, warnings);
        return result;
    }

    @Override
    public void debug(final DebugString debug) {
        debug.appendln("action: " + action);
        super.debug(debug);
    }

    @Override
    protected ObjectAdapter[][] getOptions(final Context context, final int from, final int len) {
        final ObjectAdapter[][] allOptions = action.getChoices(getTarget(context));
        final ObjectAdapter[][] options = new ObjectAdapter[len][];
        for (int i = from, j = 0; j < len; i++, j++) {
            options[j] = allOptions[i];
        }
        return options;
    }

    public boolean collectParameters() {
        // TODO use new promptForParameters method instead of all this

        final int expectedNoParameters = action.isContributed() ? 1 : 0;
        return action.getParameterCount() == expectedNoParameters;
    }
}

