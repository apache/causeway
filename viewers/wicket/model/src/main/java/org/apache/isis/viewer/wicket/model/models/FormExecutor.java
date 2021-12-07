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

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;

/**
 * Passed through the {@link ActionModelImpl} or {@link ScalarModel}, allowing
 * two different Wicket UI components (eg owning <code>ActionPanel</code> and
 * <code>ActionParametersFormPanel</code> to interact.
 */
public interface FormExecutor extends Serializable {

    enum FormExecutionOutcome {

        /**
         * if invalid arguments or recoverable exception
         */
        FAILURE_RECOVERABLE_SO_STAY_ON_PAGE,

        /**
         * redirect to result page or re-render all UI components
         */
        SUCCESS_AND_REDIRECED_TO_RESULT_PAGE,

        /**
         * do not trigger a full page re-render, when executing eg. a nested dialog
         */
        SUCCESS_IN_NESTED_CONTEXT_SO_STAY_ON_PAGE;

        public boolean isFailure() { return this == FAILURE_RECOVERABLE_SO_STAY_ON_PAGE; }
        public boolean isSuccess() { return !isFailure(); }

    }

    FormExecutionOutcome executeAndProcessResults(
            AjaxRequestTarget targetIfAny,
            Form<?> feedbackFormIfAny,
            FormExecutorContext formExecutorContext);
}
