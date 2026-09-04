/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0.
 */

import assert from 'node:assert/strict';
import test from 'node:test';

import {installDomShim} from './dom-shim.mjs';

installDomShim();
const {
  CausewayCollectionGridElement,
  causewayGridWidgetConfiguration,
  configureCausewayGridWidgets,
  failCausewayGridWidget,
  useCausewayGridWidget
} = await import('../src/grid-widget.mjs');

const fakeGridModule = new URL('./fixtures/fake-vaadin-grid.mjs', import.meta.url).href;
const delayedGridModule = new URL('./fixtures/fake-vaadin-delayed-grid.mjs', import.meta.url).href;
const column = Object.freeze({
  label: 'Name',
  render(root, item) {
    const span = document.createElement('span');
    span.textContent = item.name;
    root.appendChild(span);
  }
});

function boundedPresentation() {
  return {
    mode: 'bounded',
    rows: [{name: 'Ada'}, {name: 'Grace'}],
    columns: [column],
    pageSize: 2,
    labelledBy: 'people-label',
    describedBy: 'people-description',
    testId: 'people-grid'
  };
}

async function connect(adapter) {
  document.body.appendChild(adapter);
  await new Promise(resolve => setTimeout(resolve, 0));
}

test('Grid widget configuration is bounded and publishes value-free revisions', () => {
  const events = [];
  const listener = event => events.push(event.detail);
  document.addEventListener('causeway-grid-widget-policy', listener);
  configureCausewayGridWidgets({enabled: true, moduleUrl: fakeGridModule, definitionTimeoutMs: 100});
  const policy = causewayGridWidgetConfiguration();
  assert.equal(policy.enabled, true);
  assert.equal(policy.failed, false);
  assert.equal(policy.definitionTimeoutMs, 100);
  assert.equal(useCausewayGridWidget(), true);
  assert.equal(events.at(-1).family, 'grid');
  assert.equal(events.at(-1).reason, 'configuration');
  assert.equal(Number.isSafeInteger(events.at(-1).revision), true);
  assert.throws(() => configureCausewayGridWidgets({moduleUrl: 'javascript:alert(1)'}), /approved module protocol/);
  assert.throws(() => configureCausewayGridWidgets({definitionTimeoutMs: 0}), /definition timeout/);
  document.removeEventListener('causeway-grid-widget-policy', listener);
  configureCausewayGridWidgets({enabled: true, moduleUrl: fakeGridModule});
});

test('missing custom-element definitions fail only Grid and a later revision recovers once', async () => {
  const emptyModule = new URL('./fixtures/fake-vaadin-grid-empty.mjs', import.meta.url).href;
  configureCausewayGridWidgets({enabled: true, moduleUrl: emptyModule, definitionTimeoutMs: 10});
  const adapter = new CausewayCollectionGridElement();
  adapter.presentation = boundedPresentation();
  let detail;
  adapter.addEventListener('causeway-grid-load-failed', event => { detail = event.detail; });
  await connect(adapter);
  assert.equal(adapter.dataset.widgetState, 'fallback');
  assert.equal(detail.phase, 'definition');
  assert.equal(detail.classification, 'GRID_DEFINITION_UNAVAILABLE');
  configureCausewayGridWidgets({enabled: true, moduleUrl: fakeGridModule});
  await new Promise(resolve => setTimeout(resolve, 0));
  assert.equal(adapter.dataset.widgetState, 'ready');
  assert.equal(adapter.childNodes.filter(child => child.localName === 'vaadin-grid').length, 1);
  const gridConstructor = customElements.get('vaadin-grid');
  configureCausewayGridWidgets({enabled: true, moduleUrl: fakeGridModule});
  await new Promise(resolve => setTimeout(resolve, 0));
  assert.equal(customElements.get('vaadin-grid'), gridConstructor);
  assert.equal(adapter.childNodes.filter(child => child.localName === 'vaadin-grid').length, 1);
  document.body.removeChild(adapter);
});

test('bounded Grid upgrades lazily with immutable rows columns and relationships', async () => {
  configureCausewayGridWidgets({enabled: true, moduleUrl: fakeGridModule});
  const adapter = new CausewayCollectionGridElement();
  adapter.presentation = boundedPresentation();
  await connect(adapter);
  const control = adapter.childNodes[0];
  assert.equal(adapter.dataset.widgetState, 'ready');
  assert.equal(control.localName, 'vaadin-grid');
  assert.equal(control.items.length, 2);
  assert.equal(control.pageSize, 2);
  assert.equal(control.allRowsVisible, true);
  assert.equal(control.getAttribute('aria-labelledby'), 'people-label');
  assert.equal(control.getAttribute('aria-describedby'), 'people-description');
  assert.equal(control.getAttribute('data-testid'), 'people-grid');
  assert.equal(control.childNodes.length, 1);
  assert.equal(control.childNodes[0].header, 'Name');
  assert.equal(control.childNodes[0].resizable, false);
  assert.equal(control.columnReorderingAllowed, false);

  adapter.presentation = {
    ...boundedPresentation(),
    resizableColumns: true,
    reorderableColumns: true
  };
  assert.equal(control.childNodes[0].resizable, true);
  assert.equal(control.columnReorderingAllowed, true);

  adapter.presentation = boundedPresentation();
  assert.equal(control.childNodes[0].resizable, false);
  assert.equal(control.columnReorderingAllowed, false);
  document.body.removeChild(adapter);
});

test('Grid row details use one leading disclosure and preserve the projected item', async () => {
  configureCausewayGridWidgets({enabled: true, moduleUrl: fakeGridModule});
  let expandedKey = null;
  let detailsController;
  const item = {
    key: 'example.Staff:1',
    identity: {title: 'Ada'},
    preview: {presentation: {html: '<p>Ada</p>'}}
  };
  const adapter = new CausewayCollectionGridElement();
  adapter.presentation = {
    ...boundedPresentation(),
    rows: [item],
    rowDetails: {
      expandedKey: () => expandedKey,
      toggle(candidate) {
        expandedKey = expandedKey === candidate.key ? null : candidate.key;
        return expandedKey === candidate.key;
      },
      render(root, candidate, controller) {
        assert.equal(candidate, item);
        detailsController = controller;
        const content = document.createElement('p');
        content.textContent = candidate.identity.title;
        root.appendChild(content);
      }
    }
  };
  await connect(adapter);
  const control = adapter.childNodes[0];
  assert.equal(control.childNodes.length, 2);
  assert.equal(control.childNodes[0].header, 'Preview');
  assert.equal(control.itemIdPath, 'key');
  const cell = document.createElement('div');
  control.childNodes[0].renderer(cell, control.childNodes[0], {item});
  const button = cell.childNodes[0];
  assert.equal(button.getAttribute('aria-expanded'), 'false');
  assert.equal(button.getAttribute('aria-controls'), 'causeway-grid-preview-example.Staff_1');
  button.dispatchEvent(new Event('click'));
  assert.deepEqual(control.detailsOpenedItems, [item]);

  const details = document.createElement('div');
  control.rowDetailsRenderer(details, control, {item});
  assert.equal(details.id, 'causeway-grid-preview-example.Staff_1');
  assert.equal(details.childNodes[0].textContent, 'Ada');
  detailsController.close({restoreFocus: true});
  assert.deepEqual(control.detailsOpenedItems, []);
  document.body.removeChild(adapter);
});

test('Grid renders Causeway-owned sort headers without enabling toolkit sorting', async () => {
  configureCausewayGridWidgets({enabled: true, moduleUrl: fakeGridModule});
  const requests = [];
  const adapter = new CausewayCollectionGridElement();
  adapter.presentation = {
    ...boundedPresentation(),
    columns: [{...column, member: 'name'}],
    sortableMembers: ['name'],
    sortCriterion: {member: 'name', direction: 'ASCENDING'},
    sortCallback: member => requests.push(member)
  };
  await connect(adapter);
  const gridColumn = adapter.childNodes[0].childNodes[0];
  assert.equal(gridColumn.sortable, false);
  assert.equal(typeof gridColumn.headerRenderer, 'function');
  const root = document.createElement('div');
  gridColumn.headerRenderer(root);
  const button = root.childNodes[0];
  assert.equal(button.dataset.causewayCollectionSort, 'name');
  assert.match(button.textContent, /↑/);
  button.dispatchEvent(new Event('click'));
  assert.deepEqual(requests, ['name']);

  adapter.presentation = {
    ...boundedPresentation(),
    columns: [{...column, member: 'name'}],
    sortableMembers: ['name'],
    sortCriterion: null,
    sortCallback: member => requests.push(member)
  };
  const unsortedColumn = adapter.childNodes[0].childNodes[0];
  const unsortedRoot = document.createElement('div');
  unsortedColumn.headerRenderer(unsortedRoot);
  assert.match(unsortedRoot.childNodes[0].textContent, /↕/);
  document.body.removeChild(adapter);
});

test('virtual Grid maps page callbacks to bounded range provider', async () => {
  configureCausewayGridWidgets({enabled: true, moduleUrl: fakeGridModule});
  const requests = [];
  const adapter = new CausewayCollectionGridElement();
  adapter.presentation = {
    ...boundedPresentation(),
    mode: 'virtual',
    totalCount: 9,
    pageSize: 3,
    rangeProvider: async request => {
      requests.push(request);
      return {rows: [{name: 'Linus'}]};
    }
  };
  await connect(adapter);
  const control = adapter.childNodes[0];
  assert.equal(control.allRowsVisible, false);
  let callbackRows;
  let callbackTotal;
  control.dataProvider({page: 2, pageSize: 3}, (rows, total) => {
    callbackRows = rows;
    callbackTotal = total;
  });
  await new Promise(resolve => setTimeout(resolve, 0));
  assert.deepEqual(requests, [{offset: 6, size: 3}]);
  assert.deepEqual(callbackRows, [{name: 'Linus'}]);
  assert.equal(callbackTotal, 9);
  assert.equal(control.clearCacheCount, 1);
  document.body.removeChild(adapter);
});

test('complete one-window virtual Grid fits rows and returns to scrolling when the total grows', async () => {
  configureCausewayGridWidgets({enabled: true, moduleUrl: fakeGridModule});
  const adapter = new CausewayCollectionGridElement();
  adapter.presentation = {
    ...boundedPresentation(),
    mode: 'virtual',
    totalCount: 2,
    pageSize: 10,
    rangeProvider: async () => ({rows: boundedPresentation().rows})
  };
  await connect(adapter);
  const control = adapter.childNodes[0];
  assert.equal(control.allRowsVisible, true);

  adapter.presentation = {
    ...boundedPresentation(),
    mode: 'virtual',
    totalCount: 3,
    pageSize: 10,
    rangeProvider: async () => ({rows: boundedPresentation().rows})
  };
  assert.equal(control.allRowsVisible, false);

  adapter.presentation = boundedPresentation();
  assert.equal(control.allRowsVisible, true);
  document.body.removeChild(adapter);
});

test('semantic focus follows current row and member rather than a recycled cell node', async () => {
  configureCausewayGridWidgets({enabled: true, moduleUrl: fakeGridModule});
  const adapter = new CausewayCollectionGridElement();
  const item = {key: 'example.Person:1', cells: [{html: 'Ada'}]};
  adapter.presentation = {
    ...boundedPresentation(),
    rows: [item],
    columns: [{member: 'name', label: 'Name', render(root) { root.innerHTML = 'Ada'; }}]
  };
  await connect(adapter);
  assert.equal(adapter.restoreSemanticFocus({rowKey: item.key, member: 'name', role: 'cell'}), true);
  const root = document.createElement('div');
  document.body.appendChild(root);
  adapter.childNodes[0].childNodes[0].renderer(root, null, {item, index: 0});
  await Promise.resolve();
  assert.equal(document.activeElement, root);
  document.body.removeChild(root);
  document.body.removeChild(adapter);
});

test('disconnect and reconnect cannot apply stale Grid work', async () => {
  configureCausewayGridWidgets({enabled: true, moduleUrl: fakeGridModule});
  const adapter = new CausewayCollectionGridElement();
  adapter.presentation = boundedPresentation();
  document.body.appendChild(adapter);
  document.body.removeChild(adapter);
  await new Promise(resolve => setTimeout(resolve, 0));
  assert.notEqual(adapter.dataset.widgetState, 'ready');
  await connect(adapter);
  assert.equal(adapter.dataset.widgetState, 'ready');
  document.body.removeChild(adapter);
});

test('policy revisions cannot complete stale Grid upgrades', async () => {
  configureCausewayGridWidgets({enabled: true, moduleUrl: `${delayedGridModule}?revision=${Date.now()}`});
  const adapter = new CausewayCollectionGridElement();
  adapter.presentation = boundedPresentation();
  document.body.appendChild(adapter);
  await new Promise(resolve => setTimeout(resolve, 0));
  assert.equal(adapter.dataset.widgetState, 'loading');
  configureCausewayGridWidgets({enabled: false, moduleUrl: fakeGridModule});
  await new Promise(resolve => setTimeout(resolve, 40));
  assert.notEqual(adapter.dataset.widgetState, 'ready');
  document.body.removeChild(adapter);
});

test('native policy avoids Grid upgrade', async () => {
  configureCausewayGridWidgets({enabled: false, moduleUrl: fakeGridModule});
  const adapter = new CausewayCollectionGridElement();
  adapter.presentation = boundedPresentation();
  await connect(adapter);
  assert.equal(adapter.dataset.widgetState, 'native');
  assert.equal(adapter.childNodes.length, 0);
  document.body.removeChild(adapter);
});

test('Grid module and renderer failures are bounded and recoverable', async () => {
  configureCausewayGridWidgets({enabled: true, moduleUrl: new URL('./fixtures/missing-vaadin-grid.mjs', import.meta.url).href});
  const adapter = new CausewayCollectionGridElement();
  adapter.presentation = boundedPresentation();
  let failures = 0;
  adapter.addEventListener('causeway-grid-load-failed', event => {
    failures += 1;
    assert.equal(event.detail.family, 'grid');
    assert.equal(event.detail.phase, 'module');
    assert.equal(event.detail.classification, 'GRID_MODULE_UNAVAILABLE');
    assert.equal(Number.isSafeInteger(event.detail.revision), true);
  });
  await connect(adapter);
  assert.equal(adapter.dataset.widgetState, 'fallback');
  assert.equal(failures, 1);
  assert.equal(useCausewayGridWidget(), false);
  assert.deepEqual(causewayGridWidgetConfiguration().failure, {
    phase: 'module',
    classification: 'GRID_MODULE_UNAVAILABLE'
  });
  configureCausewayGridWidgets({enabled: true, moduleUrl: fakeGridModule});
  await new Promise(resolve => setTimeout(resolve, 0));
  assert.equal(adapter.dataset.widgetState, 'ready');
  assert.equal(adapter.childNodes.filter(child => child.localName === 'vaadin-grid').length, 1);
  document.body.removeChild(adapter);
  assert.equal(failCausewayGridWidget({phase: 'renderer', classification: 'GRID_RENDERER_UNAVAILABLE'}), true);
  assert.equal(failCausewayGridWidget({phase: 'again', classification: 'IGNORED'}), false);
  assert.equal(useCausewayGridWidget(), false);
  configureCausewayGridWidgets({enabled: true, moduleUrl: fakeGridModule});
});

test('stale range rejection after presentation replacement cannot fail the Grid family', async () => {
  configureCausewayGridWidgets({enabled: true, moduleUrl: fakeGridModule});
  let rejectRange;
  const adapter = new CausewayCollectionGridElement();
  adapter.presentation = {
    ...boundedPresentation(),
    mode: 'virtual',
    totalCount: 4,
    rangeProvider: () => new Promise((_, reject) => { rejectRange = reject; })
  };
  await connect(adapter);
  const oldControl = adapter.childNodes[0];
  oldControl.dataProvider({page: 0, pageSize: 2}, () => assert.fail('stale callback must not publish'));
  adapter.presentation = boundedPresentation();
  rejectRange(new Error('value-bearing details must remain private'));
  await new Promise(resolve => setTimeout(resolve, 0));
  assert.equal(useCausewayGridWidget(), true);
  assert.equal(adapter.dataset.widgetState, 'ready');
  document.body.removeChild(adapter);
});
