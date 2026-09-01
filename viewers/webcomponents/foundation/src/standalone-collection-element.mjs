/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import {buildCausewayGridProjection} from './collection-grid-projection.mjs';
import {
  publishCausewayGridDiagnostics,
  qualifyCausewayCollectionGrid
} from './collection-grid-qualification.mjs';
import {CausewaySemanticEvent} from './component-contracts.mjs';
import {createSemanticEvent} from './context-events.mjs';
import {
  DescriptionPresentation,
  normalizeDescriptionPresentation
} from './description-presentation.mjs';
import {
  CAUSEWAY_COLLECTION_GRID,
  CAUSEWAY_GRID_WIDGET_POLICY_EVENT,
  causewayGridWidgetConfiguration
} from './grid-widget.mjs';
import {escapeHtml} from './rendering.mjs';
import {defaultValueRendererRegistry, renderCausewayValue} from './value-renderers.mjs';

const HTMLElementBase = globalThis.HTMLElement ?? class extends EventTarget {};
const initialColumnConfigurations = new WeakMap();
let standaloneCollectionSequence = 0;

export function captureDeclarativeStandaloneCollectionColumns(root = globalThis.document) {
  if (!root?.querySelectorAll) return;
  const collections = root.localName === 'cw-standalone-collection'
    ? [root]
    : root.querySelectorAll('cw-standalone-collection');
  for (const collection of collections) {
    const columns = declarativeColumns(collection);
    if (columns.length > 0) initialColumnConfigurations.set(collection, columns);
  }
}

export class CausewayStandaloneCollectionElement extends HTMLElementBase {
  static get observedAttributes() {
    return ['named', 'described-as', 'description-as', 'resizable-columns', 'reorderable-columns'];
  }

  constructor() {
    super();
    const sequence = ++standaloneCollectionSequence;
    this.labelId = `causeway-standalone-collection-label-${sequence}`;
    this.descriptionId = `causeway-standalone-collection-description-${sequence}`;
    this._result = null;
    this._columns = [];
    this._rendererRegistry = defaultValueRendererRegistry;
    this.resultState = frozenState('idle');
    this.resultGeneration = 0;
    this.connectionStarted = false;
    this.gridHostRevision = 0;
    this.gridResponsiveRevision = 0;
    this.gridResizeRevision = 0;
    this._gridWide = false;
    this._gridProjection = buildCausewayGridProjection();
    this._gridQualification = qualifyCausewayCollectionGrid();
    this.gridPolicyListener = () => {
      this.gridHostRevision += 1;
      this.render();
    };
    this.gridResizeObserver = typeof globalThis.ResizeObserver === 'function'
      ? new ResizeObserver(entries => {
        const width = entries.at(-1)?.contentRect?.width;
        if (!Number.isFinite(width)) return;
        const revision = ++this.gridResizeRevision;
        const apply = () => {
          if (this.isConnected && revision === this.gridResizeRevision) {
            this.acceptGridResponsiveState(width > collectionBreakpointPixels());
          }
        };
        if (typeof globalThis.requestAnimationFrame === 'function') globalThis.requestAnimationFrame(apply);
        else queueMicrotask(apply);
      })
      : null;
    this.columnObserver = typeof globalThis.MutationObserver === 'function'
      ? new MutationObserver(records => {
        if (records.some(record => [...(record.addedNodes ?? []), ...(record.removedNodes ?? [])]
          .some(node => node?.localName === 'cw-collection-column'))) {
          this.#captureColumns({replace: true});
          this.gridHostRevision += 1;
          this.render();
        }
      })
      : null;
    this.addEventListener(CausewaySemanticEvent.COLLECTION_CONFIGURATION, event => {
      if (event.target?.parentNode !== this) return;
      this.#acceptColumn(event.detail?.column);
    });
  }

  get named() {
    return this.getAttribute('named') || '';
  }

  set named(value) {
    setOptionalAttribute(this, 'named', value);
  }

  get describedAs() {
    return this.getAttribute('described-as') || '';
  }

  set describedAs(value) {
    setOptionalAttribute(this, 'described-as', value);
  }

  get descriptionAs() {
    return normalizeDescriptionPresentation(this.getAttribute('description-as'));
  }

  set descriptionAs(value) {
    setOptionalAttribute(this, 'description-as', value);
  }

  get resizableColumns() {
    return this.hasAttribute('resizable-columns');
  }

  set resizableColumns(value) {
    setBooleanAttribute(this, 'resizable-columns', value);
  }

  get reorderableColumns() {
    return this.hasAttribute('reorderable-columns');
  }

  set reorderableColumns(value) {
    setBooleanAttribute(this, 'reorderable-columns', value);
  }

  get columns() {
    return Object.freeze(this._columns.map(column => Object.freeze({...column})));
  }

  set columns(value) {
    this._columns = normalizeColumns(value);
    this.gridHostRevision += 1;
    this.render();
  }

  get result() {
    return this._result;
  }

  set result(value) {
    this.resultGeneration += 1;
    this.gridHostRevision += 1;
    if (value == null) {
      this._result = null;
      this.resultState = frozenState('idle');
    } else if (value?.kind !== 'collection' || !Array.isArray(value.value)) {
      this._result = null;
      this.resultState = frozenState('unsupported', [], [{message: 'A normalized collection action result is required.'}]);
    } else {
      const rows = Object.freeze([...value.value]);
      this._result = Object.freeze({kind: 'collection', value: rows});
      this.resultState = frozenState(rows.length === 0 ? 'empty' : 'ready', rows);
    }
    this.render();
    this.#publishState();
  }

  get rendererRegistry() {
    return this._rendererRegistry;
  }

  set rendererRegistry(value) {
    this._rendererRegistry = value ?? defaultValueRendererRegistry;
    this.gridHostRevision += 1;
    this.render();
  }

  get gridQualification() {
    return this._gridQualification;
  }

  get gridProjection() {
    return this._gridProjection;
  }

  connectedCallback() {
    this.connectionStarted = true;
    this.classList?.add?.('causeway-standalone-collection-host');
    globalThis.document?.addEventListener?.(CAUSEWAY_GRID_WIDGET_POLICY_EVENT, this.gridPolicyListener);
    this.columnObserver?.observe(this, {childList: true});
    captureDeclarativeStandaloneCollectionColumns(this);
    this.#captureColumns();
    this.gridResizeObserver?.observe(this);
    this.render();
    this.#publishState();
  }

  disconnectedCallback() {
    this.connectionStarted = false;
    this.resultGeneration += 1;
    this.gridHostRevision += 1;
    this.gridResizeRevision += 1;
    globalThis.document?.removeEventListener?.(CAUSEWAY_GRID_WIDGET_POLICY_EVENT, this.gridPolicyListener);
    this.columnObserver?.disconnect();
    this.gridResizeObserver?.disconnect();
  }

  attributeChangedCallback(name, oldValue, newValue) {
    if (oldValue === newValue || !this.connectionStarted) return;
    if (name === 'resizable-columns' || name === 'reorderable-columns') this.gridHostRevision += 1;
    this.render();
  }

  acceptGridResponsiveState(wide) {
    const next = wide === true;
    if (next === this._gridWide) return false;
    this._gridWide = next;
    this.gridResponsiveRevision += 1;
    this.gridHostRevision += 1;
    this.render();
    return true;
  }

  async requestResultRange(
    {offset = 0, size = null} = {},
    generation = this.resultGeneration,
    hostRevision = this.gridHostRevision
  ) {
    const requestedSize = size ?? Math.max(this._gridProjection.rows.length, 1);
    if (!Number.isSafeInteger(offset) || offset < 0 || !Number.isSafeInteger(requestedSize) || requestedSize < 1) {
      throw new RangeError('Standalone collection ranges require a non-negative offset and positive size.');
    }
    await Promise.resolve();
    if (!this.isConnected
        || generation !== this.resultGeneration
        || hostRevision !== this.gridHostRevision) {
      const error = new Error('Standalone collection result range was superseded.');
      error.name = 'AbortError';
      throw error;
    }
    return Object.freeze({rows: Object.freeze(this._gridProjection.rows.slice(offset, offset + requestedSize))});
  }

  render() {
    const primary = this.#primary();
    if (!primary) return;
    try {
      const label = this.named.trim() || 'Action result';
      const description = normalizedText(this.describedAs) === normalizedText(label) ? '' : this.describedAs.trim();
      const descriptionVisible = this.descriptionAs === DescriptionPresentation.LABEL;
      const tooltip = description && !descriptionVisible ? description : '';
      const describedBy = description ? this.descriptionId : '';
      this.#qualifyGrid();
      let content;
      if (this.resultState.status === 'idle') {
        content = '<span class="causeway-empty" role="status">No result</span>';
      } else if (this.resultState.status === 'unsupported') {
        content = `<span class="causeway-unsupported" role="alert">${escapeHtml(this.resultState.errors[0]?.message ?? 'Unsupported action result.')}</span>`;
      } else if (this.resultState.rows.length === 0) {
        content = '<span class="causeway-empty" role="status">No items</span>';
      } else {
        content = this._gridQualification.qualified
          ? `<${CAUSEWAY_COLLECTION_GRID} data-causeway-collection-grid></${CAUSEWAY_COLLECTION_GRID}>`
          : this._columns.length > 0
            ? this.#renderTable(this.resultState.rows)
            : this.#renderRows(this.resultState.rows);
      }
      const count = ['ready', 'empty'].includes(this.resultState.status)
        ? `<output class="causeway-standalone-collection-count">${this.resultState.totalCount} result${this.resultState.totalCount === 1 ? '' : 's'}</output>`
        : '';
      primary.innerHTML = `<section class="causeway-collection causeway-standalone-collection" aria-labelledby="${this.labelId}"${describedBy ? ` aria-describedby="${describedBy}"` : ''}>
  <h2 id="${this.labelId}" class="causeway-collection-label${tooltip ? ' causeway-member-tooltip' : ''}"${tooltip ? ` tabindex="0" data-tooltip="${escapeHtml(tooltip)}" aria-describedby="${this.descriptionId}"` : ''}>${escapeHtml(label)}</h2>
  ${description ? `<p id="${this.descriptionId}" class="causeway-collection-description${descriptionVisible ? '' : ' causeway-visually-hidden'}">${escapeHtml(description)}</p>` : ''}
  ${count}
  <div class="causeway-collection-content">${content}</div>
</section>`;
      if (this._gridQualification.qualified) this.#installGrid(describedBy);
    } catch (error) {
      this.resultState = frozenState('error', [], [{message: error?.message ?? String(error)}]);
      this._gridProjection = buildCausewayGridProjection();
      this._gridQualification = qualifyCausewayCollectionGrid();
      publishCausewayGridDiagnostics(this, this._gridQualification);
      primary.innerHTML = `<section class="causeway-collection causeway-standalone-collection" aria-labelledby="${this.labelId}">
  <h2 id="${this.labelId}" class="causeway-collection-label">${escapeHtml(this.named.trim() || 'Action result')}</h2>
  <div class="causeway-collection-content"><span class="causeway-error" role="alert">${escapeHtml(error?.message ?? String(error))}</span></div>
</section>`;
    }
  }

  #renderRows(rows) {
    return `<ul class="causeway-collection-rows">${rows.map(row => {
      const metadata = row?._meta;
      if (validIdentity(metadata)) return `<li>${objectLink(metadata)}</li>`;
      const rendered = renderCausewayValue({value: row, descriptor: null}, this._rendererRegistry);
      return `<li>${rendered.html}</li>`;
    }).join('')}</ul>`;
  }

  #renderTable(rows) {
    const header = `<th scope="col">Item</th>${this._columns.map(column => `<th scope="col"${column.testId ? ` data-testid="${escapeHtml(column.testId)}"` : ''}>${escapeHtml(column.label || humanize(column.member))}</th>`).join('')}`;
    const body = rows.map((row, rowIndex) => {
      const metadata = row?._meta;
      const key = rowKey(row, rowIndex);
      const item = `<td data-causeway-grid-row-key="${escapeHtml(key)}" data-causeway-grid-member="_meta" data-causeway-grid-role="object-link">${validIdentity(metadata) ? objectLink(metadata) : '<span class="causeway-unavailable">Unavailable</span>'}</td>`;
      const cells = this._columns.map(column => this.#renderCell(row, rowIndex, column, key)).join('');
      return `<tr data-row-index="${rowIndex}" data-causeway-grid-row-key="${escapeHtml(key)}">${item}${cells}</tr>`;
    }).join('');
    return `<table class="causeway-collection-table"><thead><tr>${header}</tr></thead><tbody>${body}</tbody></table>`;
  }

  #renderCell(row, rowIndex, column, key) {
    const identity = ` data-causeway-grid-row-key="${escapeHtml(key)}" data-causeway-grid-member="${escapeHtml(column.member)}" data-causeway-grid-role="cell"`;
    if (!row || !Object.prototype.hasOwnProperty.call(row, column.member)) {
      return `<td${identity}><span class="causeway-unavailable">Unavailable</span></td>`;
    }
    const property = row[column.member];
    const error = property?.errors?.[0]?.message ?? property?.error?.message ?? null;
    if (error) return `<td${identity} class="causeway-error" role="alert">${escapeHtml(error)}</td>`;
    if (property?.hidden === true) return `<td${identity} hidden></td>`;
    const disabledReason = typeof property?.disabled === 'string' ? property.disabled : '';
    const value = property && typeof property === 'object' && Object.prototype.hasOwnProperty.call(property, 'get')
      ? property.get
      : property;
    const rendered = renderCausewayValue({value, descriptor: null}, this._rendererRegistry);
    return `<td${identity}${disabledReason ? ` aria-disabled="true" title="${escapeHtml(disabledReason)}"` : ''}>${rendered.html}</td>`;
  }

  #qualifyGrid() {
    const rows = this.resultState.rows;
    const policy = causewayGridWidgetConfiguration();
    this._gridProjection = buildCausewayGridProjection({
      rows,
      columns: this._columns,
      rendererRegistry: this._rendererRegistry
    });
    const columnsSupported = this._columns.every(column => rows.every(row => row
      && Object.prototype.hasOwnProperty.call(row, column.member)));
    const window = finiteWindow(rows.length);
    this._gridQualification = qualifyCausewayCollectionGrid({
      active: true,
      visible: true,
      wide: this._gridWide,
      ready: this.resultState.status === 'ready',
      policyEnabled: policy.enabled,
      familyHealthy: !policy.failed,
      connected: this.isConnected,
      columnsSupported,
      hasVisibleColumn: this._gridProjection.columns.length > 0 && rows.every(row => validIdentity(row?._meta)),
      renderersSupported: this._gridProjection.supported,
      bounded: false,
      orderingBasis: 'CONFIGURED',
      rows,
      window,
      lifecycle: {
        hostRevision: this.gridHostRevision,
        responsiveRevision: this.gridResponsiveRevision,
        policyRevision: policy.revision,
        rendererRevision: this.gridHostRevision
      }
    });
    publishCausewayGridDiagnostics(this, this._gridQualification);
  }

  #installGrid(describedBy) {
    const adapter = this.#primary()?.querySelector?.(`${CAUSEWAY_COLLECTION_GRID}[data-causeway-collection-grid]`);
    if (!adapter || !this.isConnected) return;
    const generation = this.resultGeneration;
    const hostRevision = this.gridHostRevision;
    const totalCount = this._gridProjection.rows.length;
    adapter.presentation = {
      mode: 'virtual',
      rows: this._gridProjection.rows,
      columns: this._gridProjection.columns,
      totalCount,
      pageSize: Math.max(totalCount, 1),
      labelledBy: this.labelId,
      describedBy,
      testId: this.getAttribute('data-testid') ? `${this.getAttribute('data-testid')}-grid` : '',
      resizableColumns: this.resizableColumns,
      reorderableColumns: this.reorderableColumns,
      sortableMembers: [],
      sortCriterion: null,
      rangeProvider: request => this.requestResultRange(request, generation, hostRevision)
    };
  }

  #primary() {
    let primary = [...(this.children ?? [])].find(child => child.hasAttribute?.('data-causeway-standalone-primary'));
    if (!primary && globalThis.document?.createElement) {
      primary = document.createElement('div');
      primary.setAttribute('data-causeway-standalone-primary', '');
      this.appendChild(primary);
    }
    return primary ?? null;
  }

  #acceptColumn(column) {
    if (!column?.member) return;
    const frozen = Object.freeze({...column});
    const existing = this._columns.findIndex(candidate => candidate.member === frozen.member);
    if (existing >= 0 && sameColumn(this._columns[existing], frozen)) return;
    if (existing >= 0) this._columns.splice(existing, 1, frozen);
    else this._columns.push(frozen);
    this.gridHostRevision += 1;
    this.render();
  }

  #captureColumns({replace = false} = {}) {
    const captured = initialColumnConfigurations.get(this) ?? declarativeColumns(this);
    initialColumnConfigurations.delete(this);
    if (replace) this._columns = [];
    for (const column of captured) {
      if (!this._columns.some(candidate => candidate.member === column.member)) {
        this._columns.push(Object.freeze({...column}));
      }
    }
  }

  #publishState() {
    this.dispatchEvent(createSemanticEvent(CausewaySemanticEvent.COLLECTION_STATE, Object.freeze({
      element: this,
      member: null,
      standalone: true,
      generation: this.resultGeneration,
      state: this.resultState
    })));
  }
}

function declarativeColumns(collection) {
  return [...(collection.children ?? collection.childNodes ?? [])]
    .filter(child => child?.localName === 'cw-collection-column' || child?.configuration?.member)
    .map(child => ({
      member: child.configuration?.member ?? child.id ?? '',
      label: child.configuration?.label ?? child.getAttribute?.('label') ?? '',
      testId: child.configuration?.testId ?? child.getAttribute?.('data-testid') ?? null
    }))
    .filter(column => column.member);
}

function normalizeColumns(value) {
  return [...(value ?? [])]
    .map(column => typeof column === 'string' ? {member: column, label: humanize(column)} : {...column})
    .filter(column => column.member)
    .map(column => Object.freeze(column));
}

function sameColumn(left, right) {
  return left.member === right.member && left.label === right.label && left.testId === right.testId;
}

function frozenState(status, rows = [], errors = []) {
  return Object.freeze({
    status,
    rows: Object.freeze([...rows]),
    totalCount: rows.length,
    errors: Object.freeze(errors.map(error => Object.freeze({message: String(error?.message ?? error)})))
  });
}

function finiteWindow(count) {
  const size = Math.max(count, 1);
  return Object.freeze({
    offset: 0,
    requestedSize: size,
    maximumSize: size,
    returnedCount: count,
    totalCount: count,
    hasPrevious: false,
    hasNext: false,
    ordering: 'CONFIGURED'
  });
}

function validIdentity(metadata) {
  return Boolean(metadata?.logicalTypeName && metadata?.id);
}

function objectLink(metadata) {
  return `<cw-object-link logical-type="${escapeHtml(metadata.logicalTypeName)}" object-id="${escapeHtml(metadata.id)}" title="${escapeHtml(metadata.title ?? metadata.id)}"${metadata.icon ? ` icon="${escapeHtml(metadata.icon)}"` : ''}></cw-object-link>`;
}

function rowKey(row, index) {
  const metadata = row?._meta;
  return validIdentity(metadata) ? `${metadata.logicalTypeName}:${metadata.id}` : `result:${index}`;
}

function humanize(value) {
  return String(value || '').replace(/([a-z0-9])([A-Z])/g, '$1 $2').replace(/^./, character => character.toUpperCase());
}

function normalizedText(value) {
  return String(value ?? '').trim().toLocaleLowerCase();
}

function setOptionalAttribute(element, name, value) {
  if (value == null || value === '') element.removeAttribute(name);
  else element.setAttribute(name, String(value));
}

function setBooleanAttribute(element, name, value) {
  if (value === true) element.setAttribute(name, '');
  else element.removeAttribute(name);
}

function collectionBreakpointPixels() {
  const candidate = globalThis.getComputedStyle?.(globalThis.document?.documentElement)?.fontSize;
  const rootFontSize = Number.parseFloat(candidate);
  return 48 * (Number.isFinite(rootFontSize) && rootFontSize > 0 ? rootFontSize : 16);
}
