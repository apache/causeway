/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */

class FakeVaadinControl extends (globalThis.HTMLElement ?? class {}) {
  constructor() {
    super();
    this.value = '';
    this.checked = false;
    this.items = [];
    this.clearButtonVisible = false;
    this.accessibleNameRef = '';
    const inputAttributes = new Map();
    this.inputElement = {
      inputMode: '',
      selectionStart: 0,
      selectionEnd: 0,
      setSelectionRange() {},
      setAttribute(name, value) { inputAttributes.set(name, String(value)); },
      getAttribute(name) { return inputAttributes.get(name) ?? null; }
    };
  }
}
if (globalThis.customElements) {
  for (const name of ['vaadin-text-field', 'vaadin-text-area', 'vaadin-password-field', 'vaadin-checkbox', 'vaadin-select']) {
    if (!customElements.get(name)) customElements.define(name, FakeVaadinControl);
  }
}
