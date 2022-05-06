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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;

import org.joda.time.LocalTime;

import lombok.ToString;

/**
 * This class represents the configuration of the calendar which will be passed to FullCalendar as JSON object.
 */
//@Getter @Setter
@ToString
public class Config implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Use these to specify calendar column formats */
	public static enum ColumnFormat {
		day, week, month;
	}

	private List<EventSource> eventSources = new ArrayList<EventSource>();
	private ButtonText buttonText = new ButtonText();
	private String loading;
	private Boolean editable;
	private String eventDrop;
	private String eventResize;
	private String eventClick;

	private String viewDisplay;
	private Boolean selectable;
	private Boolean selectHelper;
	/** A callback that will fire after a selection is made */
	private String select;
	private String initialView;
	@JsonProperty
	private Map<ColumnFormat, String> columnFormat = new HashMap<Config.ColumnFormat, String>();

	private LocalTime minTime;
	private LocalTime maxTime;
	private Integer firstHour;
	private Boolean allDaySlot;

	private String timeFormat;

	private String eventRender;

	private Boolean disableDragging;
	private Boolean disableResizing;
	private Integer slotMinutes;
	private Float aspectRatio;

	private boolean weekends = true;
	private int firstDay = 0;

	private String[] monthNames;
	private String[] monthNamesShort;
	private String[] dayNames;
	private String[] dayNamesShort;

	public Config add(final EventSource eventSource) {
		eventSources.add(eventSource);
		return this;
	}

	public Collection<EventSource> getEventSources() {
		return Collections.unmodifiableList(eventSources);
	}

	@JsonRawValue
	public String getEventResize() {
		return eventResize;
	}

	/**
	 * Sets callback url to be used with this event.
	 *
	 * WARNING: see {@link FullCalendar#setupCallbacks()}
	 *
	 * @param eventResize
	 */
	public void setEventResize(final String eventResize) {
		this.eventResize = eventResize;
	}

	@JsonRawValue
	public String getLoading() {
		return loading;
	}

	public void setLoading(final String loading) {
		this.loading = loading;
	}

	public Boolean isEditable() {
		return editable;
	}

	public void setEditable(final Boolean editable) {
		this.editable = editable;
	}

	@JsonRawValue
	public String getEventDrop() {
		return eventDrop;
	}

	/**
	 * Sets callback url to be used with this event.
	 *
	 * WARNING: see {@link FullCalendar#setupCallbacks()}
	 *
	 * @param eventDrop
	 */
	public void setEventDrop(final String eventDrop) {
		this.eventDrop = eventDrop;
	}

	public Boolean isSelectable() {
		return selectable;
	}

	public void setSelectable(final Boolean selectable) {
		this.selectable = selectable;
	}

	public Boolean isSelectHelper() {
		return selectHelper;
	}

	public void setSelectHelper(final Boolean selectHelper) {
		this.selectHelper = selectHelper;
	}

	@JsonRawValue
	public String getSelect() {
		return select;
	}

	/**
	 * Sets callback url to be used with this event.
	 *
	 * WARNING: see {@link FullCalendar#setupCallbacks()}
	 *
	 * @param select
	 */
	public void setSelect(final String select) {
		this.select = select;
	}

	@JsonRawValue
	public String getEventClick() {
		return eventClick;
	}

	/**
	 * Sets callback url to be used with this event.
	 *
	 * WARNING: see {@link FullCalendar#setupCallbacks()}
	 *
	 * @param eventClick
	 */
	public void setEventClick(final String eventClick) {
		this.eventClick = eventClick;
	}

	/**
	 * @return the defaultView
	 */
	public String getInitialView() {
		return initialView;
	}

	/**
	 * See <a href="http://arshaw.com/fullcalendar/docs/views/Available_Views/">http ://arshaw.com/
	 * fullcalendar/docs/views/Available_Views/</a> for the list of possible values.
	 *
	 * @param initialView
	 *            the defaultView to set
	 */
	public void setInitialView(final String initialView) {
		this.initialView = initialView;
	}

	@JsonIgnore
	public String getColumnFormatDay() {
		return columnFormat.get(ColumnFormat.day);
	}

	public void setColumnFormatDay(final String format) {
		columnFormat.put(ColumnFormat.day, format);
	}

	@JsonIgnore
	public String getColumnFormatWeek() {
		return columnFormat.get(ColumnFormat.week);
	}

	public void setColumnFormatWeek(final String format) {
		columnFormat.put(ColumnFormat.week, format);
	}

	@JsonIgnore
	public String getColumnFormatMonth() {
		return columnFormat.get(ColumnFormat.month);
	}

	public void setColumnFormatMonth(final String format) {
		columnFormat.put(ColumnFormat.month, format);
	}

	public ButtonText getButtonText() {
		return buttonText;
	}

	public LocalTime getMinTime() {
		return minTime;
	}

	public void setMinTime(final LocalTime minTime) {
		this.minTime = minTime;
	}

	public LocalTime getMaxTime() {
		return maxTime;
	}

	public void setMaxTime(final LocalTime maxTime) {
		this.maxTime = maxTime;
	}

	public Integer getFirstHour() {
		return firstHour;
	}

	public void setFirstHour(final Integer firstHour) {
		this.firstHour = firstHour;
	}

	public Boolean getAllDaySlot() {
		return allDaySlot;
	}

	public void setAllDaySlot(final Boolean allDaySlot) {
		this.allDaySlot = allDaySlot;
	}

	public String getTimeFormat() {
		return timeFormat;
	}

	public void setTimeFormat(final String timeFormat) {
		this.timeFormat = timeFormat;
	}

	@JsonRawValue
	public String getEventRender() {
		return eventRender;
	}

	public void setEventRender(final String eventRenderer) {
		this.eventRender = eventRenderer;
	}

	public Boolean getDisableDragging() {
		return disableDragging;
	}

	public void setDisableDragging(final Boolean disableDragging) {
		this.disableDragging = disableDragging;
	}

	public Boolean getDisableResizing() {
		return disableResizing;
	}

	public void setDisableResizing(final Boolean disableResizing) {
		this.disableResizing = disableResizing;
	}

	@JsonRawValue
	public String getViewDisplay() {
		return viewDisplay;
	}

	public void setViewDisplay(final String viewDisplay) {
		this.viewDisplay = viewDisplay;
	}

	public void setSlotMinutes(final Integer slotMinutes) {
		this.slotMinutes = slotMinutes;
	}

	public Integer getSlotMinutes() {
		return slotMinutes;
	}

	/**
	 * See <a href="http://arshaw.com/fullcalendar/docs/display/aspectRatio/">http ://arshaw.com/
	 * fullcalendar/docs/display/aspectRatio/</a>
	 *
	 * @param aspectRatio
	 *            the aspectRatio to set
	 */
	public void setAspectRatio(final Float aspectRatio) {
		this.aspectRatio = aspectRatio;
	}

	/**
	 * See <a href="http://arshaw.com/fullcalendar/docs/display/aspectRatio/">http ://arshaw.com/
	 * fullcalendar/docs/display/aspectRatio/</a>
	 *
	 * @return the aspectRatio
	 */
	public Float getAspectRatio() {
		return aspectRatio;
	}

	/**
	 * If <var>weekends</var> is {@code false}, then it will not display weekends. The default value is {@code true}
	 *
	 * @see <a href="http://arshaw.com/fullcalendar/docs/display/weekends/">http ://arshaw.com/
	 *      fullcalendar/docs/display/weekends/</a>
	 * @return whether or not the calendar shows weekends
	 */
	public boolean isWeekends() {
		return weekends;
	}

	/**
	 * If <var>weekends</var> is {@code false}, then it will not display weekends. The default value is {@code true}
	 *
	 * @see <a href="http://arshaw.com/fullcalendar/docs/display/weekends/">http ://arshaw.com/
	 *      fullcalendar/docs/display/weekends/</a>
	 * @param weekends
	 *            whether or not the calendar shows weekends
	 */
	public void setWeekends(final boolean weekends) {
		this.weekends = weekends;
	}

	/**
	 * Get the first day of a week as an {@code int} {@code 0} represent Sunday, {@code 1} represent Monday, etc...
	 * <p>
	 * The default value is {@code 0} Sunday
	 *
	 * @see <a href="http://arshaw.com/fullcalendar/docs/display/firstDay/">http://arshaw.com/
	 *      fullcalendar/docs/display/firstDay/</a>
	 * @return firstDay
	 */
	public int getFirstDay() {
		return firstDay;
	}

	/**
	 * Set the first day of a week as an {@code int} {@code 0} represent Sunday, {@code 1} represent Monday, etc...
	 * <p>
	 * The default value is {@code 0} Sunday
	 *
	 * @see <a href="http://arshaw.com/fullcalendar/docs/display/firstDay/">http://arshaw.com/
	 *      fullcalendar/docs/display/firstDay/</a>
	 * @param firstDay
	 *            is the first day of a week represented by an {@code int}
	 */
	public void setFirstDay(final int firstDay) {
		this.firstDay = firstDay;
	}

	/**
	 * Override month names depending on your {@code Locale}
	 * <p>
	 * Use {@link #setMonthNames(String[])} to override month names.
	 *
	 * @see <a href="http://arshaw.com/fullcalendar/docs/text/monthNames/">http://arshaw.com/
	 *      fullcalendar/docs/text/monthNames</a>
	 * @return monthNames
	 */
	public String[] getMonthNames() {
		return monthNames;
	}

	/**
	 * Override month names depending on your {@code Locale}
	 * <p>
	 * It overrides all month names, do not forget one or it will print {@code null}
	 * <p>
	 * This is an example to set month names in French:
	 *
	 * setMonthNames(new String[]{"Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet", "Août", "Septembre",
	 * "Octobre", "Novembre", "Décembre"});
	 *
	 * @see <a href="http://arshaw.com/fullcalendar/docs/text/monthNames/">http://arshaw.com/
	 *      fullcalendar/docs/text/monthNames</a>
	 * @param monthNames
	 *            month names to override
	 */
	public void setMonthNames(final String[] monthNames) {
		this.monthNames = monthNames;
	}

	/**
	 * Override month short names depending on your {@code Locale}
	 * <p>
	 * Use {@link #setMonthNamesShort(String[])} to override month short names.
	 *
	 * @see <a href="http://arshaw.com/fullcalendar/docs/text/monthNamesShort/">http://arshaw.com/
	 *      fullcalendar/docs/text/monthNamesShort</a>
	 * @return monthNamesShort
	 */
	public String[] getMonthNamesShort() {
		return monthNamesShort;
	}

	/**
	 * Override month short names depending on your {@code Locale}
	 * <p>
	 * It overrides all month short names, do not forget one or it will print {@code null}
	 * <p>
	 * This is an example to set month short names in French: setMonthNamesShort(new String[]{"Janv.", "Fév.", "Mars",
	 * "Avr.", "Mai", "Juin", "Juil.", "Août", "Sept.", "Oct.", "Nov.", "Déc."});
	 *
	 * @see <a href="http://arshaw.com/fullcalendar/docs/text/monthNamesShort/">http://arshaw.com/
	 *      fullcalendar/docs/text/monthNamesShort</a>
	 * @param monthNamesShort
	 *            month short names to override
	 */
	public void setMonthNamesShort(final String[] monthNamesShort) {
		this.monthNamesShort = monthNamesShort;
	}

	/**
	 * Override day names depending on your {@code Locale}
	 * <p>
	 * Use {@link #setDayNames(String[])} to override day names.
	 *
	 * @see <a href="http://arshaw.com/fullcalendar/docs/text/dayNames/">http://arshaw.com/
	 *      fullcalendar/docs/text/dayNames</a>
	 * @return dayNames
	 */
	public String[] getDayNames() {
		return dayNames;
	}

	/**
	 * Override day names depending on your {@code Locale}
	 * <p>
	 * It overrides all day names, do not forget one or it will print {@code null}
	 * <p>
	 * This is an example to set day names in French: setDayNames(new String[]{"Dimanche", "Lundi", "Mardi", "Mercredi",
	 * "Jeudi", "Vendredi", "Samedi"});
	 *
	 * @see <a href="http://arshaw.com/fullcalendar/docs/text/dayNames/">http://arshaw.com/
	 *      fullcalendar/docs/text/dayNames</a>
	 * @param dayNames
	 *            day names to override
	 */
	public void setDayNames(final String[] dayNames) {
		this.dayNames = dayNames;
	}

	/**
	 * Override day short names depending on your {@code Locale}
	 * <p>
	 * Use {@link #setDayNamesShort(String[])} to override day short names.
	 *
	 * @see <a href="http://arshaw.com/fullcalendar/docs/text/dayNamesShort/">http://arshaw.com/
	 *      fullcalendar/docs/text/dayNamesShort</a>
	 * @return dayNamesShort
	 */
	public String[] getDayNamesShort() {
		return dayNamesShort;
	}

	/**
	 * Override day short names depending on your {@code Locale}
	 * <p>
	 * It overrides all day short names, do not forget one or it will print {@code null}
	 * <p>
	 * This is an example to set day short names in French: setDayNamesShort(new String[]{"Dim", "Lun", "Mar", "Mer",
	 * "Jeu", "ven", "Sam"});
	 *
	 * @see <a href="http://arshaw.com/fullcalendar/docs/text/dayNamesShort/">http://arshaw.com/
	 *      fullcalendar/docs/text/dayNamesShort</a>
	 * @param dayNamesShort
	 *            day short names to override
	 */
	public void setDayNamesShort(final String[] dayNamesShort) {
		this.dayNamesShort = dayNamesShort;
	}
}
