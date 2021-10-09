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
package org.apache.isis.viewer.wicket.ui.panels;

import java.util.Optional;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.springframework.lang.Nullable;

import org.apache.isis.applib.services.exceprecog.Category;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizerService;
import org.apache.isis.applib.services.exceprecog.Recognition;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal.base._Either;
import org.apache.isis.core.metamodel.spec.ManagedObjects.EntityUtil;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.FormExecutor;
import org.apache.isis.viewer.wicket.model.models.ScalarPropertyModel;
import org.apache.isis.viewer.wicket.ui.actionresponse.ActionResultResponse;
import org.apache.isis.viewer.wicket.ui.actionresponse.ActionResultResponseType;
import org.apache.isis.viewer.wicket.ui.pages.entity.EntityPage;

import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public final class FormExecutorDefault
implements FormExecutor {

    private static final long serialVersionUID = 1L;

    protected final WicketViewerSettings settings;
    private final _Either<ActionModel, ScalarPropertyModel> actionOrPropertyModel;

    public FormExecutorDefault(
            final _Either<ActionModel, ScalarPropertyModel> actionOrPropertyModel) {
        this.actionOrPropertyModel = actionOrPropertyModel;
        this.settings = getSettings();
    }

    /**
     * @return <tt>false</tt> - if invalid args;
     * <tt>true</tt> if redirecting to new page, or repainting all components
     */
    @Override
    public boolean executeAndProcessResults(
            final Page page,
            final AjaxRequestTarget ajaxTarget,
            final Form<?> feedbackFormIfAny,
            final boolean withinPrompt) {

        try {

            final Optional<Recognition> invalidReasonIfAny = getReasonInvalidIfAny();
            if (invalidReasonIfAny.isPresent()) {
                raiseWarning(ajaxTarget, feedbackFormIfAny, invalidReasonIfAny.get());
                return false; // invalid args, stay on page
            }

            //
            // the following line will (attempt to) invoke the action, and will in turn either:
            //
            // 1. return a non-null result from a successful invocation
            //
            // 2. return a null result (from a successful action returning void)
            //
            // 3. throws a RuntimeException, either:
            //    a) as result of application throwing RecoverableException (DN transaction still intact)
            //    b) as result of DB exception, eg uniqueness constraint violation (DN transaction marked to abort)
            //
            // (The DB exception might actually be thrown by the flush() that follows.
            //
            val resultAdapter = actionOrPropertyModel.fold(
                    act->act.executeActionAndReturnResult(),
                    prop->prop.applyValueThenReturnOwner());

            if(log.isDebugEnabled()) {
                log.debug("about to redirect with {} after execution result {}",
                        EntityUtil.getEntityState(resultAdapter),
                        resultAdapter);
            }

            val resultResponse =
            actionOrPropertyModel.fold(
                    act->ActionResultResponseType
                            .determineAndInterpretResult(act, ajaxTarget, resultAdapter, act.snapshotArgs()),
                    prop->ActionResultResponse
                            .toPage(EntityPage.ofAdapter(prop.getCommonContext(), resultAdapter)));

            // redirect unconditionally
            resultResponse
                .getHandlingStrategy()
                .handleResults(getCommonContext(), resultResponse);

            return true; // valid args, allow redirect

        } catch (Throwable ex) {

            // there's no need to set the abort cause on the transaction, it will have already been done
            // (in IsisTransactionManager#executeWithinTransaction(...)).

            // if inline prompt then redirect to error page
            if (withinPrompt) {
                // throwing an exception will get caught by WebRequestCycleForIsis#onException(...)
                throw ex; // redirect to the error page.
            }

            // attempt to recognize this exception using the ExceptionRecognizers
            if(recognizeExceptionThenRaise(ex, ajaxTarget, feedbackFormIfAny).isPresent()) {
                return false; // invalid args, stay on page
            }

            throw ex; // redirect to the error page.

        }
    }

    // -- HELPER

    private Optional<Recognition> recognizeExceptionThenRaise(
            final Throwable ex,
            final AjaxRequestTarget target,
            final Form<?> feedbackForm) {

        val recognition = getExceptionRecognizerService().recognize(ex);
        recognition.ifPresent(recog->raiseWarning(target, feedbackForm, recog));
        return recognition;
    }

    private void raiseWarning(
            final @Nullable AjaxRequestTarget targetIfAny,
            final @Nullable Form<?> feedbackFormIfAny,
            final @NonNull  Recognition recognition) {

        //[ISIS-2419] for a consistent user experience with action dialog validation messages,
        //be less verbose (suppress the category) if its a Category.CONSTRAINT_VIOLATION.
        val errorMsg = recognition.getCategory()==Category.CONSTRAINT_VIOLATION
                ? recognition.toMessageNoCategory(getTranslationService())
                : recognition.toMessage(getTranslationService());

        if(targetIfAny != null && feedbackFormIfAny != null) {
            targetIfAny.add(feedbackFormIfAny);
            feedbackFormIfAny.error(errorMsg);
        } else {
            getMessageService().warnUser(errorMsg);
        }
    }

    private Optional<Recognition> getReasonInvalidIfAny() {
        val reason = actionOrPropertyModel
                .fold(
                        act->act.getValidityConsent().getReason(),
                        prop->prop.getReasonInvalidIfAny());
        return Recognition.of(Category.CONSTRAINT_VIOLATION, reason);
    }

    // -- DEPENDENCIES

    private IsisAppCommonContext getCommonContext() {
        return actionOrPropertyModel
                .fold(
                        act->act.getCommonContext(),
                        prop->prop.getCommonContext());
    }

    private ExceptionRecognizerService getExceptionRecognizerService() {
        return getServiceRegistry().lookupServiceElseFail(ExceptionRecognizerService.class);
    }

    private TranslationService getTranslationService() {
        return getCommonContext().getTranslationService();
    }

    private MessageService getMessageService() {
        return getServiceRegistry().lookupServiceElseFail(MessageService.class);
    }

    private ServiceRegistry getServiceRegistry() {
        return getCommonContext().getServiceRegistry();
    }

    private WicketViewerSettings getSettings() {
        return getCommonContext().lookupServiceElseFail(WicketViewerSettings.class);
    }

}
