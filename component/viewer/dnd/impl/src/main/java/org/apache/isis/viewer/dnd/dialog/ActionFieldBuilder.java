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

package org.apache.isis.viewer.dnd.dialog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewFactory;
import org.apache.isis.viewer.dnd.view.action.ActionContent;
import org.apache.isis.viewer.dnd.view.action.ObjectParameter;
import org.apache.isis.viewer.dnd.view.action.ObjectParameterImpl;
import org.apache.isis.viewer.dnd.view.action.ParameterContent;
import org.apache.isis.viewer.dnd.view.action.TextParseableParameter;
import org.apache.isis.viewer.dnd.view.composite.AbstractViewBuilder;

public class ActionFieldBuilder extends AbstractViewBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(ActionFieldBuilder.class);
    private final ViewFactory subviewDesign;

    public ActionFieldBuilder(final ViewFactory subviewDesign) {
        this.subviewDesign = subviewDesign;
    }

    @Override
    public void build(final View view, final Axes axes) {
        Assert.assertEquals(view.getView(), view);

        final ActionContent actionContent = ((ActionContent) view.getContent());
        if (view.getSubviews().length == 0) {
            initialBuild(view, actionContent);
        } else {
            updateBuild(view, actionContent);
        }

    }

    private View createFieldView(final View view, final ParameterContent parameter, final int sequence) {
        final View fieldView = subviewDesign.createView(parameter, null, sequence);
        if (fieldView == null) {
            throw new IsisException("All parameters must be shown");
        }
        return fieldView;
    }

    private void initialBuild(final View view, final ActionContent actionContent) {
        LOG.debug("build new view " + view + " for " + actionContent);
        final int noParameters = actionContent.getNoParameters();
        View focusOn = null;
        for (int f = 0; f < noParameters; f++) {
            final ParameterContent parameter = actionContent.getParameterContent(f);
            final View fieldView = createFieldView(view, parameter, f);
            final View decoratedSubview = decorateSubview(view.getViewAxes(), fieldView);
            view.addView(decoratedSubview);

            // set focus to first value field
            if (focusOn == null && parameter instanceof TextParseableParameter && fieldView.canFocus()) {
                focusOn = decoratedSubview;
            }
        }

        if (focusOn != null) {
            view.getViewManager().setKeyboardFocus(focusOn);
        }
    }

    private void updateBuild(final View view, final ActionContent actionContent) {
        LOG.debug("rebuild view " + view + " for " + actionContent);
        final View[] subviews = view.getSubviews();

        for (int i = 0; i < subviews.length; i++) {
            final View subview = subviews[i];
            final Content content = subview.getContent();

            final ObjectAdapter subviewsObject = subview.getContent().getAdapter();
            final ObjectAdapter invocationsObject = ((ActionContent) view.getContent()).getParameterObject(i);

            if (content instanceof ObjectParameter) {
                if (subviewsObject != invocationsObject) {
                    final ObjectParameter parameter = new ObjectParameterImpl((ObjectParameterImpl) content, invocationsObject);
                    final View fieldView = createFieldView(view, parameter, i);
                    view.replaceView(subview, decorateSubview(view.getViewAxes(), fieldView));
                }
            } else {
                subview.refresh();
            }
        }
    }
}
