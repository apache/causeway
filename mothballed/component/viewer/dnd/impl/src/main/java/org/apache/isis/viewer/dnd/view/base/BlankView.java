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

package org.apache.isis.viewer.dnd.view.base;

import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.content.NullContent;

public class BlankView extends AbstractView {
    private final Size size;

    public BlankView() {
        this(new NullContent());
    }

    public BlankView(final Content content) {
        super(content);
        size = new Size(100, 50);
    }

    public BlankView(final Content content, final Size size) {
        super(content);
        this.size = size;
    }

    @Override
    public Size getRequiredSize(final Size availableSpace) {
        final Size requiredSize = new Size(size);
        requiredSize.limitSize(availableSpace);
        return requiredSize;
    }
}
