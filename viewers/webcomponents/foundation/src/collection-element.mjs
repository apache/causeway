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
import {CausewayCollectionRangeBroker} from './collection-range-broker.mjs';
import {CausewaySemanticEvent} from './component-contracts.mjs';
import {CausewayContextConsumerElement} from './context-consumer-element.mjs';
import {createSemanticEvent} from './context-events.mjs';
import {
  boundedTooltipSection,
  composeMemberTooltip,
  DescriptionPresentation,
  normalizeDescriptionPresentation
} from './description-presentation.mjs';
import {
  connectMemberComposition,
  disconnectMemberComposition,
  eventOriginatesFromAssociatedAction,
  refreshMemberComposition,
  renderMemberPrimary
} from './member-composition.mjs';
import {errorMessage, escapeHtml} from './rendering.mjs';
import {
  CAUSEWAY_COLLECTION_GRID,
  CAUSEWAY_GRID_WIDGET_POLICY_EVENT,
  causewayGridWidgetConfiguration,
  failCausewayGridWidget
} from './grid-widget.mjs';
import {defaultValueRendererRegistry, renderCausewayValue} from './value-renderers.mjs';

let collectionSequence = 0;
const initialColumnConfigurations = new WeakMap();

export function captureDeclarativeCollectionColumns(root = globalThis.document) {
  if (!root?.querySelectorAll) {
    return;
  }
  const collections = root.localName === 'cw-collection'
    ? [root]
    : root.querySelectorAll('cw-collection');
  for (const collection of collections) {
    const columns = [...(collection.children ?? collection.childNodes ?? [])]
      .filter(child => child.localName === 'cw-collection-column' || child?.configuration?.member)
      .map(child => ({
        member: child.id || '',
        label: child.getAttribute('label') || '',
        testId: child.getAttribute('data-testid') || null
      }))
      .filter(column => column.member);
    if (columns.length > 0) {
      initialColumnConfigurations.set(collection, columns);
    }
  }
}

export class CausewayCollectionElement extends CausewayContextConsumerElement {
  static get observedAttributes() {
    return [
      'id',
      'named',
      'described-as',
      'description-as',
      'label',
      'active',
      'paged',
      'sortable',
      'filterable',
      'resizable-columns',
      'reorderable-columns'
    ];
  }

  constructor() {
    super();
    const sequence = ++collectionSequence;
    this.labelId = `causeway-collection-label-${sequence}`;
    this.descriptionId = `causeway-collection-description-${sequence}`;
    this.reasonId = `causeway-collection-reason-${sequence}`;
    this.searchId = `causeway-collection-search-${sequence}`;
    this._columns = [];
    this._rendererRegistry = defaultValueRendererRegistry;
    this.collectionState = Object.freeze({status: 'idle', data: null, errors: []});
    this.authoritativeResult = null;
    this.rangeBroker = null;
    this.rowContexts = [];
    this.loadRevision = 0;
    this.loadAbortController = null;
    this.loadedGeneration = -1;
    this.sortCriterion = null;
    this._gridOrderingBasis = null;
    this.searchText = '';
    this.searchTimer = null;
    this.criteriaFocusIntent = null;
    this.connectionStarted = false;
    this.gridHostRevision = 0;
    this.gridResponsiveRevision = 0;
    this._gridWide = false;
    this._gridProjection = buildCausewayGridProjection();
    this._gridQualification = qualifyCausewayCollectionGrid();
    this.gridFocusIntent = null;
    this.gridResizeRevision = 0;
    this.gridResizeObserver = typeof globalThis.ResizeObserver === 'function'
      ? new ResizeObserver(entries => {
        const entry = entries.at(-1);
        const width = entry?.contentRect?.width;
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
    this.gridPolicyListener = () => {
      this.gridHostRevision += 1;
      this.#rebuildRangeBroker();
      if (this.isConnected && this.componentState) this.renderComponentState(this.componentState);
    };
    this.columnObserver = typeof globalThis.MutationObserver === 'function'
      ? new MutationObserver(records => {
        for (const record of records) {
          for (const node of record.addedNodes ?? []) {
            if (node.localName === 'cw-collection-column' && node.configuration?.member) {
              this.#acceptColumn(node.configuration);
            }
          }
        }
      })
      : null;
    this.columnObserver?.observe(this, {childList: true});
    this.addEventListener(CausewaySemanticEvent.COLLECTION_CONFIGURATION, event => {
      this.#acceptColumn(event.detail?.column);
    });
    this.addEventListener('click', event => {
      if (eventOriginatesFromAssociatedAction(this, event.target)) return;
      if (event.target?.hasAttribute?.('data-causeway-activate')) {
        this.activate();
      } else if (event.target?.hasAttribute?.('data-causeway-grid-previous')) {
        this.#loadNormalizedPage(this.collectionState.window?.previousOffset);
      } else if (event.target?.hasAttribute?.('data-causeway-grid-next')) {
        this.#loadNormalizedPage(this.collectionState.window?.nextOffset);
      } else if (event.target?.hasAttribute?.('data-causeway-collection-sort')) {
        this.#changeSort(event.target.getAttribute('data-causeway-collection-sort'));
      } else if (event.target?.hasAttribute?.('data-causeway-collection-search-clear')) {
        this.#commitSearch('');
      }
    });
    this.addEventListener('input', event => {
      if (!event.target?.hasAttribute?.('data-causeway-collection-search')) return;
      const value = String(event.target.value ?? '').slice(0, MAX_COLLECTION_SEARCH_LENGTH);
      globalThis.clearTimeout(this.searchTimer);
      this.searchTimer = globalThis.setTimeout(() => this.#commitSearch(value), COLLECTION_SEARCH_DEBOUNCE_MS);
    });
    this.addEventListener('keydown', event => {
      if (event.key !== 'Escape' || !event.target?.hasAttribute?.('data-causeway-collection-search')) return;
      event.preventDefault?.();
      globalThis.clearTimeout(this.searchTimer);
      event.target.value = '';
      if (!this.#commitSearch('')) {
        this.criteriaFocusIntent = 'search';
        this.renderComponentState(this.componentState);
      }
    });
    this.addEventListener('focusin', event => {
      const target = event.composedPath?.().find(node => node?.dataset?.causewayGridRowKey) ?? event.target;
      if (!target?.dataset?.causewayGridRowKey) return;
      this.gridFocusIntent = Object.freeze({
        rowKey: target.dataset.causewayGridRowKey,
        member: target.dataset.causewayGridMember ?? '',
        role: target.dataset.causewayGridRole ?? 'cell',
        objectGeneration: this.componentState?.generation,
        collectionMember: this.id
      });
    });
    this.addEventListener('causeway-grid-ready', event => {
      if (this.criteriaFocusIntent) {
        const restored = event.target?.restoreSortFocus?.(this.criteriaFocusIntent);
        if (restored) this.criteriaFocusIntent = null;
      } else if (this.#focusIntentIsCurrent()) {
        event.target?.restoreSemanticFocus?.(this.gridFocusIntent);
      }
    });
  }

  get named() {
    return this.getAttribute('named') || '';
  }

  set named(value) {
    this.setAttribute('named', value);
  }

  get describedAs() {
    return this.getAttribute('described-as') || '';
  }

  set describedAs(value) {
    this.setAttribute('described-as', value);
  }

  get descriptionAs() {
    return normalizeDescriptionPresentation(this.getAttribute('description-as'));
  }

  set descriptionAs(value) {
    this.setAttribute('description-as', value);
  }

  get label() {
    return this.getAttribute('label') || '';
  }

  set label(value) {
    this.setAttribute('label', value);
  }

  get active() {
    return this.hasAttribute('active');
  }

  set active(value) {
    if (value) {
      this.setAttribute('active', '');
    } else {
      this.removeAttribute('active');
    }
  }

  get paged() {
    return normalizedPageSize(this.getAttribute('paged'));
  }

  set paged(value) {
    if (value == null || value === '') {
      this.removeAttribute('paged');
    } else {
      this.setAttribute('paged', String(value));
    }
  }

  get sortable() {
    return this.hasAttribute('sortable');
  }

  set sortable(value) {
    if (value === true) this.setAttribute('sortable', '');
    else this.removeAttribute('sortable');
  }

  get filterable() {
    return this.hasAttribute('filterable');
  }

  set filterable(value) {
    if (value === true) this.setAttribute('filterable', '');
    else this.removeAttribute('filterable');
  }

  get resizableColumns() {
    return this.hasAttribute('resizable-columns');
  }

  set resizableColumns(value) {
    if (value === true) this.setAttribute('resizable-columns', '');
    else this.removeAttribute('resizable-columns');
  }

  get reorderableColumns() {
    return this.hasAttribute('reorderable-columns');
  }

  set reorderableColumns(value) {
    if (value === true) this.setAttribute('reorderable-columns', '');
    else this.removeAttribute('reorderable-columns');
  }

  get columns() {
    return Object.freeze(this._columns.map(column => Object.freeze({...column})));
  }

  set columns(value) {
    this.gridHostRevision += 1;
    this.#disconnectRangeBroker();
    this._columns = [...(value ?? [])]
      .map(column => typeof column === 'string' ? {member: column, label: humanize(column)} : {...column})
      .filter(column => column.member);
    if (this.sortCriterion && !this._columns.some(column => column.member === this.sortCriterion.member)) {
      this.sortCriterion = null;
      this.criteriaFocusIntent = null;
    }
    if (this.active && this.componentState?.status === 'ready') {
      void this.load({force: true});
    } else if (this.componentState) {
      this.renderComponentState(this.componentState);
    }
  }

  get gridQualification() {
    return this._gridQualification;
  }

  get gridProjection() {
    return this._gridProjection;
  }

  acceptGridResponsiveState(wide) {
    const next = wide === true;
    if (next === this._gridWide) return false;
    this._gridWide = next;
    this.gridResponsiveRevision += 1;
    this.gridHostRevision += 1;
    this.#rebuildRangeBroker();
    if (this.componentState) this.renderComponentState(this.componentState);
    return true;
  }

  get rendererRegistry() {
    return this._rendererRegistry;
  }

  set rendererRegistry(value) {
    this._rendererRegistry = value ?? defaultValueRendererRegistry;
    this.gridHostRevision += 1;
    this.#rebuildRangeBroker();
    if (this.componentState) {
      this.renderComponentState(this.componentState);
    }
  }

  connectedCallback() {
    connectMemberComposition(this, {primaryPlacement: 'last'});
    globalThis.document?.addEventListener?.(CAUSEWAY_GRID_WIDGET_POLICY_EVENT, this.gridPolicyListener);
    this.columnObserver?.observe(this, {childList: true});
    globalThis.setTimeout(() => {
      if (!this.isConnected) {
        return;
      }
      captureDeclarativeCollectionColumns(this);
      this.#captureColumns();
      this.connectionStarted = true;
      this.gridResizeObserver?.observe(this);
      super.connectedCallback();
    }, 0);
  }

  disconnectedCallback() {
    disconnectMemberComposition(this);
    globalThis.document?.removeEventListener?.(CAUSEWAY_GRID_WIDGET_POLICY_EVENT, this.gridPolicyListener);
    this.columnObserver?.disconnect();
    this.gridResizeObserver?.disconnect();
    this.gridResizeRevision += 1;
    this.connectionStarted = false;
    this.loadRevision += 1;
    this.loadAbortController?.abort();
    this.loadAbortController = null;
    globalThis.clearTimeout(this.searchTimer);
    this.searchTimer = null;
    this.#disconnectRangeBroker();
    for (const context of this.rowContexts) {
      context.disconnect?.();
    }
    this.rowContexts = [];
    this.gridHostRevision += 1;
    super.disconnectedCallback();
  }

  attributeChangedCallback(name, oldValue, newValue) {
    if (oldValue === newValue || !this.connectionStarted) {
      return;
    }
    if (name === 'id') {
      this.gridHostRevision += 1;
      refreshMemberComposition(this);
      this.loadAbortController?.abort();
      this.loadAbortController = null;
      this.#disconnectRangeBroker();
      this.authoritativeResult = null;
      this.sortCriterion = null;
      this._gridOrderingBasis = null;
      this.searchText = '';
      this.criteriaFocusIntent = null;
      this.collectionState = Object.freeze({status: 'idle', data: null, errors: []});
      this.loadedGeneration = -1;
      this.reconnectRequirement();
    } else if (name === 'active' && this.active && this.componentState?.status === 'ready') {
      void this.load();
    } else if (name === 'paged') {
      this.gridHostRevision += 1;
      this.loadAbortController?.abort();
      this.loadAbortController = null;
      this.#disconnectRangeBroker();
      if (this.active && this.componentState?.status === 'ready') {
        void this.load({force: true, offset: 0, size: this.paged});
      } else {
        this.renderComponentState(this.componentState);
      }
    } else if (name === 'sortable' || name === 'filterable') {
      if (!this.sortable) this.sortCriterion = null;
      if (!this.filterable) this.searchText = '';
      this.criteriaFocusIntent = null;
      this.#reloadCriteria();
    } else {
      if (name === 'resizable-columns' || name === 'reorderable-columns') {
        this.gridHostRevision += 1;
      }
      this.renderComponentState(this.componentState);
    }
  }

  createRequirement() {
    return {kind: 'collection', member: this.id};
  }

  acceptComponentState(state) {
    super.acceptComponentState(state);
    if (state.status === 'ready' && this.active && this.loadedGeneration !== state.generation) {
      void this.load({force: this.loadedGeneration >= 0});
    }
  }

  activate() {
    if (this.componentState?.data?.hidden === true) {
      return false;
    }
    if (!this.active) {
      this.active = true;
    } else if (this.componentState?.status === 'ready') {
      void this.load();
    }
    return true;
  }

  async load({force = false, offset = 0, size = null} = {}) {
    const context = this._resolvedContext;
    const requestedSize = size ?? this.paged;
    if (!context || this.componentState?.status !== 'ready') {
      return null;
    }
    const revision = ++this.loadRevision;
    this.loadAbortController?.abort();
    this.#disconnectRangeBroker();
    this.authoritativeResult = null;
    const abortController = new AbortController();
    this.loadAbortController = abortController;
    this.collectionState = Object.freeze({...this.collectionState, status: 'loading', data: null, errors: []});
    this.renderComponentState(this.componentState);
    this.#publishCollectionState();
    try {
      const result = await context.loadCollection({
        member: this.id,
        columns: this._columns,
        offset,
        size: requestedSize,
        sortBy: this.sortable ? this.sortCriterion?.member ?? null : null,
        sortDirection: this.sortable ? this.sortCriterion?.direction ?? 'ASCENDING' : 'ASCENDING',
        search: this.filterable ? this.searchText : null,
        requestKey: this,
        force,
        signal: abortController.signal
      });
      if (revision !== this.loadRevision || !this.isConnected) {
        return null;
      }
      for (const rowContext of this.rowContexts) {
        rowContext.disconnect?.();
      }
      const rows = collectionRows(result);
      this.rowContexts = rows
        .filter(row => row?._meta?.logicalTypeName && row?._meta?.id)
        .map(row => context.createHydratedRowContext(row, result.rowSelection));
      this.loadedGeneration = this.componentState.generation;
      this.gridHostRevision += 1;
      this.authoritativeResult = result;
      if (!this.sortCriterion) {
        this._gridOrderingBasis = result.window?.ordering ?? null;
      }
      this.collectionState = Object.freeze({
        status: result.errors.length > 0 ? 'partial-error' : 'ready',
        data: result.data,
        rows,
        window: result.window,
        errors: result.errors,
        descriptor: result.descriptor,
        rowDescription: result.rowDescription,
        rowSelection: result.rowSelection
      });
      this.renderComponentState(this.componentState);
      if (this._gridQualification.qualified) this.#installRangeBroker(context, result);
      this.#publishCollectionState();
      if (this.loadAbortController === abortController) {
        this.loadAbortController = null;
      }
      return result;
    } catch (error) {
      if (revision !== this.loadRevision || error?.name === 'AbortError') {
        return null;
      }
      if (this.loadAbortController === abortController) {
        this.loadAbortController = null;
      }
      this.authoritativeResult = null;
      this.collectionState = Object.freeze({
        status: 'error',
        data: null,
        errors: Object.freeze([{message: error?.message ?? String(error), path: [], extensions: {}}])
      });
      this.renderComponentState(this.componentState);
      this.#publishCollectionState();
      return null;
    }
  }

  renderComponentState(state) {
    if (!state) {
      return;
    }
    this.#qualifyGrid(state);
    const label = firstNonBlank(this.named, this.label, state.data?.metadata?.friendlyName, humanize(this.id));
    const candidateDescription = firstNonBlank(this.describedAs, state.data?.metadata?.description);
    const description = normalizedText(candidateDescription) === normalizedText(label)
      ? ''
      : candidateDescription;
    const descriptionPresentation = normalizeDescriptionPresentation(this.getAttribute('description-as'));
    const disabledReason = boundedTooltipSection(typeof state.data?.disabled === 'string'
      ? state.data.disabled
      : state.data?.disabled === true ? 'Disabled' : '');
    const describedBy = [description ? this.descriptionId : '', disabledReason ? this.reasonId : '']
      .filter(Boolean)
      .join(' ');
    const tooltipDescription = descriptionPresentation === DescriptionPresentation.TOOLTIP || disabledReason
      ? description
      : '';
    const tooltip = composeMemberTooltip(tooltipDescription, disabledReason);
    const shell = (content, attributes = '') => collectionShell({
      labelId: this.labelId,
      label,
      descriptionId: this.descriptionId,
      description,
      descriptionVisible: descriptionPresentation === DescriptionPresentation.LABEL,
      reasonId: this.reasonId,
      disabledReason,
      describedBy,
      tooltip
    }, content, attributes);
    if (['idle', 'schema-loading', 'object-loading'].includes(state.status)) {
      renderMemberPrimary(this, shell('<span role="status">Loading collection metadata…</span>', ' aria-busy="true"'));
      return;
    }
    const recoverablePartialState = state.status === 'partial-error'
      && state.data
      && typeof state.data.hidden === 'boolean';
    if (['terminal-error', 'unsupported'].includes(state.status)
        || (state.status === 'partial-error' && !recoverablePartialState)) {
      renderMemberPrimary(this, shell(`<span class="causeway-error" role="alert">${escapeHtml(errorMessage(state))}</span>`));
      return;
    }
    if (state.data?.hidden === true) {
      renderMemberPrimary(this, '', {hidden: true});
      return;
    }
    if (!this.active) {
      renderMemberPrimary(this, shell(`<button type="button" data-causeway-activate>Load ${escapeHtml(label)}</button>`));
      return;
    }
    const criteriaControls = this.#renderCriteriaControls();
    if (this.collectionState.status === 'idle' || this.collectionState.status === 'loading') {
      renderMemberPrimary(this, shell(`${criteriaControls}<span role="status">Loading collection…</span>`, ' aria-busy="true"'));
      return;
    }
    if (this.collectionState.status === 'error') {
      renderMemberPrimary(this, shell(`${criteriaControls}<span class="causeway-error" role="alert">${escapeHtml(errorMessage(this.collectionState))}</span>`));
      return;
    }
    const rows = collectionRows(this.collectionState);
    if (rows.length === 0) {
      const pager = this.paged ? this.#renderBoundedPager(this.collectionState.window) : '';
      renderMemberPrimary(this, shell(`${criteriaControls}<span class="causeway-empty" role="status">No items</span>${pager}`));
      this.#restoreCriteriaFocus();
      return;
    }
    const errors = this.collectionState.errors?.length
      ? `<p class="causeway-error" role="alert">${escapeHtml(errorMessage(this.collectionState))}</p>`
      : '';
    if (this._gridQualification.qualified) {
      this.#renderGrid(shell, errors, describedBy);
      return;
    }
    const content = this._columns.length > 0
      ? this.#renderTable(rows)
      : this.#renderDefaultRows(rows);
    const pager = this.paged ? this.#renderBoundedPager(this.collectionState.window) : '';
    renderMemberPrimary(this, shell(`${criteriaControls}${content}${pager}${errors}`));
    this.#restoreNativeFocus();
    this.#restoreCriteriaFocus();
  }

  #renderGrid(shell, errors, describedBy) {
    const revision = this.gridHostRevision;
    const bounded = this._gridQualification.presentation === 'grid-bounded';
    const window = this.collectionState.window;
    const pager = bounded ? this.#renderBoundedPager(window) : '';
    renderMemberPrimary(this, shell(`${this.#renderCriteriaControls()}<${CAUSEWAY_COLLECTION_GRID} data-causeway-collection-grid></${CAUSEWAY_COLLECTION_GRID}>${pager}${errors}`));
    const adapter = this.querySelector?.(`${CAUSEWAY_COLLECTION_GRID}[data-causeway-collection-grid]`);
    if (!adapter || revision !== this.gridHostRevision || !this.isConnected) return;
    adapter.presentation = {
      mode: bounded ? 'bounded' : 'virtual',
      rows: this._gridProjection.rows,
      columns: this._gridProjection.columns,
      totalCount: window.totalCount,
      pageSize: window.requestedSize,
      labelledBy: this.labelId,
      describedBy,
      testId: this.getAttribute('data-testid') ? `${this.getAttribute('data-testid')}-grid` : '',
      resizableColumns: this.resizableColumns,
      reorderableColumns: this.reorderableColumns,
      sortableMembers: this.sortable ? this.collectionState.window?.sortableMembers ?? [] : [],
      sortCriterion: this.sortCriterion,
      sortCallback: member => this.#changeSort(member),
      rangeProvider: request => this.#projectRange(request, revision)
    };
    this.#restoreCriteriaFocus();
  }

  async #projectRange(request, revision) {
    const result = await this.requestCollectionRange(request);
    if (revision !== this.gridHostRevision || !this.isConnected || !this._gridQualification.qualified) {
      const error = new Error('Collection Grid range was superseded.');
      error.name = 'AbortError';
      throw error;
    }
    if (!sameWindowContract(this.collectionState.window, result.window)) {
      this.gridHostRevision += 1;
      this.#disconnectRangeBroker();
      void this.load({force: true, offset: 0, size: this.collectionState.window?.requestedSize});
      const error = new Error('Collection Grid range contract changed.');
      error.name = 'AbortError';
      throw error;
    }
    const projection = buildCausewayGridProjection({
      rows: result.rows,
      columns: this._columns,
      rowDescription: result.rowDescription,
      errors: result.errors,
      rendererRegistry: this._rendererRegistry
    });
    if (!projection.supported) {
      failCausewayGridWidget({phase: 'renderer', classification: 'GRID_RANGE_PROJECTION_UNAVAILABLE'});
      throw new Error('Collection Grid range projection is unavailable.');
    }
    return Object.freeze({rows: projection.rows});
  }

  #renderCriteriaControls() {
    const window = this.collectionState.window;
    if (!this.filterable || window?.searchSupported !== true) return '';
    const prompt = window.searchPrompt || 'Search collection';
    return `<div class="causeway-collection-search" role="search">
      <label for="${this.searchId}">${escapeHtml(prompt)}</label>
      <input id="${this.searchId}" type="search" maxlength="${MAX_COLLECTION_SEARCH_LENGTH}" value="${escapeHtml(this.searchText)}" data-causeway-collection-search autocomplete="off">
      <button type="button" data-causeway-collection-search-clear${this.searchText ? '' : ' disabled aria-disabled="true"'}>Clear</button>
    </div>`;
  }

  #changeSort(member) {
    const sortableMembers = new Set(this.collectionState.window?.sortableMembers ?? []);
    if (!this.sortable || !sortableMembers.has(member) || !this._columns.some(column => column.member === member)) {
      return false;
    }
    const current = this.sortCriterion?.member === member ? this.sortCriterion.direction : null;
    this.sortCriterion = current === 'ASCENDING'
      ? Object.freeze({member, direction: 'DESCENDING'})
      : current === 'DESCENDING' ? null : Object.freeze({member, direction: 'ASCENDING'});
    this.criteriaFocusIntent = member;
    this.#reloadCriteria();
    return true;
  }

  #commitSearch(value) {
    const normalized = String(value ?? '').trim().slice(0, MAX_COLLECTION_SEARCH_LENGTH);
    if (!this.filterable || this.collectionState.window?.searchSupported !== true || normalized === this.searchText) {
      return false;
    }
    this.searchText = normalized;
    this.criteriaFocusIntent = 'search';
    this.#reloadCriteria();
    return true;
  }

  #reloadCriteria() {
    this.gridHostRevision += 1;
    this.loadAbortController?.abort();
    this.loadAbortController = null;
    this.#disconnectRangeBroker();
    if (this.active && this.componentState?.status === 'ready') {
      void this.load({force: true, offset: 0, size: this.paged ?? this.collectionState.window?.requestedSize});
    } else if (this.componentState) {
      this.renderComponentState(this.componentState);
    }
  }

  #restoreCriteriaFocus() {
    const intent = this.criteriaFocusIntent;
    if (!intent || !this.querySelector) return;
    const target = intent === 'search'
      ? this.querySelector('[data-causeway-collection-search]')
      : this.querySelector(`[data-causeway-collection-sort="${intent}"]`);
    if (!target) return;
    queueMicrotask(() => {
      if (!this.isConnected || this.criteriaFocusIntent !== intent) return;
      target.focus?.();
      if (intent === 'search') target.setSelectionRange?.(target.value.length, target.value.length);
      this.criteriaFocusIntent = null;
    });
  }

  #renderBoundedPager(window) {
    if (!window) return '';
    const total = Number.isSafeInteger(window.totalCount) && window.totalCount >= 0
      ? ` of ${window.totalCount}`
      : '';
    const range = window.rangeStart == null
      ? 'No items'
      : `Items ${window.rangeStart}–${window.rangeEnd}${total}`;
    return `<nav class="causeway-collection-pager" aria-label="Collection pages">
      <button type="button" data-causeway-grid-previous${window.hasPrevious ? '' : ' disabled aria-disabled="true"'}>Previous</button>
      <span class="causeway-collection-range" aria-live="polite">${range}</span>
      <button type="button" data-causeway-grid-next${window.hasNext ? '' : ' disabled aria-disabled="true"'}>Next</button>
    </nav>`;
  }

  #loadNormalizedPage(offset) {
    if (!Number.isSafeInteger(offset) || offset < 0) return false;
    const size = this.collectionState.window?.requestedSize;
    void this.load({offset, size});
    return true;
  }

  #renderDefaultRows(rows) {
    return `<ul class="causeway-collection-rows">${rows.map(row => {
      const metadata = row?._meta ?? {};
      return `<li><cw-object-link logical-type="${escapeHtml(metadata.logicalTypeName ?? '')}" object-id="${escapeHtml(metadata.id ?? '')}" title="${escapeHtml(metadata.title ?? metadata.id ?? '')}"${metadata.icon ? ` icon="${escapeHtml(metadata.icon)}"` : ''}></cw-object-link></li>`;
    }).join('')}</ul>`;
  }

  #renderTable(rows) {
    const header = `<th scope="col">Item</th>${this._columns.map(column => this.#renderColumnHeader(column)).join('')}`;
    const body = rows.map((row, rowIndex) => {
      const metadata = row?._meta ?? {};
      const key = gridRowKey(row);
      const item = `<td data-causeway-grid-row-key="${escapeHtml(key)}" data-causeway-grid-member="_meta" data-causeway-grid-role="object-link"><cw-object-link logical-type="${escapeHtml(metadata.logicalTypeName ?? '')}" object-id="${escapeHtml(metadata.id ?? '')}" title="${escapeHtml(metadata.title ?? metadata.id ?? '')}"${metadata.icon ? ` icon="${escapeHtml(metadata.icon)}"` : ''}></cw-object-link></td>`;
      const cells = this._columns.map(column => this.#renderCell(row, rowIndex, column)).join('');
      return `<tr data-row-index="${rowIndex}" data-causeway-grid-row-key="${escapeHtml(key)}">${item}${cells}</tr>`;
    }).join('');
    return `<table class="causeway-collection-table"><thead><tr>${header}</tr></thead><tbody>${body}</tbody></table>`;
  }

  #renderColumnHeader(column) {
    const label = column.label || humanize(column.member);
    const sortable = this.sortable
      && this.collectionState.window?.sortableMembers?.includes?.(column.member);
    const currentDirection = this.sortCriterion?.member === column.member
      ? this.sortCriterion.direction
      : null;
    const ariaSort = currentDirection === 'ASCENDING'
      ? 'ascending'
      : currentDirection === 'DESCENDING' ? 'descending' : 'none';
    const direction = currentDirection === 'ASCENDING' ? '↑' : currentDirection === 'DESCENDING' ? '↓' : '↕';
    const testId = column.testId ? ` data-testid="${escapeHtml(column.testId)}"` : '';
    return sortable
      ? `<th scope="col" aria-sort="${ariaSort}"${testId}><button type="button" data-causeway-collection-sort="${escapeHtml(column.member)}" aria-label="Sort ${escapeHtml(label)}${currentDirection === 'ASCENDING' ? ' descending' : currentDirection === 'DESCENDING' ? ' off' : ' ascending'}">${escapeHtml(label)}<span class="causeway-collection-sort-indicator" aria-hidden="true">${direction}</span></button></th>`
      : `<th scope="col"${testId}>${escapeHtml(label)}</th>`;
  }

  #renderCell(row, rowIndex, column) {
    const property = row?.[column.member] ?? null;
    const errors = (this.collectionState.errors ?? []).filter(error => {
      const path = error.path ?? [];
      return path.includes(rowIndex) && path.includes(column.member);
    });
    const identity = ` data-causeway-grid-row-key="${escapeHtml(gridRowKey(row))}" data-causeway-grid-member="${escapeHtml(column.member)}" data-causeway-grid-role="cell"`;
    if (errors.length > 0) {
      return `<td${identity} class="causeway-error" role="alert">${escapeHtml(errors[0].message)}</td>`;
    }
    if (property?.hidden === true) {
      return `<td${identity} hidden></td>`;
    }
    const disabledReason = typeof property?.disabled === 'string' ? property.disabled : '';
    const descriptor = this.collectionState.rowDescription?.members?.get(column.member) ?? null;
    const rendered = renderCausewayValue({value: property?.get, descriptor}, this._rendererRegistry);
    return `<td${identity}${disabledReason ? ` aria-disabled="true" title="${escapeHtml(disabledReason)}"` : ''}>${rendered.html}</td>`;
  }

  requestCollectionRange({offset, size} = {}) {
    if (!this.rangeBroker) {
      return Promise.reject(new Error('The collection has no current bounded range broker.'));
    }
    return this.rangeBroker.request(offset, size);
  }

  #installRangeBroker(context, result) {
    this.#disconnectRangeBroker();
    const window = result?.window;
    const maximumRetainedRows = window?.maximumSize * 6;
    if (!window
        || !Number.isSafeInteger(window.maximumSize)
        || !Number.isSafeInteger(window.requestedSize)
        || !Number.isSafeInteger(maximumRetainedRows)) return;
    const member = this.id;
    const columns = this.columns;
    const sortBy = this.sortable ? this.sortCriterion?.member ?? null : null;
    const sortDirection = this.sortable ? this.sortCriterion?.direction ?? 'ASCENDING' : 'ASCENDING';
    const search = this.filterable ? this.searchText : null;
    const broker = new CausewayCollectionRangeBroker({
      maximumSize: window.maximumSize,
      defaultSize: window.requestedSize,
      maxConcurrent: 3,
      maxEntries: 6,
      maxRows: maximumRetainedRows,
      maxErrors: 20,
      loadRange: ({offset, size, signal, requestKey}) => context.loadCollection({
        member,
        columns,
        offset,
        size,
        sortBy,
        sortDirection,
        search,
        requestKey,
        cache: false,
        signal
      }),
      hydrate: range => range.rows
        .filter(row => row?._meta?.logicalTypeName && row?._meta?.id)
        .map(row => context.createHydratedRowContext(row, range.rowSelection))
    });
    broker.seed(result, {ownedContexts: false, contexts: this.rowContexts});
    this.rangeBroker = broker;
  }

  #rebuildRangeBroker() {
    const policy = causewayGridWidgetConfiguration();
    if (this.componentState) this.#qualifyGrid(this.componentState);
    if (!this.isConnected
        || !this._gridWide
        || !policy.enabled
        || policy.failed
        || !this._gridQualification.qualified
        || !this.authoritativeResult
        || !this._resolvedContext) {
      this.#disconnectRangeBroker();
      return;
    }
    this.#installRangeBroker(this._resolvedContext, this.authoritativeResult);
  }

  #disconnectRangeBroker() {
    this.rangeBroker?.disconnect();
    this.rangeBroker = null;
  }

  #qualifyGrid(state) {
    const rows = collectionRows(this.collectionState);
    const policy = causewayGridWidgetConfiguration();
    this._gridProjection = buildCausewayGridProjection({
      rows,
      columns: this._columns,
      rowDescription: this.collectionState.rowDescription,
      errors: this.collectionState.errors,
      rendererRegistry: this._rendererRegistry
    });
    const columnsSupported = this._columns.every(column => {
      if (!column?.member || typeof column.member !== 'string') return false;
      return this.collectionState.rowDescription?.members?.has?.(column.member) === true;
    });
    this._gridQualification = qualifyCausewayCollectionGrid({
      active: this.active,
      visible: state.data?.hidden !== true,
      wide: this._gridWide,
      ready: ['ready', 'partial-error'].includes(this.collectionState.status),
      policyEnabled: policy.enabled,
      familyHealthy: !policy.failed,
      connected: this.isConnected,
      columnsSupported,
      hasVisibleColumn: this._gridProjection.columns.length > 0
        && rows.some(row => row?._meta?.logicalTypeName && row?._meta?.id),
      renderersSupported: this._gridProjection.supported,
      bounded: this.paged != null,
      orderingBasis: this.sortCriterion ? this._gridOrderingBasis : null,
      rows,
      window: this.collectionState.window,
      lifecycle: {
        hostRevision: this.gridHostRevision,
        responsiveRevision: this.gridResponsiveRevision,
        policyRevision: policy.revision,
        rendererRevision: this.gridHostRevision
      }
    });
    publishCausewayGridDiagnostics(this, this._gridQualification);
  }

  #focusIntentIsCurrent() {
    const intent = this.gridFocusIntent;
    return Boolean(intent
      && intent.collectionMember === this.id
      && intent.objectGeneration === this.componentState?.generation);
  }

  #restoreNativeFocus() {
    if (!this.#focusIntentIsCurrent() || !this.querySelectorAll) return;
    const target = [...this.querySelectorAll('[data-causeway-grid-row-key][data-causeway-grid-member]')]
      .find(candidate => candidate.dataset.causewayGridRowKey === this.gridFocusIntent.rowKey
        && candidate.dataset.causewayGridMember === this.gridFocusIntent.member
        && candidate.hidden !== true);
    if (!target) return;
    const candidate = target.querySelector?.('cw-object-link, a, button, [tabindex]') ?? target;
    queueMicrotask(() => {
      if (this.isConnected && this.#focusIntentIsCurrent()) candidate.focus?.();
    });
  }

  #acceptColumn(column) {
    if (!column?.member) {
      return;
    }
    const frozen = Object.freeze({...column});
    const existing = this._columns.findIndex(candidate => candidate.member === column.member);
    if (existing >= 0 && sameColumnConfiguration(this._columns[existing], frozen)) {
      return;
    }
    this.gridHostRevision += 1;
    this.#disconnectRangeBroker();
    if (existing >= 0) {
      this._columns.splice(existing, 1, frozen);
    } else {
      this._columns.push(frozen);
    }
    if (this.active && this.componentState?.status === 'ready') {
      void this.load({force: true});
    }
  }

  #captureColumns() {
    for (const column of initialColumnConfigurations.get(this) ?? []) {
      if (!this._columns.some(candidate => candidate.member === column.member)) {
        this._columns.push(Object.freeze({...column}));
      }
    }
    initialColumnConfigurations.delete(this);
    for (const child of this.childNodes ?? []) {
      if (child?.localName !== 'cw-collection-column') {
        continue;
      }
      const member = child?.configuration?.member ?? child?.id;
      if (!member || this._columns.some(column => column.member === member)) {
        continue;
      }
      this._columns.push(Object.freeze({
        member,
        label: child?.configuration?.label ?? child?.getAttribute?.('label') ?? '',
        testId: child?.configuration?.testId ?? child?.getAttribute?.('data-testid') ?? null
      }));
    }
  }

  #publishCollectionState() {
    this.dispatchEvent(createSemanticEvent(
      CausewaySemanticEvent.COLLECTION_STATE,
      {element: this, member: this.id, state: this.collectionState}
    ));
  }
}

function sameColumnConfiguration(left, right) {
  return left.member === right.member
    && left.label === right.label
    && left.testId === right.testId;
}

function collectionRows(state) {
  if (Array.isArray(state?.rows)) {
    return state.rows;
  }
  if (Array.isArray(state?.data?.window?.rows)) {
    return state.data.window.rows;
  }
  return Array.isArray(state?.data?.get) ? state.data.get : [];
}

function collectionShell(presentation, content, attributes = '') {
  const {
    labelId,
    label,
    descriptionId,
    description,
    descriptionVisible,
    reasonId,
    disabledReason,
    describedBy,
    tooltip
  } = presentation;
  const tooltipAttributes = tooltip
    ? ` tabindex="0" data-tooltip="${escapeHtml(tooltip)}"${describedBy ? ` aria-describedby="${describedBy}"` : ''}`
    : '';
  return `<section class="causeway-collection" aria-labelledby="${labelId}"${describedBy ? ` aria-describedby="${describedBy}"` : ''}${attributes}>
  <h2 id="${labelId}" class="causeway-collection-label${tooltip ? ' causeway-member-tooltip' : ''}"${tooltipAttributes}>${escapeHtml(label)}</h2>
  ${description ? `<p id="${descriptionId}" class="causeway-collection-description${descriptionVisible ? '' : ' causeway-visually-hidden'}">${escapeHtml(description)}</p>` : ''}
  ${disabledReason ? `<span id="${reasonId}" class="causeway-visually-hidden">${escapeHtml(disabledReason)}</span>` : ''}
  <div class="causeway-collection-content">${content}</div>
</section>`;
}

function humanize(value) {
  return String(value || '').replace(/([a-z0-9])([A-Z])/g, '$1 $2').replace(/^./, character => character.toUpperCase());
}

function firstNonBlank(...values) {
  return values.find(value => String(value ?? '').trim())?.trim() ?? '';
}

function normalizedText(value) {
  return String(value ?? '').trim().toLocaleLowerCase();
}

function collectionBreakpointPixels() {
  const candidate = globalThis.getComputedStyle?.(globalThis.document?.documentElement)?.fontSize;
  const rootFontSize = Number.parseFloat(candidate);
  return 48 * (Number.isFinite(rootFontSize) && rootFontSize > 0 ? rootFontSize : 16);
}

function gridRowKey(row) {
  const metadata = row?._meta ?? {};
  return `${metadata.logicalTypeName ?? ''}:${metadata.id ?? ''}`;
}

const MAX_DECLARATIVE_PAGE_SIZE = 100;
const MAX_COLLECTION_SEARCH_LENGTH = 256;
const COLLECTION_SEARCH_DEBOUNCE_MS = 250;

function normalizedPageSize(value) {
  const normalized = String(value ?? '').trim();
  if (!/^\d+$/.test(normalized)) return null;
  const parsed = Number(normalized);
  return Number.isSafeInteger(parsed) && parsed >= 1 && parsed <= MAX_DECLARATIVE_PAGE_SIZE
    ? parsed
    : null;
}

function sameWindowContract(authoritative, candidate) {
  if (!authoritative || !candidate) return false;
  return authoritative.ordering === candidate.ordering
    && authoritative.totalCount === candidate.totalCount
    && authoritative.maximumSize === candidate.maximumSize
    && authoritative.searchSupported === candidate.searchSupported
    && sameMembers(authoritative.sortableMembers, candidate.sortableMembers);
}

function sameMembers(left, right) {
  return JSON.stringify(left ?? []) === JSON.stringify(right ?? []);
}
