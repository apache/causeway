/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0.
 */

import assert from 'node:assert/strict';
import test from 'node:test';

import {installDomShim} from './dom-shim.mjs';
import {projectCausewayMenuBar} from '../src/menubar-projection.mjs';

installDomShim();
const {CausewayMenubarControlElement, configureCausewayMenubarWidgets} = await import('../src/menubar-widget.mjs');

const projection = projectCausewayMenuBar({
  role: 'primary',
  menus: [{label: 'Administration', sections: [{actions: [{serviceLogicalTypeName: 'demo.Admin', actionId: 'run'}]}]}]
}, {generation: 1});

test('Menu Bar rendering failure is redacted and falls back', async () => {
  configureCausewayMenubarWidgets({
    enabled: true,
    moduleUrl: new URL('./fixtures/fake-vaadin-menubar-reject.mjs', import.meta.url).href
  });
  const adapter = new CausewayMenubarControlElement();
  adapter.presentation = {projection, activate() {}};
  let failure;
  adapter.addEventListener('causeway-menubar-load-failed', event => { failure = event.detail; });
  document.body.appendChild(adapter);
  await new Promise(resolve => setTimeout(resolve, 0));
  assert.equal(adapter.dataset.widgetState, 'fallback');
  assert.equal(failure.phase, 'adapter');
  assert.equal(failure.classification, 'MENUBAR_ADAPTER_UNAVAILABLE');
  assert.equal(JSON.stringify(failure).includes('private'), false);
  document.body.removeChild(adapter);
});
