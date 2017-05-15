package org.apache.isis.viewer.wicket.ui.components.actions;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.ActionPrompt;
import org.apache.isis.viewer.wicket.model.models.BookmarkableModel;
import org.apache.isis.viewer.wicket.model.models.BookmarkedPagesModel;
import org.apache.isis.viewer.wicket.ui.actionresponse.ActionResultResponse;
import org.apache.isis.viewer.wicket.ui.actionresponse.ActionResultResponseType;
import org.apache.isis.viewer.wicket.ui.pages.BookmarkedPagesModelProvider;
import org.apache.isis.viewer.wicket.ui.panels.FormExecutorAbstract;

public class ActionParametersFormExecutor extends FormExecutorAbstract<ActionModel> {

    public ActionParametersFormExecutor(final ActionModel actionModel) {
        super(actionModel);
    }


    protected ObjectAdapter obtainTargetAdapter() {
        return model.getTargetAdapter();
    }

    protected String getReasonInvalidIfAny() {
        return model.getReasonInvalidIfAny();
    }

    protected void onExecuteAndProcessResults(final AjaxRequestTarget target) {

        if (model.isBookmarkable()) {
            bookmarkPage(model);
        }

        if (actionPrompt != null) {
            actionPrompt.closePrompt(target);
            // cos will be reused next time, so mustn't cache em.
            model.clearArguments();
        }
    }

    protected ObjectAdapter obtainResultAdapter() {
        return model.executeHandlingApplicationExceptions();
    }

    protected void forwardOnConcurrencyException(
            final ObjectAdapter targetAdapter,
            final ConcurrencyException ex) {
        ActionResultResponse resultResponse = ActionResultResponseType.OBJECT
                .interpretResult(model, targetAdapter, ex);
        resultResponse.getHandlingStrategy().handleResults(resultResponse, getIsisSessionFactory());
    }

    protected void forwardOntoResult(
            final ObjectAdapter resultAdapter,
            final AjaxRequestTarget target) {
        ActionResultResponse resultResponse = ActionResultResponseType
                .determineAndInterpretResult(model, target, resultAdapter);
        resultResponse.getHandlingStrategy().handleResults(resultResponse, getIsisSessionFactory());
    }

    ///////////////////////////////////////////////////////

    protected void bookmarkPage(BookmarkableModel<?> model) {
        getBookmarkedPagesModel().bookmarkPage(model);
    }

    private BookmarkedPagesModel getBookmarkedPagesModel() {
        BookmarkedPagesModelProvider application = (BookmarkedPagesModelProvider) getSession();
        return application.getBookmarkedPagesModel();
    }

    Session getSession() {
        return Session.get();
    }


    ///////////////////////////////////////////////////////
    // Dependencies (from context)
    ///////////////////////////////////////////////////////

    private ActionPrompt actionPrompt;

    void setActionPrompt(final ActionPrompt actionPrompt) {
        this.actionPrompt = actionPrompt;
    }


}
