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

import org.apache.isis.viewer.dnd.CompositeViewBuilder;
import org.apache.isis.viewer.dnd.Location;
import org.apache.isis.viewer.dnd.Size;
import org.apache.isis.viewer.dnd.View;
import org.apache.isis.viewer.dnd.core.AbstractBuilderDecorator;

public class GridLayout extends AbstractBuilderDecorator {
	public GridLayout(CompositeViewBuilder design) {
		super(design);
	}
	
	public Size getRequiredSize(View view) {
		int height = 0;
		int width = 0;
        View views[] = view.getSubviews();

        for (int i = 0; i < views.length; i++) {
            View v = views[i];
			Size s = v.getRequiredSize();
			height = Math.max(height, s.getHeight());
			width += s.getWidth();
		}

		return new Size(width, height);
	}    
    
    public boolean isOpen() {
		return true;
	}

    public void layout(View view) {
		int x = 0, y = 0;
        View views[] = view.getSubviews();

        for (int i = 0; i < views.length; i++) {
            View v = views[i];
			Size s = v.getRequiredSize();
			v.setSize(s);
			v.setLocation(new Location(x, y));
			x += s.getWidth();
		}
	}

	
}

