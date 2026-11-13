/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0.
 */

if (globalThis.customElements && !customElements.get('vaadin-menu-bar')) {
  customElements.define('vaadin-menu-bar', class extends (globalThis.HTMLElement ?? class {}) {
    constructor() {
      super();
      this.items = [];
      this.updateComplete = Promise.reject(new Error('private rendering failure'));
    }
  });
}
