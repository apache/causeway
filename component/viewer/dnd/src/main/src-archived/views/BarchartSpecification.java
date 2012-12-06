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


package org.apache.isis.viewer.dnd.special;

import org.apache.isis.object.ObjectAdapter;
import org.apache.isis.object.CollectionAdapter;
import org.apache.isis.viewer.dnd.Content;
import org.apache.isis.viewer.dnd.View;
import org.apache.isis.viewer.dnd.ViewAxis;
import org.apache.isis.viewer.dnd.ViewSpecification;
import org.apache.isis.viewer.dnd.basic.WindowDecorator;
import org.apache.isis.viewer.dnd.core.AbstractCompositeViewSpecification;

public class BarchartSpecification extends AbstractCompositeViewSpecification implements SubviewSpec {
	private ViewSpecification specification = new BarSpecification();

	public BarchartSpecification() {
		builder = new WindowDecorator(new StackLayout(new CollectionElementBuilder(this, false)));
	}
	
	public View createSubview(Content content, ViewAxis axis) {
		return specification.createView(content, axis);
	}

	public String getName() {
		return "Percentage Barchart";
	}
	
	public boolean canDisplay(ObjectAdapter object) {
		return object instanceof CollectionAdapter;
	}
}


