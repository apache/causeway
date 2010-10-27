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


package org.apache.isis.viewer.wicket.ui.util;

import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.basic.Label;

public final class Components {

	private Components() {
	}

	/**
	 * Permanently hides by replacing with a {@link Label} that has an empty string for its caption.
	 */
	public static void permanentlyHide(MarkupContainer container, String... ids) {
		for (String id : ids) {
			permanentlyHideSingle(container, id);
		}
	}

	/**
	 * @see #permanentlyHide(MarkupContainer, String...)
	 */
	public static void permanentlyHide(MarkupContainer container,
			ComponentType... componentIds) {
		for (ComponentType componentType : componentIds) {
			String wicketId = componentType.getWicketId();
			permanentlyHideSingle(container, wicketId);
		}
	}
	
	/**
	 * Not overloaded because - although compiles ok on JDK6u20 (Mac), fails to on JDK6u18 (Ubuntu)
	 */
	private static void permanentlyHideSingle(MarkupContainer container, String id) {
		Label label = new Label(id, "");
		label.setVisible(false);
		container.addOrReplace(label);
	}

	/**
	 * Sets the visibility of the child component(s) within the supplied container.
	 */
	public static void setVisible(MarkupContainer container, boolean visibility, String... ids) {
		for (String id : ids) {
			setVisible(container, visibility, id);
		}
	}

	/**
	 * @see #setVisible(MarkupContainer, boolean, String...)
	 */
	public static void setVisible(MarkupContainer container, boolean visibility, ComponentType... componentTypes) {
		for (ComponentType componentType: componentTypes) {
			String wicketId = componentType.getWicketId();
			setVisible(container, visibility, wicketId);
		}
	}

	private static void setVisible(MarkupContainer container,
			boolean visibility, String wicketId) {
		Component childComponent = container.get(wicketId);
		childComponent.setVisible(visibility);
	}

}
