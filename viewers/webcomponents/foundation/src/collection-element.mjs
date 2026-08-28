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

import {CausewaySemanticEvent} from './component-contracts.mjs';
import {CausewayContextConsumerElement} from './context-consumer-element.mjs';
import {createSemanticEvent} from './context-events.mjs';
import {
  connectMemberComposition,
  disconnectMemberComposition,
  eventOriginatesFromAssociatedAction,
  refreshMemberComposition,
  renderMemberPrimary
} from './member-composition.mjs';
import {errorMessage, escapeHtml} from './rendering.mjs';
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
    return ['id', 'label', 'active'];
  }

  constructor() {
    super();
    const sequence = ++collectionSequence;
    this.labelId = `causeway-collection-label-${sequence}`;
    this.descriptionId = `causeway-collection-description-${sequence}`;
    this._columns = [];
    this._rendererRegistry = defaultValueRendererRegistry;
    this.collectionState = Object.freeze({status: 'idle', data: null, errors: []});
    this.rowContexts = [];
    this.loadRevision = 0;
    this.loadAbortController = null;
    this.loadedGeneration = -1;
    this.connectionStarted = false;
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
      if (!eventOriginatesFromAssociatedAction(this, event.target)
          && event.target?.hasAttribute?.('data-causeway-activate')) {
        this.activate();
      }
    });
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

  get columns() {
    return Object.freeze(this._columns.map(column => Object.freeze({...column})));
  }

  set columns(value) {
    this._columns = [...(value ?? [])]
      .map(column => typeof column === 'string' ? {member: column, label: humanize(column)} : {...column})
      .filter(column => column.member);
    if (this.active && this.componentState?.status === 'ready') {
      void this.load({force: true});
    } else if (this.componentState) {
      this.renderComponentState(this.componentState);
    }
  }

  get rendererRegistry() {
    return this._rendererRegistry;
  }

  set rendererRegistry(value) {
    this._rendererRegistry = value ?? defaultValueRendererRegistry;
    if (this.componentState) {
      this.renderComponentState(this.componentState);
    }
  }

  connectedCallback() {
    connectMemberComposition(this);
    this.columnObserver?.observe(this, {childList: true});
    globalThis.setTimeout(() => {
      if (!this.isConnected) {
        return;
      }
      captureDeclarativeCollectionColumns(this);
      this.#captureColumns();
      this.connectionStarted = true;
      super.connectedCallback();
    }, 0);
  }

  disconnectedCallback() {
    disconnectMemberComposition(this);
    this.columnObserver?.disconnect();
    this.connectionStarted = false;
    this.loadRevision += 1;
    this.loadAbortController?.abort();
    this.loadAbortController = null;
    for (const context of this.rowContexts) {
      context.disconnect?.();
    }
    this.rowContexts = [];
    super.disconnectedCallback();
  }

  attributeChangedCallback(name, oldValue, newValue) {
    if (oldValue === newValue || !this.connectionStarted) {
      return;
    }
    if (name === 'id') {
      refreshMemberComposition(this);
      this.loadAbortController?.abort();
      this.loadAbortController = null;
      this.collectionState = Object.freeze({status: 'idle', data: null, errors: []});
      this.loadedGeneration = -1;
      this.reconnectRequirement();
    } else if (name === 'active' && this.active && this.componentState?.status === 'ready') {
      void this.load();
    } else {
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
    if (!context || this.componentState?.status !== 'ready') {
      return null;
    }
    const revision = ++this.loadRevision;
    this.loadAbortController?.abort();
    const abortController = new AbortController();
    this.loadAbortController = abortController;
    this.collectionState = Object.freeze({status: 'loading', data: null, errors: []});
    this.renderComponentState(this.componentState);
    this.#publishCollectionState();
    try {
      const result = await context.loadCollection({
        member: this.id,
        columns: this._columns,
        offset,
        size,
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
    const label = this.label || humanize(this.id);
    const candidateDescription = state.descriptor?.description || '';
    const description = candidateDescription.trim().toLocaleLowerCase() === label.trim().toLocaleLowerCase()
      ? ''
      : candidateDescription;
    const shell = (content, attributes = '') => collectionShell(
      this.labelId,
      label,
      this.descriptionId,
      description,
      content,
      attributes
    );
    if (['idle', 'schema-loading', 'object-loading'].includes(state.status)) {
      renderMemberPrimary(this, shell('<span role="status">Loading collection metadata…</span>', ' aria-busy="true"'));
      return;
    }
    if (['terminal-error', 'unsupported', 'partial-error'].includes(state.status)) {
      renderMemberPrimary(this, shell(`<span class="causeway-error" role="alert">${escapeHtml(errorMessage(state))}</span>`));
      return;
    }
    if (state.data?.hidden === true) {
      renderMemberPrimary(this, '', {hidden: true});
      return;
    }
    const disabledReason = typeof state.data?.disabled === 'string'
      ? state.data.disabled
      : state.data?.disabled === true ? 'Disabled' : '';
    const disabledMarkup = disabledReason
      ? `<p class="causeway-collection-disabled-reason">${escapeHtml(disabledReason)}</p>`
      : '';
    if (!this.active) {
      renderMemberPrimary(this, shell(`${disabledMarkup}<button type="button" data-causeway-activate>Load ${escapeHtml(label)}</button>`));
      return;
    }
    if (this.collectionState.status === 'idle' || this.collectionState.status === 'loading') {
      renderMemberPrimary(this, shell('<span role="status">Loading collection…</span>', ' aria-busy="true"'));
      return;
    }
    if (this.collectionState.status === 'error') {
      renderMemberPrimary(this, shell(`<span class="causeway-error" role="alert">${escapeHtml(errorMessage(this.collectionState))}</span>`));
      return;
    }
    const rows = collectionRows(this.collectionState);
    if (rows.length === 0) {
      renderMemberPrimary(this, shell('<span class="causeway-empty" role="status">No items</span>'));
      return;
    }
    const content = this._columns.length > 0
      ? this.#renderTable(rows)
      : this.#renderDefaultRows(rows);
    const errors = this.collectionState.errors?.length
      ? `<p class="causeway-error" role="alert">${escapeHtml(errorMessage(this.collectionState))}</p>`
      : '';
    renderMemberPrimary(this, shell(`${disabledMarkup}${content}${errors}`));
  }

  #renderDefaultRows(rows) {
    return `<ul class="causeway-collection-rows">${rows.map(row => {
      const metadata = row?._meta ?? {};
      return `<li><cw-object-link logical-type="${escapeHtml(metadata.logicalTypeName ?? '')}" object-id="${escapeHtml(metadata.id ?? '')}" title="${escapeHtml(metadata.title ?? metadata.id ?? '')}"></cw-object-link></li>`;
    }).join('')}</ul>`;
  }

  #renderTable(rows) {
    const header = `<th scope="col">Item</th>${this._columns.map(column => `<th scope="col"${column.testId ? ` data-testid="${escapeHtml(column.testId)}"` : ''}>${escapeHtml(column.label || humanize(column.member))}</th>`).join('')}`;
    const body = rows.map((row, rowIndex) => {
      const metadata = row?._meta ?? {};
      const item = `<td><cw-object-link logical-type="${escapeHtml(metadata.logicalTypeName ?? '')}" object-id="${escapeHtml(metadata.id ?? '')}" title="${escapeHtml(metadata.title ?? metadata.id ?? '')}"></cw-object-link></td>`;
      const cells = this._columns.map(column => this.#renderCell(row, rowIndex, column)).join('');
      return `<tr data-row-index="${rowIndex}">${item}${cells}</tr>`;
    }).join('');
    return `<table class="causeway-collection-table"><thead><tr>${header}</tr></thead><tbody>${body}</tbody></table>`;
  }

  #renderCell(row, rowIndex, column) {
    const property = row?.[column.member] ?? null;
    const errors = (this.collectionState.errors ?? []).filter(error => {
      const path = error.path ?? [];
      return path.includes(rowIndex) && path.includes(column.member);
    });
    if (errors.length > 0) {
      return `<td class="causeway-error" role="alert">${escapeHtml(errors[0].message)}</td>`;
    }
    if (property?.hidden === true) {
      return '<td hidden></td>';
    }
    const disabledReason = typeof property?.disabled === 'string' ? property.disabled : '';
    const descriptor = this.collectionState.rowDescription?.members?.get(column.member) ?? null;
    const rendered = renderCausewayValue({value: property?.get, descriptor}, this._rendererRegistry);
    return `<td${disabledReason ? ` aria-disabled="true" title="${escapeHtml(disabledReason)}"` : ''}>${rendered.html}</td>`;
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

function collectionShell(labelId, label, descriptionId, description, content, attributes = '') {
  return `<section class="causeway-collection" aria-labelledby="${labelId}"${description ? ` aria-describedby="${descriptionId}"` : ''}${attributes}>
  <h2 id="${labelId}" class="causeway-collection-label">${escapeHtml(label)}</h2>
  ${description ? `<p id="${descriptionId}" class="causeway-collection-description">${escapeHtml(description)}</p>` : ''}
  <div class="causeway-collection-content">${content}</div>
</section>`;
}

function humanize(value) {
  return String(value || '').replace(/([a-z0-9])([A-Z])/g, '$1 $2').replace(/^./, character => character.toUpperCase());
}
