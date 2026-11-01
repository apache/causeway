/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0.
 */

import {escapeHtml} from './rendering.mjs';
import {defaultValueRendererRegistry, renderCausewayValue} from './value-renderers.mjs';

export function buildCausewayGridProjection({
  rows = [],
  columns = [],
  rowDescription = null,
  errors = [],
  rendererRegistry = defaultValueRendererRegistry,
  previewForRow = () => null
} = {}) {
  if (!Array.isArray(rows) || !Array.isArray(columns) || !Array.isArray(errors)) {
    return unsupportedProjection('invalid-input');
  }
  const acceptedColumns = columns.filter(column => column?.hidden !== true);
  const descriptors = [identityColumn(), ...acceptedColumns.map(memberColumn)];
  const projectedRows = [];
  try {
    for (let rowIndex = 0; rowIndex < rows.length; rowIndex += 1) {
      projectedRows.push(projectRow({
        row: rows[rowIndex],
        rowIndex,
        columns: acceptedColumns,
        rowDescription,
        errors,
        rendererRegistry,
        preview: typeof previewForRow === 'function' ? previewForRow(rows[rowIndex], rowIndex) : null
      }));
    }
  } catch (_error) {
    return unsupportedProjection('projection-unavailable');
  }
  return Object.freeze({
    supported: true,
    reason: null,
    columns: Object.freeze(descriptors),
    rows: Object.freeze(projectedRows)
  });
}

export function renderCausewayGridCell(root, cell, rowKey = '') {
  if (!root) return false;
  clearCell(root);
  if (!cell) return false;
  if (rowKey) root.setAttribute('data-causeway-grid-row-key', rowKey);
  root.setAttribute('data-causeway-grid-member', cell.member);
  root.setAttribute('data-causeway-grid-role', cell.member === '_meta' ? 'object-link' : 'cell');
  if (cell.hidden) {
    root.hidden = true;
    root.setAttribute('aria-hidden', 'true');
    return true;
  }
  root.hidden = false;
  if (cell.error) {
    root.className = 'causeway-error';
    root.setAttribute('role', 'alert');
  }
  if (cell.disabledReason) {
    root.setAttribute('aria-disabled', 'true');
    root.setAttribute('title', cell.disabledReason);
  }
  if (cell.testId) root.setAttribute('data-testid', cell.testId);
  root.innerHTML = cell.html;
  return true;
}

function identityColumn() {
  return Object.freeze({
    kind: 'identity',
    member: '_meta',
    label: 'Item',
    testId: null,
    render(root, item) {
      renderCausewayGridCell(root, item?.cells?.[0], item?.key);
    }
  });
}

function memberColumn(column, columnIndex) {
  const member = String(column.member ?? '');
  if (!member) throw new TypeError('A Grid member column requires an identity.');
  return Object.freeze({
    kind: 'member',
    member,
    label: String(column.label || humanize(member)),
    testId: column.testId ? String(column.testId) : null,
    render(root, item) {
      renderCausewayGridCell(root, item?.cells?.[columnIndex + 1], item?.key);
    }
  });
}

function projectRow({row, rowIndex, columns, rowDescription, errors, rendererRegistry, preview}) {
  const metadata = row?._meta;
  if (!metadata?.logicalTypeName || !metadata?.id) throw new TypeError('A Grid row requires Causeway object identity.');
  const identity = Object.freeze({
    logicalTypeName: String(metadata.logicalTypeName),
    id: String(metadata.id),
    title: String(metadata.title ?? metadata.id),
    icon: metadata.icon ? String(metadata.icon) : ''
  });
  const cells = [Object.freeze({
    member: '_meta',
    hidden: false,
    disabledReason: '',
    error: false,
    testId: null,
    rendererId: 'object-link',
    standard: true,
    kind: 'reference',
    html: `<cw-object-link logical-type="${escapeHtml(identity.logicalTypeName)}" object-id="${escapeHtml(identity.id)}" title="${escapeHtml(identity.title)}"${identity.icon ? ` icon="${escapeHtml(identity.icon)}"` : ''}></cw-object-link>`
  })];
  for (const column of columns) {
    const property = row?.[column.member] ?? null;
    if (property?.hidden === true) {
      cells.push(Object.freeze({
        member: column.member,
        hidden: true,
        disabledReason: '',
        error: false,
        testId: null,
        rendererId: 'hidden',
        standard: true,
        kind: 'hidden',
        html: ''
      }));
      continue;
    }
    const matchingError = errors.find(error => {
      const path = error?.path ?? [];
      return path.includes(rowIndex) && path.includes(column.member);
    });
    if (matchingError) {
      cells.push(Object.freeze({
        member: column.member,
        hidden: false,
        disabledReason: '',
        error: true,
        testId: cellTestId(column.testId, rowIndex),
        rendererId: 'error',
        standard: true,
        kind: 'error',
        html: escapeHtml(matchingError.message ?? 'Collection cell error')
      }));
      continue;
    }
    const descriptor = rowDescription?.members?.get?.(column.member) ?? null;
    if (!descriptor) throw new TypeError('A Grid member column requires an accepted semantic descriptor.');
    const rendered = renderCausewayValue({value: property?.get, descriptor}, rendererRegistry);
    cells.push(Object.freeze({
      member: column.member,
      hidden: false,
      disabledReason: typeof property?.disabled === 'string' ? property.disabled : '',
      error: false,
      testId: cellTestId(column.testId, rowIndex),
      rendererId: rendered.rendererId,
      standard: rendered.standard === true,
      kind: rendered.kind,
      html: rendered.html
    }));
  }
  return Object.freeze({
    key: `${identity.logicalTypeName}:${identity.id}`,
    identity,
    cells: Object.freeze(cells),
    preview: preview ? Object.freeze({...preview}) : null
  });
}

function unsupportedProjection(reason) {
  return Object.freeze({
    supported: false,
    reason,
    columns: Object.freeze([]),
    rows: Object.freeze([])
  });
}

function clearCell(root) {
  root.innerHTML = '';
  root.className = '';
  root.hidden = false;
  for (const attribute of [
    'aria-disabled',
    'aria-hidden',
    'role',
    'title',
    'data-testid',
    'aria-describedby',
    'data-causeway-grid-row-key',
    'data-causeway-grid-member',
    'data-causeway-grid-role'
  ]) {
    root.removeAttribute?.(attribute);
  }
}

function cellTestId(testId, rowIndex) {
  return testId ? `${testId}-row-${rowIndex}` : null;
}

function humanize(value) {
  return String(value || '').replace(/([a-z0-9])([A-Z])/g, '$1 $2').replace(/^./, character => character.toUpperCase());
}
