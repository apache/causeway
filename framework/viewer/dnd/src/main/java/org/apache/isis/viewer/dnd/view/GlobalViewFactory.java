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

package org.apache.isis.viewer.dnd.view;

import java.util.Enumeration;

import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.viewer.dnd.drawing.Location;

/*
 * TODO this factory should always create views, not provide specifications; alternatively, this should be
 * called something else and always return the specification The caller would then need to call the create
 * method to create the object. The only case that would be slightly different would be the DragOutline one as
 * the Axis is never used.
 */
public interface GlobalViewFactory extends DebuggableWithTitle {

    void addSpecification(ViewSpecification spec);

    Enumeration<ViewSpecification> availableViews(ViewRequirement viewRequirement);

    Enumeration<ViewSpecification> availableDesigns(ViewRequirement viewRequirement);

    View createDragViewOutline(View view);

    DragEvent createDragContentOutline(View view, Location location);

    View createMinimizedView(View view);

    View createDialog(Content content);

    View createView(ViewRequirement requirement);
}
