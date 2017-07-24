package org.apache.isis.viewer.wicket.ui.components.actions;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.ActionPrompt;
import org.apache.isis.viewer.wicket.model.models.BookmarkedPagesModel;
import org.apache.isis.viewer.wicket.ui.actionresponse.ActionResultResponse;
import org.apache.isis.viewer.wicket.ui.actionresponse.ActionResultResponseType;
import org.apache.isis.viewer.wicket.ui.pages.BookmarkedPagesModelProvider;
import org.apache.isis.viewer.wicket.ui.panels.FormExecutorAbstract;

public class ActionParametersFormExecutor implements FormExecutorAbstract.FormExecutorStrategy {

    private final ActionModel model;

    public ActionParametersFormExecutor(final ActionModel actionModel) {
        model = actionModel;
    }


    public ObjectAdapter obtainTargetAdapter() {
        return model.getTargetAdapter();
    }

    public String getReasonInvalidIfAny() {
        return model.getReasonInvalidIfAny();
    }

    public void onExecuteAndProcessResults(final AjaxRequestTarget target) {

        if (model.isBookmarkable()) {
            BookmarkedPagesModelProvider application = (BookmarkedPagesModelProvider) Session.get();
            BookmarkedPagesModel bookmarkedPagesModel = application.getBookmarkedPagesModel();
            bookmarkedPagesModel.bookmarkPage(model);
        }

        if (actionPrompt != null) {
            actionPrompt.closePrompt(target);
            // cos will be reused next time, so mustn't cache em.
            model.clearArguments();
        }
    }

    public ObjectAdapter obtainResultAdapter() {
        return model.execute();
    }


    public void redirectTo(
            final ObjectAdapter resultAdapter,
            final AjaxRequestTarget targetIfany) {
        ActionResultResponse resultResponse = ActionResultResponseType
                .determineAndInterpretResult(model, targetIfany, resultAdapter);
        resultResponse.getHandlingStrategy().handleResults(resultResponse, getIsisSessionFactory());
    }


    ///////////////////////////////////////////////////////
    // Dependencies (from context)
    ///////////////////////////////////////////////////////

    private ActionPrompt actionPrompt;

    void setActionPrompt(final ActionPrompt actionPrompt) {
        this.actionPrompt = actionPrompt;
    }


    protected IsisSessionFactory getIsisSessionFactory() {
        return IsisContext.getSessionFactory();
    }

}
