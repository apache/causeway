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

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.model.models.ValueModel;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.ComponentFactoryAbstract;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

/**
 * {@link ComponentFactory} for {@link TreePanel}.
 */
public class TreePanelFactory extends ComponentFactoryAbstract {

    private static final long serialVersionUID = 1L;
    
    public TreePanelFactory(ComponentType componentType) {
        super(componentType, TreePanel.class);
    }

    @Override
    public ApplicationAdvice appliesTo(final IModel<?> model) {
    	switch (getComponentType()) {
		case SCALAR_NAME_AND_VALUE:
			return appliesToIfScalar(model);
		case VALUE:
			return appliesToIfValue(model);
		default:
			return ApplicationAdvice.DOES_NOT_APPLY;
		}
    }
    
    @Override
    public final Component createComponent(final String id, final IModel<?> model) {
    	
    	switch (getComponentType()) {
		case SCALAR_NAME_AND_VALUE:
			return createComponentIfScalar(id, model);
		case VALUE:
			return createComponentIfValue(id, model);
		default:
			return new Label(id, "TreePanelFactory.createComponent: case not handled '"+getComponentType()+"'");
		}
    }
    
    // -- VARIANTS OF APPLIES TO
    
	private ApplicationAdvice appliesToIfScalar(IModel<?> model) {
		if (!(model instanceof ScalarModel))
            return ApplicationAdvice.DOES_NOT_APPLY;
        
        final ScalarModel scalarModel = (ScalarModel) model;
        
        if(!scalarModel.isScalarTypeSubtypingAnyOf(org.apache.isis.applib.tree.TreeNode.class)) 
            return ApplicationAdvice.DOES_NOT_APPLY;

        return appliesIf( !scalarModel.hasChoices() );
	}
	
    private ApplicationAdvice appliesToIfValue(IModel<?> model) {
    	if (!(model instanceof ValueModel))
            return ApplicationAdvice.DOES_NOT_APPLY;
    	final ValueModel valueModel = (ValueModel) model;
    	final ObjectAdapter adapter = valueModel.getObject();
    	if(adapter==null || adapter.getObject()==null)
    		return ApplicationAdvice.DOES_NOT_APPLY;
    	
    	return appliesIf( adapter.getObject() instanceof org.apache.isis.applib.tree.TreeNode );
	}

    // -- VARIANTS OF CREATE COMPONENT
    
    private final Component createComponentIfScalar(final String id, final IModel<?> model) {
    	
    	System.out.println("!!! BINGO createComponentIfScalar");
    	
        return new TreePanel(id, (ScalarModel) model);
    }
    
    private final Component createComponentIfValue(final String id, final IModel<?> model) {
    	
    	System.out.println("!!! BINGO createComponentIfValue");
    	
        return new StandaloneTreePanel(id, (ValueModel) model);
    }
    

}
