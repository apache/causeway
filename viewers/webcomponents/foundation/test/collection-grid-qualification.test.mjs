/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0.
 */

import assert from 'node:assert/strict';
import test from 'node:test';

import {
  CausewayCollectionPresentation,
  publishCausewayGridDiagnostics,
  qualifyCausewayCollectionGrid
} from '../src/collection-grid-qualification.mjs';

const rows = Object.freeze([{_meta: {id: '1', logicalTypeName: 'example.Person'}}]);

function candidate(overrides = {}) {
  return {
    active: true,
    visible: true,
    wide: true,
    ready: true,
    policyEnabled: true,
    familyHealthy: true,
    connected: true,
    columnsSupported: true,
    hasVisibleColumn: true,
    renderersSupported: true,
    rows,
    window: {
      offset: 0,
      requestedSize: 20,
      returnedCount: 1,
      totalCount: 42,
      maximumSize: 50,
      hasPrevious: false,
      hasNext: true,
      previousOffset: null,
      nextOffset: 1,
      ordering: 'CONFIGURED'
    },
    lifecycle: {hostRevision: 2, responsiveRevision: 3, policyRevision: 4, rendererRevision: 5},
    ...overrides
  };
}

test('stable positive total and deterministic ordering qualify virtual Grid', () => {
  const result = qualifyCausewayCollectionGrid(candidate());
  assert.equal(result.qualified, true);
  assert.equal(result.presentation, CausewayCollectionPresentation.GRID_VIRTUAL);
  assert.equal(result.count, 'available');
  assert.equal(result.ordering, 'CONFIGURED');
  assert.deepEqual(result.lifecycle, {hostRevision: 2, responsiveRevision: 3, policyRevision: 4, rendererRevision: 5});
  assert.equal(Object.isFrozen(result), true);
  assert.equal(Object.isFrozen(result.lifecycle), true);
});

test('unavailable total with normalized paging qualifies bounded Grid without invented size', () => {
  const window = {...candidate().window, totalCount: null};
  const result = qualifyCausewayCollectionGrid(candidate({window}));
  assert.equal(result.qualified, true);
  assert.equal(result.presentation, CausewayCollectionPresentation.GRID_BOUNDED);
  assert.equal(result.count, 'unavailable');
});

test('safe zero and terminal empty first windows retain native empty state', () => {
  const zero = qualifyCausewayCollectionGrid(candidate({
    rows: [],
    window: {...candidate().window, returnedCount: 0, totalCount: 0, hasNext: false, nextOffset: null}
  }));
  assert.equal(zero.presentation, 'native');
  assert.equal(zero.reason, 'empty');
  assert.equal(zero.count, 'zero');
  const terminal = qualifyCausewayCollectionGrid(candidate({
    rows: [],
    window: {...candidate().window, returnedCount: 0, totalCount: null, hasNext: false, nextOffset: null}
  }));
  assert.equal(terminal.reason, 'empty');
  assert.equal(terminal.count, 'unavailable');
});

test('unsafe totals and unavailable totals without paging fail closed', () => {
  const invalid = qualifyCausewayCollectionGrid(candidate({window: {...candidate().window, totalCount: -1}}));
  assert.equal(invalid.reason, 'total-invalid');
  const noPaging = qualifyCausewayCollectionGrid(candidate({
    window: {...candidate().window, totalCount: null, hasNext: false, nextOffset: null}
  }));
  assert.equal(noPaging.reason, 'paging-unavailable');
});

test('encounter unstable and malformed ordering never qualify', () => {
  for (const ordering of ['ENCOUNTER', null, 'unstable/value']) {
    const result = qualifyCausewayCollectionGrid(candidate({window: {...candidate().window, ordering}}));
    assert.equal(result.qualified, false);
    assert.equal(result.reason, 'ordering-not-deterministic');
  }
});

test('window capability and every bounded range invariant fail closed', () => {
  assert.equal(qualifyCausewayCollectionGrid(candidate({window: null})).reason, 'window-unavailable');
  const invalidWindows = [
    {offset: -1},
    {requestedSize: 0},
    {requestedSize: 60},
    {maximumSize: 0},
    {returnedCount: 2},
    {hasPrevious: true, previousOffset: null},
    {hasNext: true, nextOffset: null}
  ];
  for (const override of invalidWindows) {
    const result = qualifyCausewayCollectionGrid(candidate({window: {...candidate().window, ...override}}));
    assert.equal(result.reason, 'window-unavailable');
  }
});

test('activity visibility width readiness policy family columns and renderers fail independently', () => {
  const matrix = [
    ['policyEnabled', false, 'policy-native'],
    ['familyHealthy', false, 'family-failed'],
    ['connected', false, 'disconnected'],
    ['active', false, 'inactive'],
    ['visible', false, 'hidden'],
    ['wide', false, 'narrow'],
    ['ready', false, 'not-ready'],
    ['columnsSupported', false, 'columns-unsupported'],
    ['hasVisibleColumn', false, 'columns-unsupported'],
    ['renderersSupported', false, 'renderers-unsupported']
  ];
  for (const [key, value, reason] of matrix) {
    const result = qualifyCausewayCollectionGrid(candidate({[key]: value}));
    assert.equal(result.qualified, false, key);
    assert.equal(result.reason, reason, key);
  }
});

test('bounded value-free diagnostics expose qualification classes only', () => {
  const host = {dataset: {}};
  const result = qualifyCausewayCollectionGrid(candidate({wide: false}));
  publishCausewayGridDiagnostics(host, result);
  assert.deepEqual(host.dataset, {
    causewayGridToolkit: 'vaadin',
    causewayGridPresentation: 'native',
    causewayGridOrdering: 'CONFIGURED',
    causewayGridCount: 'available',
    causewayGridResponsive: 'narrow',
    causewayGridFallback: 'narrow'
  });
  assert.equal(JSON.stringify(host.dataset).includes('example.Person'), false);
});
