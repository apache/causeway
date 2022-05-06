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

package org.apache.isis.extensions.fullcalendar.wkt.fullcalendar;

import java.io.Serializable;

import org.joda.time.DateTime;

public class Event implements Serializable {

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

	public String getId() {
		return id;
	}

	public Event setId(final String id) {
		this.id = id;
		return this;
	}

	public String getTitle() {
		return title;
	}

	public Event setTitle(final String title) {
		this.title = title;
		return this;
	}

	public boolean isAllDay() {
		return allDay;
	}

	public Event setAllDay(final boolean allDay) {
		this.allDay = allDay;
		return this;
	}

	public DateTime getStart() {
		return start;
	}

	public Event setStart(final DateTime start) {
		this.start = start;
		return this;
	}

	public DateTime getEnd() {
		return end;
	}

	public Event setEnd(final DateTime end) {
		this.end = end;
		return this;
	}

	public String getUrl() {
		return url;
	}

	public Event setUrl(final String url) {
		this.url = url;
		return this;
	}

	public String getClassName() {
		return className;
	}

	public Event setClassName(final String className) {
		this.className = className;
		return this;
	}

	public Boolean isEditable() {
		return editable;
	}

	public Event setEditable(final Boolean editable) {
		this.editable = editable;
		return this;
	}

	public String getColor() {
		return color;
	}

	public Event setColor(final String color) {
		this.color = color;
		return this;
	}

	public String getBackgroundColor() {
		return backgroundColor;
	}

	public Event setBackgroundColor(final String backgroundColor) {
		this.backgroundColor = backgroundColor;
		return this;
	}

	public String getBorderColor() {
		return borderColor;
	}

	public Event setBorderColor(final String borderColor) {
		this.borderColor = borderColor;
		return this;
	}

	public String getTextColor() {
		return textColor;
	}

	public Event setTextColor(final String textColor) {
		this.textColor = textColor;
		return this;
	}

	public Serializable getPayload() {
		return payload;
	}

	public void setPayload(final Serializable payload) {

		this.payload = payload;
	}

}
