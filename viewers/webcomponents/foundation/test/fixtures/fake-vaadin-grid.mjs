/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0.
 */

class FakeVaadinGrid extends (globalThis.HTMLElement ?? class {}) {
  constructor() {
    super();
    this.items = undefined;
    this.dataProvider = undefined;
    this.pageSize = 0;
    this.size = 0;
    this.clearCacheCount = 0;
    this.updateComplete = Promise.resolve();
  }

  clearCache() {
    this.clearCacheCount += 1;
  }
}

class FakeVaadinGridColumn extends (globalThis.HTMLElement ?? class {}) {
  constructor() {
    super();
    this.header = '';
    this.renderer = null;
  }
}

if (globalThis.customElements && !customElements.get('vaadin-grid')) {
  customElements.define('vaadin-grid', FakeVaadinGrid);
}
if (globalThis.customElements && !customElements.get('vaadin-grid-column')) {
  customElements.define('vaadin-grid-column', FakeVaadinGridColumn);
}
