/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0.
 */

import assert from 'node:assert/strict';
import test from 'node:test';

import {installDomShim} from './dom-shim.mjs';

const {document} = installDomShim();
const observers = [];
globalThis.getComputedStyle = () => ({fontSize: '16px'});
globalThis.ResizeObserver = class {
  constructor(callback) {
    this.callback = callback;
    this.connected = false;
    observers.push(this);
  }
  observe() {
    this.connected = true;
  }
  disconnect() {
    this.connected = false;
  }
  resize(width) {
    if (this.connected) this.callback([{contentRect: {width}}]);
  }
};

const {
  CausewayCollectionElement,
  configureCausewayGridWidgets
} = await import('../src/index.mjs');

function row(offset = 0) {
  return {
    _meta: {logicalTypeName: 'example.Person', id: `${offset + 1}`, title: `Person ${offset + 1}`},
    name: {get: `Person ${offset + 1}`, hidden: false, disabled: null}
  };
}

function result(offset = 0, totalCount = 30) {
  const rows = [row(offset)];
  const knownUpperBound = totalCount ?? 30;
  return {
    descriptor: {id: 'people'},
    data: {window: {rows}},
    rows,
    window: {
      offset,
      requestedSize: 10,
      returnedCount: 1,
      totalCount,
      countAvailable: totalCount != null,
      maximumSize: 20,
      hasPrevious: offset > 0,
      hasNext: offset + 1 < knownUpperBound,
      previousOffset: offset > 0 ? Math.max(0, offset - 10) : null,
      nextOffset: offset + 1 < knownUpperBound ? offset + 1 : null,
      rangeStart: offset + 1,
      rangeEnd: offset + 1,
      ordering: 'CONFIGURED'
    },
    errors: [],
    rowDescription: {members: new Map([['name', {value: {typeRef: {kind: 'SCALAR', name: 'String', ofType: null}}}]])},
    rowSelection: {_meta: {id: true}, name: {get: true}}
  };
}

async function waitFor(predicate) {
  for (let attempt = 0; attempt < 100; attempt += 1) {
    if (predicate()) return;
    await new Promise(resolve => setTimeout(resolve, 1));
  }
  assert.fail('condition was not reached');
}

test('container observer retains native at 48rem and reuses authoritative rows when widening', async () => {
  configureCausewayGridWidgets({enabled: true});
  const loads = [];
  const context = {
    registerRequirement(_requirement, listener) {
      listener({status: 'ready', descriptor: {id: 'people'}, data: {hidden: false}, errors: [], generation: 1});
      return () => {};
    },
    async loadCollection(options) {
      loads.push(options);
      return result(options.offset ?? 0);
    },
    createHydratedRowContext() {
      return {disconnect() {}};
    }
  };
  const adapter = {presentation: null, restoreSemanticFocus() { return true; }};
  const collection = new CausewayCollectionElement();
  collection.querySelector = selector => selector.includes('cw-collection-grid') ? adapter : null;
  collection.querySelectorAll = () => [];
  collection.id = 'people';
  collection.columns = [{member: 'name', label: 'Name'}];
  collection.active = true;
  collection.context = context;
  document.body.appendChild(collection);
  await waitFor(() => collection.collectionState.status === 'ready' && observers[0]?.connected);
  assert.equal(loads.length, 1);
  assert.equal(collection.gridQualification.reason, 'narrow');

  observers[0].resize(900);
  await Promise.resolve();
  assert.equal(collection.gridQualification.presentation, 'grid-virtual');
  assert.equal(adapter.presentation.mode, 'virtual');
  assert.equal(loads.length, 1);

  const nativeCell = document.createElement('td');
  const nativeLink = document.createElement('cw-object-link');
  nativeCell.setAttribute('data-causeway-grid-row-key', 'example.Person:1');
  nativeCell.setAttribute('data-causeway-grid-member', '_meta');
  nativeCell.setAttribute('data-causeway-grid-role', 'object-link');
  nativeCell.querySelector = () => nativeLink;
  collection.appendChild(nativeCell);
  nativeCell.dispatchEvent(new Event('focusin', {bubbles: true, composed: true}));
  collection.querySelectorAll = () => [nativeCell];

  observers[0].resize(768);
  await Promise.resolve();
  await Promise.resolve();
  assert.equal(collection.gridQualification.reason, 'narrow');
  assert.equal(document.activeElement, nativeLink);
  assert.equal(loads.length, 1);

  observers[0].resize(769);
  observers[0].resize(700);
  observers[0].resize(1000);
  await Promise.resolve();
  assert.equal(collection.gridQualification.presentation, 'grid-virtual');
  assert.equal(loads.length, 1);
  document.body.removeChild(collection);
  assert.equal(observers[0].connected, false);
});

test('bounded pager presents an authoritative total consistently across pages', async () => {
  configureCausewayGridWidgets({enabled: true});
  const loads = [];
  const context = {
    registerRequirement(_requirement, listener) {
      listener({status: 'ready', descriptor: {id: 'visits'}, data: {hidden: false}, errors: [], generation: 1});
      return () => {};
    },
    async loadCollection(options) {
      loads.push(options);
      return result(options.offset ?? 0, 3);
    },
    createHydratedRowContext() {
      return {disconnect() {}};
    }
  };
  const collection = new CausewayCollectionElement();
  collection.querySelector = () => ({presentation: null});
  collection.id = 'visits';
  collection.columns = [{member: 'name'}];
  collection.paged = 1;
  collection.active = true;
  collection.acceptGridResponsiveState(true);
  collection.context = context;
  document.body.appendChild(collection);
  await waitFor(() => collection.collectionState.status === 'ready');
  assert.equal(collection.gridQualification.presentation, 'grid-bounded');
  assert.match(collection.innerHTML, /Items 1–1 of 3/);

  const next = document.createElement('button');
  next.setAttribute('data-causeway-grid-next', '');
  collection.appendChild(next);
  next.dispatchEvent(new Event('click', {bubbles: true}));
  await waitFor(() => collection.collectionState.window?.offset === 1);
  assert.match(collection.innerHTML, /Items 2–2 of 3/);
  next.dispatchEvent(new Event('click', {bubbles: true}));
  await waitFor(() => collection.collectionState.window?.offset === 2);
  assert.match(collection.innerHTML, /Items 3–3 of 3/);
  assert.equal(collection.collectionState.window.hasNext, false);
  document.body.removeChild(collection);
});

test('bounded pager uses only normalized offsets and never invents a total', async () => {
  configureCausewayGridWidgets({enabled: true});
  const loads = [];
  const context = {
    registerRequirement(_requirement, listener) {
      listener({status: 'ready', descriptor: {id: 'people'}, data: {hidden: false}, errors: [], generation: 1});
      return () => {};
    },
    async loadCollection(options) {
      loads.push(options);
      return result(options.offset ?? 0, null);
    },
    createHydratedRowContext() {
      return {disconnect() {}};
    }
  };
  const collection = new CausewayCollectionElement();
  collection.querySelector = () => ({presentation: null});
  collection.id = 'people';
  collection.columns = [{member: 'name'}];
  collection.active = true;
  collection.acceptGridResponsiveState(true);
  collection.context = context;
  document.body.appendChild(collection);
  await waitFor(() => collection.collectionState.status === 'ready');
  assert.equal(collection.gridQualification.presentation, 'grid-bounded');
  assert.match(collection.innerHTML, /Items 1–1/);
  assert.doesNotMatch(collection.innerHTML, / of /);

  const next = document.createElement('button');
  next.setAttribute('data-causeway-grid-next', '');
  collection.appendChild(next);
  next.dispatchEvent(new Event('click', {bubbles: true}));
  await waitFor(() => loads.length === 2);
  assert.equal(loads[1].offset, 1);
  assert.equal(loads[1].size, 10);
  document.body.removeChild(collection);
});
