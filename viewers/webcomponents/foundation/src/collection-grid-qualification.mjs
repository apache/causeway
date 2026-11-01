/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0.
 */

export const CausewayCollectionPresentation = Object.freeze({
  NATIVE: 'native',
  GRID_VIRTUAL: 'grid-virtual',
  GRID_BOUNDED: 'grid-bounded'
});

const DETERMINISTIC_ORDERINGS = new Set(['CONFIGURED', 'REQUESTED']);

export function qualifyCausewayCollectionGrid(candidate = {}) {
  const window = candidate.window ?? null;
  const rows = Array.isArray(candidate.rows) ? candidate.rows : [];
  const ordering = boundedOrdering(window?.ordering);
  const orderingBasis = boundedOrdering(candidate.orderingBasis ?? window?.ordering);
  const count = classifyCount(window?.totalCount);
  const responsive = candidate.wide === true ? 'wide' : 'narrow';
  const lifecycle = freezeLifecycle(candidate.lifecycle);
  const base = {ordering, count, responsive, lifecycle};

  if (candidate.policyEnabled !== true) return native(base, 'policy-native');
  if (candidate.familyHealthy !== true) return native(base, 'family-failed');
  if (candidate.connected !== true) return native(base, 'disconnected');
  if (candidate.active !== true) return native(base, 'inactive');
  if (candidate.visible !== true) return native(base, 'hidden');
  if (candidate.wide !== true) return native(base, 'narrow');
  if (candidate.ready !== true) return native(base, 'not-ready');
  if (!validWindow(window, rows)) return native(base, 'window-unavailable');
  if (!DETERMINISTIC_ORDERINGS.has(orderingBasis)) return native(base, 'ordering-not-deterministic');
  if (count === 'invalid') return native(base, 'total-invalid');
  if (count === 'zero' || terminalEmptyFirstWindow(window, rows)) return native(base, 'empty');
  if (candidate.columnsSupported !== true || candidate.hasVisibleColumn !== true) {
    return native(base, 'columns-unsupported');
  }
  if (candidate.renderersSupported !== true) return native(base, 'renderers-unsupported');
  if (count === 'available') {
    return grid(base, candidate.bounded === true
      ? CausewayCollectionPresentation.GRID_BOUNDED
      : CausewayCollectionPresentation.GRID_VIRTUAL);
  }
  if (window.hasPrevious === true || window.hasNext === true) {
    return grid(base, CausewayCollectionPresentation.GRID_BOUNDED);
  }
  return native(base, 'paging-unavailable');
}

export function publishCausewayGridDiagnostics(host, qualification) {
  if (!host?.dataset || !qualification) return;
  host.dataset.causewayGridToolkit = qualification.toolkit;
  host.dataset.causewayGridPresentation = qualification.presentation;
  host.dataset.causewayGridOrdering = qualification.ordering;
  host.dataset.causewayGridCount = qualification.count;
  host.dataset.causewayGridResponsive = qualification.responsive;
  host.dataset.causewayGridFallback = qualification.reason ?? '';
}

function grid(base, presentation) {
  return Object.freeze({
    ...base,
    qualified: true,
    toolkit: 'vaadin',
    presentation,
    reason: null
  });
}

function native(base, reason) {
  return Object.freeze({
    ...base,
    qualified: false,
    toolkit: reason === 'policy-native' ? 'native' : 'vaadin',
    presentation: CausewayCollectionPresentation.NATIVE,
    reason
  });
}

function validWindow(window, rows) {
  if (!window) return false;
  if (!safeNonNegative(window.offset)) return false;
  if (!safePositive(window.requestedSize)) return false;
  if (!safePositive(window.maximumSize)) return false;
  if (window.requestedSize > window.maximumSize) return false;
  if (!safeNonNegative(window.returnedCount)) return false;
  if (window.returnedCount > window.requestedSize || window.returnedCount !== rows.length) return false;
  if (window.hasPrevious === true && !safeNonNegative(window.previousOffset)) return false;
  if (window.hasNext === true && !safeNonNegative(window.nextOffset)) return false;
  return true;
}

function terminalEmptyFirstWindow(window, rows) {
  return window.offset === 0 && rows.length === 0 && window.hasNext !== true;
}

function classifyCount(value) {
  if (value == null) return 'unavailable';
  if (!safeNonNegative(value)) return 'invalid';
  return value === 0 ? 'zero' : 'available';
}

function boundedOrdering(value) {
  const token = String(value ?? 'UNAVAILABLE').replace(/[^A-Z0-9_-]/gi, '_').slice(0, 40).toUpperCase();
  return token || 'UNAVAILABLE';
}

function freezeLifecycle(value = {}) {
  return Object.freeze({
    hostRevision: safeRevision(value.hostRevision),
    responsiveRevision: safeRevision(value.responsiveRevision),
    policyRevision: safeRevision(value.policyRevision),
    rendererRevision: safeRevision(value.rendererRevision)
  });
}

function safeRevision(value) {
  return safeNonNegative(value) ? value : 0;
}

function safeNonNegative(value) {
  return Number.isSafeInteger(value) && value >= 0;
}

function safePositive(value) {
  return Number.isSafeInteger(value) && value > 0;
}
