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
package org.apache.isis.viewer.wicket.ui.panels;

import org.apache.isis.viewer.wicket.ui.components.layout.bs3.col.Col;

/**
 * Implemented by all Wicket UI components that contain other content which may or
 * may not be visible, eg per user, or per imperative hideXxx() method.
 *
 * <p>
 *     The {@link Col}, {@link org.apache.isis.viewer.wicket.ui.components.layout.bs3.row.Row} etc components that implement this interface cascade their visibility up to their parent component; so for example if a tabpanel contains a tab that contains a single fieldset that contains a single property, and that property is invisible, then the entire tab is invisible.
 * </p>
 */
public interface HasDynamicallyVisibleContent {

    boolean isVisible();

}
