/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */

class FakeVaadinButton extends (globalThis.HTMLElement ?? class {}) {
  constructor() {
    super();
    this.disabled = false;
    this.updateComplete = Promise.resolve();
  }
}
if (globalThis.customElements && !customElements.get('vaadin-button')) {
  customElements.define('vaadin-button', FakeVaadinButton);
}
