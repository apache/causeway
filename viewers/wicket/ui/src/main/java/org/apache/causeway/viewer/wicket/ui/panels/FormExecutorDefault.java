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
package org.apache.causeway.viewer.wicket.ui.panels;

import java.util.Optional;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.services.exceprecog.Category;
import org.apache.causeway.applib.services.exceprecog.ExceptionRecognizerService;
import org.apache.causeway.applib.services.exceprecog.Recognition;
import org.apache.causeway.commons.functional.Either;
import org.apache.causeway.viewer.wicket.model.models.ActionModel;
import org.apache.causeway.viewer.wicket.model.models.FormExecutor;
import org.apache.causeway.viewer.wicket.model.models.FormExecutorContext;
import org.apache.causeway.viewer.wicket.model.models.HasCommonContext;
import org.apache.causeway.viewer.wicket.model.models.ScalarPropertyModel;
import org.apache.causeway.viewer.wicket.ui.actionresponse.ActionResultResponse;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

@Log4j2
public final class FormExecutorDefault
implements FormExecutor, HasCommonContext {

    private static final long serialVersionUID = 1L;

    // -- FACTORIES

    public static FormExecutor forAction(final ActionModel actionModel) {
        return new FormExecutorDefault(Either.left(actionModel));
    }

    public static FormExecutor forProperty(final ScalarPropertyModel propertyModel) {
        return new FormExecutorDefault(Either.right(propertyModel));
    }

    public static FormExecutor forMember(final Either<ActionModel, ScalarPropertyModel> actionOrPropertyModel) {
        return new FormExecutorDefault(actionOrPropertyModel);
    }

    // -- CONSTRUCTION

    private final Either<ActionModel, ScalarPropertyModel> actionOrPropertyModel;

    private FormExecutorDefault(
            final Either<ActionModel, ScalarPropertyModel> actionOrPropertyModel) {
        this.actionOrPropertyModel = actionOrPropertyModel;
    }

    /**
     * @return <tt>false</tt> - if invalid args;
     * <tt>true</tt> if redirecting to new page, or repainting all components
     */
    @Override
    public FormExecutionOutcome executeAndProcessResults(
            final AjaxRequestTarget ajaxTarget,
            final Form<?> feedbackFormIfAny,
            final FormExecutorContext formExecutorContext) {

        try {

            final Optional<Recognition> invalidReasonIfAny = Recognition.of(
                    Category.CONSTRAINT_VIOLATION,
                    actionOrPropertyModel
                        .fold(
                                act->act.getValidityConsent().getReasonAsString().orElse(null),
                                prop->prop.getReasonInvalidIfAny()));

            if (invalidReasonIfAny.isPresent()) {
                raiseErrorMessage(ajaxTarget, feedbackFormIfAny, invalidReasonIfAny.get());
                return FormExecutionOutcome.FAILURE_RECOVERABLE_SO_STAY_ON_PAGE; // invalid args, stay on page
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
            //XXX triggers BookmarkedObjectWkt.getObjectAndReAttach() down the call-stack
            //XXX applies the pending property
            var resultAdapter = actionOrPropertyModel.fold(
                    act->act.executeActionAndReturnResult(),
                    prop->prop.applyValueThenReturnOwner());

            // if we are in a nested dialog/form, that supports an action parameter,
            // the result must be fed back into the calling dialog's/form's parameter
            // negotiation model (instead of redirecting to a new page)
            if(formExecutorContext.getAssociatedParameter().isPresent()) {
                formExecutorContext.getAssociatedParameter().get()
                .setValue(resultAdapter);
                return FormExecutionOutcome.SUCCESS_IN_NESTED_CONTEXT_SO_STAY_ON_PAGE;
            }

            //XXX triggers ManagedObject.getBookmarkRefreshed()
            var resultResponse = actionOrPropertyModel.fold(
                    act->ActionResultResponse
                            .determineAndInterpretResult(act, ajaxTarget, resultAdapter),
                    prop->ActionResultResponse
                            .toEntityPage(resultAdapter));

            // redirect using associated strategy
            // XXX note: on property edit, triggers SQL update (on JPA)
            resultResponse
                .getHandlingStrategy()
                .handleResults(resultResponse);

            return FormExecutionOutcome.SUCCESS_AND_REDIRECED_TO_RESULT_PAGE; // success (valid args), allow redirect

        } catch (Throwable ex) {

            // there's no need to set the abort cause on the transaction, it will have already been done
            // (in CausewayTransactionManager#executeWithinTransaction(...)).

            // attempt to recognize this exception using the ExceptionRecognizers (but only when not in inline prompt context!?)
            if(!formExecutorContext.isWithinInlinePrompt()
                    && recognizeExceptionThenRaise(ex, ajaxTarget, feedbackFormIfAny).isPresent()) {
                return FormExecutionOutcome.FAILURE_RECOVERABLE_SO_STAY_ON_PAGE; // invalid args, stay on page
            }

            // throwing an exception will get caught by WebRequestCycleForCauseway#onException(...)
            throw ex; // redirect to the error page.
        }
    }

    // -- HELPER

    private Optional<Recognition> recognizeExceptionThenRaise(
            final Throwable ex,
            final AjaxRequestTarget target,
            final Form<?> feedbackForm) {

        var recognition = getExceptionRecognizerService().recognize(ex);
        recognition.ifPresent(recog->raiseErrorMessage(target, feedbackForm, recog));
        return recognition;
    }

    private void raiseErrorMessage(
            final @Nullable AjaxRequestTarget targetIfAny,
            final @Nullable Form<?> feedbackFormIfAny,
            final @NonNull  Recognition recognition) {

        var errorMsg = recognition.getCategory().isSuppressCategoryInUI()
                ? recognition.toMessageNoCategory(getTranslationService())
                : recognition.toMessage(getTranslationService());

        if(targetIfAny != null && feedbackFormIfAny != null) {
            targetIfAny.add(feedbackFormIfAny);
            feedbackFormIfAny.error(errorMsg);
        } else {
            getMessageService().setError(errorMsg);
        }
    }

    // -- DEPENDENCIES

    private ExceptionRecognizerService getExceptionRecognizerService() {
        return getServiceRegistry().lookupServiceElseFail(ExceptionRecognizerService.class);
    }

}
