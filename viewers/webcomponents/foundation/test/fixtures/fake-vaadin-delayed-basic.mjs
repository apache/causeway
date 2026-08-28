/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */

if (globalThis.causewayDelayedBasicModulePromise) {
  await globalThis.causewayDelayedBasicModulePromise;
}

class FakeDelayedTextField extends (globalThis.HTMLElement ?? class {}) {
  constructor() {
    super();
    this.value = '';
    this.clearButtonVisible = false;
    this.accessibleNameRef = '';
    this.inputElement = {setAttribute() {}, inputMode: ''};
    this.updateComplete = Promise.resolve();
  }
}
if (globalThis.customElements && !customElements.get('vaadin-delayed-text-field')) {
  customElements.define('vaadin-delayed-text-field', FakeDelayedTextField);
}
