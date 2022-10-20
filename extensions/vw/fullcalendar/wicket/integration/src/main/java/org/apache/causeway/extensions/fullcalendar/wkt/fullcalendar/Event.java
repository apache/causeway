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

package org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar;

import java.io.Serializable;

import org.joda.time.DateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain = true)
@ToString
public class Event implements Serializable {

    private static final long serialVersionUID = 1L;

	private String id;
	private String title;
	private boolean allDay = false;
	private DateTime start;
	private DateTime end;
	private String url;
	private String className;
	private Boolean editable;
	private String color;
	private String backgroundColor;
	private String borderColor;
	private String textColor;
	private Serializable payload;

}
