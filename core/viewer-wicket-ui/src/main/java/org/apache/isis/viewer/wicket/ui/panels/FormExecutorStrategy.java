package org.apache.isis.viewer.wicket.ui.panels;

import org.apache.wicket.ajax.AjaxRequestTarget;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.wicket.model.models.BookmarkableModel;
import org.apache.isis.viewer.wicket.model.models.ParentEntityModelProvider;

public interface FormExecutorStrategy<M extends BookmarkableModel<ObjectAdapter> & ParentEntityModelProvider> {

    M getModel();

    ObjectAdapter obtainTargetAdapter();

    String getReasonInvalidIfAny();

    void onExecuteAndProcessResults(final AjaxRequestTarget target);

    ObjectAdapter obtainResultAdapter();

    void redirectTo(
            final ObjectAdapter resultAdapter,
            final AjaxRequestTarget target);

}
