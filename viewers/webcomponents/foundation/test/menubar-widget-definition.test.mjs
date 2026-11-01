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
const {CausewayMenubarControlElement, configureCausewayMenubarWidgets, useCausewayMenubarWidget} = await import('../src/menubar-widget.mjs');

const projection = projectCausewayMenuBar({
  role: 'primary',
  menus: [{label: 'Administration', sections: [{actions: [{serviceLogicalTypeName: 'demo.Admin', actionId: 'run'}]}]}]
}, {generation: 1});

test('missing Menu Bar definition times out with bounded family failure', async () => {
  configureCausewayMenubarWidgets({
    enabled: true,
    moduleUrl: new URL(`./fixtures/fake-vaadin-menubar-empty.mjs?${Date.now()}`, import.meta.url).href,
    definitionTimeoutMs: 10
  });
  const adapter = new CausewayMenubarControlElement();
  adapter.presentation = {projection, activate() {}};
  let failure;
  adapter.addEventListener('causeway-menubar-load-failed', event => { failure = event.detail; });
  document.body.appendChild(adapter);
  await new Promise(resolve => setTimeout(resolve, 20));
  assert.equal(adapter.dataset.widgetState, 'fallback');
  assert.equal(failure.phase, 'definition');
  assert.equal(failure.classification, 'MENUBAR_DEFINITION_UNAVAILABLE');
  assert.equal(useCausewayMenubarWidget(), false);
  document.body.removeChild(adapter);
});
