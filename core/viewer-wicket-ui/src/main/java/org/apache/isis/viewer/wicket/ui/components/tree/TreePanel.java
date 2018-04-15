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

package org.apache.isis.viewer.wicket.ui.components.tree;

import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract2;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;

public class TreePanel extends ScalarPanelAbstract2 {

    public TreePanel(String id, ScalarModel scalarModel) {
		super(id, scalarModel);
	}

	private static final long serialVersionUID = 1L;

	@Override
	protected String getScalarPanelType() {
		return "treePanel";
	}

	@Override
	protected InlinePromptConfig getInlinePromptConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected MarkupContainer createComponentForRegular() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Component createComponentForCompact() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Component getScalarValueComponent() {
		// TODO Auto-generated method stub
		return null;
	}
    

}
