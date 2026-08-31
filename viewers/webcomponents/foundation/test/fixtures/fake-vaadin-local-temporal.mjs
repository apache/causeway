/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */

function internalControl(localName, picker) {
  const field = document.createElement('div');
  field.localName = localName;
  field.value = '';
  field.step = 0.001;
  field.disabled = false;
  field.readOnly = false;
  field.inputElement = document.createElement('input');
  field.focusElement = field.inputElement;
  const trigger = document.createElement('div');
  field._trigger = trigger;
  trigger.setAttribute('part', 'field-button toggle-button');
  trigger.setAttribute('aria-hidden', 'true');
  trigger.click = () => { field.opened = !field.opened; };
  field.shadowRoot = {
    querySelector: selector => selector === '[part~="toggle-button"]' ? trigger : null,
    querySelectorAll: () => []
  };
  field.querySelectorAll = () => [];
  field.updateComplete = Promise.resolve();
  picker?.push(field);
  return field;
}

class FakeTemporalControl extends (globalThis.HTMLElement ?? class {}) {
  constructor() {
    super();
    this.value = '';
    this.step = 0.001;
    this.clearButtonVisible = false;
    this.accessibleNameRef = '';
    this.inputElement = document.createElement('input');
    this.focusElement = this.inputElement;
    this._trigger = document.createElement('div');
    this._trigger.setAttribute('part', 'field-button toggle-button');
    this._trigger.setAttribute('aria-hidden', 'true');
    this._trigger.click = () => { this.opened = !this.opened; };
    this.shadowRoot = {
      querySelector: selector => selector === '[part~="toggle-button"]' ? this._trigger : null,
      querySelectorAll: () => []
    };
    this.updateComplete = Promise.resolve();
  }

  querySelectorAll() {
    return [];
  }
}

class FakeDateTimePicker extends FakeTemporalControl {
  constructor() {
    super();
    this._fields = [];
    this._datePicker = internalControl('vaadin-date-picker', this._fields);
    this._timePicker = internalControl('vaadin-time-picker', this._fields);
    this.shadowRoot = {
      querySelector: () => null,
      querySelectorAll: selector => this._fields.filter(field => field.localName === selector)
    };
  }
}

if (globalThis.customElements) {
  if (!customElements.get('vaadin-time-picker')) customElements.define('vaadin-time-picker', FakeTemporalControl);
  if (!customElements.get('vaadin-date-time-picker')) customElements.define('vaadin-date-time-picker', FakeDateTimePicker);
}
