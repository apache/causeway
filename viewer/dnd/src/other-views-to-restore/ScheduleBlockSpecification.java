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

import org.apache.isis.object.ObjectAdapterRuntimeException;
import org.apache.isis.object.ObjectSpecification;
import org.apache.isis.object.ObjectSpecificationLoader;
import org.apache.isis.object.reflect.ObjectField;
import org.apache.isis.viewer.dnd.Content;
import org.apache.isis.viewer.dnd.ObjectContent;
import org.apache.isis.viewer.dnd.View;
import org.apache.isis.viewer.dnd.ViewAxis;
import org.apache.isis.viewer.dnd.basic.SimpleIdentifier;
import org.apache.isis.viewer.dnd.core.AbstractCompositeViewSpecification;

import org.apache.log4j.Logger;

public class ScheduleBlockSpecification extends AbstractCompositeViewSpecification{
	private static final Logger LOG = Logger.getLogger(ScheduleBlockView.class);

	public View createView(Content content, ViewAxis axis) {
    	ObjectSpecification nc = ((ObjectContent) content).getObject().getSpecification();
    	ObjectField[] flds = nc.getFields();
    	ObjectField timePeriodField = null;
    	ObjectField colorField = null;
    	for (int i = 0; i < flds.length; i++) {
			ObjectField field = flds[i];
			if(field.getType().isOfType(Isis.getSpecificationLoader().loadSpecification(TimePeriod.class))) {
				LOG.debug("found TimePeriod field " + field);
				timePeriodField = field;
			}
			if(field.getType().isOfType(Isis.getSpecificationLoader().loadSpecification(org.apache.isis.application.value.Color.class))) {
				LOG.debug("found Color field " + field);
				colorField = field;
			}
		}
    	if(timePeriodField == null) {
        	throw new ObjectAdapterRuntimeException("Can't create Shedule view without a TimePeriod");
    	} else {
    		return new SimpleIdentifier(new ScheduleBlockView(content, this, axis, timePeriodField, colorField));
    	}
	}
	
	public String getName() {
		return "Schedule Block";
	}
}


/*
[[NAME]] - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  [[NAME]] Group Ltd

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

The authors can be contacted via isis.apache.org (the
registered address of [[NAME]] Group is Kingsway House, 123 Goldworth
Road, Woking GU21 1NR, UK).
*/