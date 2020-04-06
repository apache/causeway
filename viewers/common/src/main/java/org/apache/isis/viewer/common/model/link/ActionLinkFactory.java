package org.apache.isis.viewer.common.model.link;

import org.apache.isis.core.metamodel.spec.feature.ObjectAction;

/**
 * 
 * @since Apr 6, 2020
 *
 * @param <T> - link component type, native to the viewer 
 */
public interface ActionLinkFactory<T> {

    LinkAndLabelUiModel<T> newLink(ObjectAction objectAction);
}