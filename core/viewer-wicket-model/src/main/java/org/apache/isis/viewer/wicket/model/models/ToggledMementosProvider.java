package org.apache.isis.viewer.wicket.model.models;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;

import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;

public interface ToggledMementosProvider extends Serializable {
    List<ObjectAdapterMemento> getToggles();
    void clearToggles(final AjaxRequestTarget target);
}
