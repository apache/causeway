/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */

import './generated/vaadin-selective.js';

if (matchMedia('(prefers-color-scheme: dark)').matches) document.documentElement.setAttribute('theme', 'dark');

const wait = milliseconds => new Promise(resolve => setTimeout(resolve, milliseconds));
const referenceItems = Array.from({length: 240}, (_, index) => ({
  id: `petclinic.owner.Owner:${index + 1}`,
  logicalTypeName: 'petclinic.owner.Owner',
  objectId: String(index + 1),
  label: `Owner ${String(index + 1).padStart(3, '0')} — ${index % 7 === 0 ? 'A deliberately long family name for overflow evidence' : ['Anderson', 'Baker', 'Chen', 'Diaz'][index % 4]}`
}));
const collectionRows = Array.from({length: 1200}, (_, index) => ({
  id: `petclinic.visit.Visit:${index + 1}`,
  logicalTypeName: 'petclinic.visit.Visit',
  objectId: String(index + 1),
  name: `Visit ${String(index + 1).padStart(4, '0')}`,
  owner: referenceItems[index % referenceItems.length].label,
  status: ['Scheduled', 'Complete', 'Needs follow-up'][index % 3],
  date: `2026-${String((index % 12) + 1).padStart(2, '0')}-${String((index % 27) + 1).padStart(2, '0')}`
}));

class GraphQLAnalysisAdapter {
  constructor() {
    this.generation = 1;
    this.events = [];
    this.sequence = 0;
  }

  record(kind, detail = {}) {
    const event = {sequence: ++this.sequence, generation: this.generation, kind, ...detail};
    this.events.push(event);
    window.dispatchEvent(new CustomEvent('causeway-analysis-event', {detail: event}));
    return event;
  }

  nextGeneration() {
    this.generation += 1;
    this.record('route-generation', {value: this.generation});
    return this.generation;
  }

  async autocomplete(search, {signal, delay = search.includes('slow') ? 240 : 55} = {}) {
    const generation = this.generation;
    const request = this.record('graphql-autocomplete-start', {
      operationName: 'CausewayPropertyAutoComplete',
      variables: {search},
      publicContract: true,
      serverPaging: false
    });
    await abortableDelay(delay, signal);
    if (search === '__error__') {
      this.record('graphql-autocomplete-terminal-error', {request: request.sequence, search});
      throw new Error('Representative GraphQL autocomplete failure');
    }
    if (generation !== this.generation) {
      this.record('graphql-autocomplete-stale', {request: request.sequence, search});
      throw new DOMException('Superseded route generation', 'AbortError');
    }
    const normalized = search.trim().toLowerCase();
    const items = search === '__partial__'
      ? referenceItems.slice(0, 2)
      : referenceItems.filter(item => item.label.toLowerCase().includes(normalized));
    this.record(search === '__partial__' ? 'graphql-autocomplete-partial-error' : 'graphql-autocomplete-complete', {request: request.sequence, search, count: items.length});
    return items;
  }

  async autocompletePage({filter = '', page = 0, pageSize = 25, signal} = {}) {
    const all = await this.autocomplete(filter, {signal});
    const start = page * pageSize;
    this.record('adapter-local-choice-page', {filter, page, pageSize, totalCount: all.length, gap: 'GraphQL autocomplete has no paging arguments'});
    return {items: all.slice(start, start + pageSize), totalCount: all.length};
  }

  async collectionWindow({offset = 0, size = 50, sortOrders = [], filters = [], signal, failure = null} = {}) {
    const generation = this.generation;
    const request = this.record('graphql-collection-window-start', {
      operationName: 'CausewayCollectionWindow', variables: {offset, size}, publicContract: true,
      unsupportedSortOrders: sortOrders, unsupportedFilters: filters
    });
    await abortableDelay(45, signal);
    if (failure === 'terminal') {
      this.record('graphql-collection-window-terminal-error', {request: request.sequence});
      throw new Error('Representative GraphQL collection failure');
    }
    if (generation !== this.generation) throw new DOMException('Superseded route generation', 'AbortError');
    const rows = collectionRows.slice(offset, offset + size);
    this.record(failure === 'partial' ? 'graphql-collection-window-partial-error' : 'graphql-collection-window-complete', {request: request.sequence, offset, returnedCount: rows.length, totalCount: collectionRows.length});
    return {rows, offset, requestedSize: size, returnedCount: rows.length, totalCount: collectionRows.length, hasPrevious: offset > 0, hasNext: offset + rows.length < collectionRows.length, ordering: 'CONFIGURED', partialError: failure === 'partial' ? 'Representative partial row error' : null};
  }

  validate(field, value) {
    const message = value == null || String(value).trim() === '' ? `${field} is required` : null;
    this.record('graphql-validate', {operationName: 'CausewayValidateAction', field, valid: !message});
    return message;
  }

  invoke(values, outcome = 'object') {
    this.record('graphql-invoke', {operationName: 'CausewayInvokeAction', values, outcome});
    return outcome === 'scalar' ? {kind: 'scalar', value: `Saved ${values.name}`}
      : outcome === 'collection' ? {kind: 'collection', value: collectionRows.slice(0, 3)}
        : outcome === 'void' ? {kind: 'void', value: null}
          : outcome === 'error' ? {kind: 'error', errors: ['Representative invocation failure']}
            : {kind: 'object', value: referenceItems[0]};
  }
}

function abortableDelay(milliseconds, signal) {
  return new Promise((resolve, reject) => {
    const timeout = setTimeout(resolve, milliseconds);
    signal?.addEventListener('abort', () => {
      clearTimeout(timeout);
      reject(new DOMException('Aborted', 'AbortError'));
    }, {once: true});
  });
}

const adapter = new GraphQLAnalysisAdapter();

class CausewayObjectContextStub extends HTMLElement {
  connectedCallback() {
    this.dataset.contextState = 'ready';
    this.dataset.logicalType = this.getAttribute('logical-type') ?? '';
    this.dataset.objectId = this.getAttribute('object-id') ?? '';
  }
}
customElements.define('causeway-object-context-stub', CausewayObjectContextStub);

class CausewayVaadinReference extends HTMLElement {
  connectedCallback() {
    if (this.dataset.ready) return;
    this.dataset.ready = 'true';
    this.dataset.analysisAdapter = 'reference';
    const combo = document.createElement('vaadin-combo-box');
    combo.label = this.getAttribute('label') ?? 'Owner reference';
    combo.itemLabelPath = 'label';
    combo.itemValuePath = 'id';
    combo.pageSize = 25;
    combo.clearButtonVisible = true;
    combo.required = this.hasAttribute('required');
    combo.disabled = this.hasAttribute('disabled');
    combo.helperText = this.getAttribute('disabled-reason') ?? 'Search uses the public GraphQL autocomplete operation';
    const status = document.createElement('p');
    status.className = 'status-line';
    status.setAttribute('role', 'status');
    combo.dataProvider = async (params, callback) => {
      const controller = new AbortController();
      this._requestController?.abort();
      this._requestController = controller;
      this.dataset.state = 'loading';
      status.textContent = 'Loading choices…';
      try {
        const result = await adapter.autocompletePage({...params, signal: controller.signal});
        this.dataset.state = params.filter === '__partial__' ? 'partial-error' : result.items.length ? 'ready' : 'empty';
        status.textContent = params.filter === '__partial__' ? 'Choices loaded with a representative partial error.' : result.items.length ? `${result.totalCount} matching choices.` : 'No matching choices.';
        callback(result.items, result.totalCount);
      } catch (error) {
        if (error.name !== 'AbortError') {
          this.dataset.state = 'terminal-error';
          this.dataset.error = error.message;
          status.textContent = error.message;
          callback([], 0);
        }
      }
    };
    combo.addEventListener('selected-item-changed', event => {
      const item = event.detail.value ?? null;
      this.dispatchEvent(new CustomEvent('causeway-reference-change', {bubbles: true, detail: {value: item, identity: item?.id ?? null}}));
    });
    this.append(combo, status);
    this.combo = combo;
  }

  disconnectedCallback() { this._requestController?.abort(); }
}
customElements.define('causeway-vaadin-reference', CausewayVaadinReference);

class CausewayVaadinMultiReference extends HTMLElement {
  connectedCallback() {
    if (this.dataset.ready) return;
    this.dataset.ready = 'true';
    this.dataset.analysisAdapter = 'multi-reference';
    const combo = document.createElement('vaadin-multi-select-combo-box');
    combo.label = this.getAttribute('label') ?? 'Participants';
    combo.itemLabelPath = 'label';
    combo.itemValuePath = 'id';
    combo.pageSize = 25;
    combo.dataProvider = async (params, callback) => {
      const controller = new AbortController();
      this._requestController?.abort();
      this._requestController = controller;
      try {
        const result = await adapter.autocompletePage({...params, signal: controller.signal});
        callback(result.items, result.totalCount);
      } catch (error) {
        if (error.name !== 'AbortError') callback([], 0);
      }
    };
    combo.addEventListener('selected-items-changed', event => {
      const items = event.detail.value ?? [];
      this.dispatchEvent(new CustomEvent('causeway-multi-reference-change', {bubbles: true, detail: {values: items, identities: items.map(item => item.id)}}));
    });
    this.append(combo);
    this.combo = combo;
  }

  disconnectedCallback() { this._requestController?.abort(); }
}
customElements.define('causeway-vaadin-multi-reference', CausewayVaadinMultiReference);

class CausewayVaadinGrid extends HTMLElement {
  connectedCallback() {
    if (this.dataset.ready) return;
    this.dataset.ready = 'true';
    this.dataset.analysisAdapter = 'grid';
    const grid = document.createElement('vaadin-grid');
    grid.setAttribute('aria-label', this.getAttribute('label') ?? 'Visits');
    grid.pageSize = 50;
    grid.itemIdPath = 'id';
    for (const [path, header] of [['name', 'Visit'], ['owner', 'Owner'], ['status', 'Status'], ['date', 'Date']]) {
      const column = document.createElement('vaadin-grid-column');
      column.path = path;
      column.header = header;
      column.autoWidth = path !== 'owner';
      grid.append(column);
    }
    grid.dataProvider = async (params, callback) => {
      const controller = new AbortController();
      this._requestControllers ??= new Set();
      this._requestControllers.add(controller);
      try {
        const result = await adapter.collectionWindow({
          offset: params.page * params.pageSize, size: params.pageSize,
          sortOrders: params.sortOrders, filters: params.filters, signal: controller.signal
        });
        callback(result.rows, result.totalCount);
      } catch (error) {
        if (error.name !== 'AbortError') callback([], 0);
      } finally {
        this._requestControllers.delete(controller);
      }
    };
    grid.addEventListener('active-item-changed', event => {
      const item = event.detail.value;
      if (!item) return;
      this.dispatchEvent(new CustomEvent('causeway-navigate', {bubbles: true, detail: {target: {logicalTypeName: item.logicalTypeName, id: item.objectId}}}));
    });
    this.append(grid);
    this.grid = grid;
  }

  disconnectedCallback() { for (const controller of this._requestControllers ?? []) controller.abort(); }
}
customElements.define('causeway-vaadin-grid', CausewayVaadinGrid);

function contextSummary(mode) {
  return `<section class="context-summary">
    <div><p>Route object context</p><h2>Owner: Samantha Peterson</h2></div>
    <p><strong>${mode}</strong><br>petclinic.owner.Owner · 1</p>
  </section>`;
}

function typedFields() {
  return `<section class="panel"><h2>Typed fields and validation</h2><div class="field-grid">
    <vaadin-text-field label="Required display name" required value="Samantha Peterson" helper-text="Validated by Causeway"></vaadin-text-field>
    <vaadin-text-field label="Disabled reason" value="System assigned" disabled></vaadin-text-field>
    <vaadin-text-area label="Notes with a deliberately long label" value="Follow-up requested after the next appointment"></vaadin-text-area>
    <vaadin-select label="Status"></vaadin-select>
    <vaadin-date-picker label="Visit date" required value="2026-08-21"></vaadin-date-picker>
    <vaadin-time-picker label="Visit time" value="10:30"></vaadin-time-picker>
    <vaadin-date-time-picker label="Review date and time" value="2026-08-22T13:45"></vaadin-date-time-picker>
    <vaadin-checkbox checked label="Send a reminder"></vaadin-checkbox>
  </div><p class="status-line" data-field-status>All values are valid.</p></section>`;
}

function actionPanel() {
  return `<section class="panel"><h2>Action interaction</h2>
    <p>Causeway owns validation, invocation, cancellation, and outcomes; Vaadin supplies browser controls.</p>
    <div class="action-row"><vaadin-button theme="primary" data-open-action>Schedule follow-up</vaadin-button><vaadin-button data-cancel-action disabled>Cancel invocation</vaadin-button></div>
    <div class="result-card" data-action-result role="status">No action result.</div>
    <vaadin-dialog data-action-dialog aria-label="Schedule follow-up action"></vaadin-dialog>
  </section>`;
}

function eventPanel() {
  return `<section class="panel full"><h2>Semantic and GraphQL evidence</h2><pre class="event-log" data-event-log tabindex="0" aria-label="Most recent analysis events"></pre></section>`;
}

function genericPage() {
  return `<causeway-object-context-stub class="route-context" logical-type="petclinic.owner.Owner" object-id="1" data-page-mode="generic">
    ${contextSummary('Generic semantic page')}
    <div class="workspace">
      <section class="panel"><h2>Reference choices</h2><causeway-vaadin-reference label="Primary owner" required></causeway-vaadin-reference><causeway-vaadin-multi-reference label="Consulting owners"></causeway-vaadin-multi-reference><causeway-vaadin-reference label="Disabled system owner" disabled disabled-reason="Disabled because ownership is system assigned"></causeway-vaadin-reference><p class="status-line">GraphQL autocomplete is search-only; Vaadin pages the returned set locally.</p></section>
      ${typedFields()}
      <section class="panel full"><h2>Lazy collection</h2><causeway-vaadin-grid label="Owner visits"></causeway-vaadin-grid><p class="status-line">GraphQL window supports offset, size and total count; interactive sort and filter are intentionally unavailable.</p></section>
      ${actionPanel()}${eventPanel()}
    </div>
  </causeway-object-context-stub>`;
}

function semanticCustomPage() {
  return `<causeway-object-context-stub class="route-context" logical-type="petclinic.owner.Owner" object-id="1" data-page-mode="semantic">
    ${contextSummary('Router-selected custom HTML page')}
    <p class="mode-note">This ordinary HTML fragment uses stable Causeway wrappers; page code does not implement Vaadin data-provider or event protocols.</p>
    <div class="workspace"><section class="panel"><h2>Custom reference card</h2><causeway-vaadin-reference label="Related owner"></causeway-vaadin-reference><causeway-vaadin-multi-reference label="Care team"></causeway-vaadin-multi-reference></section>${typedFields()}<section class="panel full"><h2>Custom visit table</h2><causeway-vaadin-grid label="Custom owner visits"></causeway-vaadin-grid></section>${actionPanel()}${eventPanel()}</div>
  </causeway-object-context-stub>`;
}

function rawCustomPage() {
  return `<causeway-object-context-stub class="route-context" logical-type="petclinic.owner.Owner" object-id="1" data-page-mode="raw">
    ${contextSummary('Optional raw-widget custom page')}
    <p class="mode-note">This lower-level page directly configures allowlisted Vaadin widgets and is version-coupled to the bundled component API.</p>
    <div class="workspace"><section class="panel"><h2>Raw reference controls</h2><vaadin-combo-box data-raw-combo label="Raw related owner" item-label-path="label" item-value-path="id"></vaadin-combo-box><vaadin-multi-select-combo-box data-raw-multi label="Raw care team" item-label-path="label" item-value-path="id"></vaadin-multi-select-combo-box></section>${typedFields()}<section class="panel full"><h2>Raw Grid</h2><vaadin-grid data-raw-grid aria-label="Raw custom visits"><vaadin-grid-column path="name" header="Visit"></vaadin-grid-column><vaadin-grid-column path="owner" header="Owner"></vaadin-grid-column><vaadin-grid-column path="status" header="Status"></vaadin-grid-column></vaadin-grid><p class="status-line">Application code owns these raw component APIs and their upgrade compatibility.</p></section>${eventPanel()}</div>
  </causeway-object-context-stub>`;
}

function configureTypedFields(root) {
  const select = root.querySelector('vaadin-select');
  if (select) select.items = [{label: 'Scheduled', value: 'scheduled'}, {label: 'Complete', value: 'complete'}, {label: 'Needs follow-up', value: 'follow-up'}];
  const required = root.querySelector('vaadin-text-field[required]');
  required?.addEventListener('value-changed', event => {
    const message = adapter.validate('Display name', event.detail.value);
    required.invalid = Boolean(message);
    required.errorMessage = message ?? '';
    const status = root.querySelector('[data-field-status]');
    if (status) {
      status.textContent = message ?? 'All values are valid.';
      status.dataset.kind = message ? 'error' : 'valid';
    }
  });
}

function configureAction(root) {
  const dialog = root.querySelector('[data-action-dialog]');
  const open = root.querySelector('[data-open-action]');
  const result = root.querySelector('[data-action-result]');
  if (!dialog || !open || !result) return;
  dialog.renderer = dialogRoot => {
    if (dialogRoot.childElementCount) return;
    const wrapper = document.createElement('div');
    wrapper.className = 'panel';
    wrapper.innerHTML = '<h3>Schedule follow-up</h3><vaadin-text-field label="Reason" required data-action-reason></vaadin-text-field><vaadin-date-picker label="Date" required value="2026-08-29" data-action-date></vaadin-date-picker><div class="action-row"><vaadin-button theme="primary" data-invoke>Invoke</vaadin-button><vaadin-button data-close>Cancel</vaadin-button></div><p class="status-line" data-dialog-status></p>';
    wrapper.querySelector('[data-close]').addEventListener('click', () => { dialog.opened = false; adapter.record('action-cancel'); });
    wrapper.querySelector('[data-invoke]').addEventListener('click', async () => {
      const reason = wrapper.querySelector('[data-action-reason]');
      const message = adapter.validate('Reason', reason.value);
      reason.invalid = Boolean(message);
      reason.errorMessage = message ?? '';
      if (message) { wrapper.querySelector('[data-dialog-status]').textContent = message; return; }
      wrapper.querySelector('[data-dialog-status]').textContent = 'Invoking…';
      await wait(60);
      const outcome = adapter.invoke({name: reason.value, date: wrapper.querySelector('[data-action-date]').value}, 'object');
      result.textContent = `Object result: ${outcome.value.label}`;
      dialog.opened = false;
      open.focus();
    });
    dialogRoot.append(wrapper);
  };
  open.addEventListener('click', () => { dialog.opened = true; adapter.record('action-open'); });
}

function configureRaw(root) {
  const combo = root.querySelector('[data-raw-combo]');
  const multi = root.querySelector('[data-raw-multi]');
  for (const control of [combo, multi]) {
    if (!control) continue;
    control.pageSize = 25;
    control.dataProvider = async (params, callback) => {
      try {
        const result = await adapter.autocompletePage(params);
        callback(result.items, result.totalCount);
      } catch (error) {
        if (error.name !== 'AbortError') callback([], 0);
      }
    };
  }
  const grid = root.querySelector('[data-raw-grid]');
  if (grid) {
    grid.pageSize = 50;
    grid.itemIdPath = 'id';
    grid.dataProvider = async (params, callback) => {
      try {
        const result = await adapter.collectionWindow({offset: params.page * params.pageSize, size: params.pageSize, sortOrders: params.sortOrders, filters: params.filters});
        callback(result.rows, result.totalCount);
      } catch (error) {
        if (error.name !== 'AbortError') callback([], 0);
      }
    };
  }
}

function configureEventLog(root) {
  const output = root.querySelector('[data-event-log]');
  const update = () => { if (output) output.textContent = adapter.events.slice(-14).map(event => JSON.stringify(event)).join('\n'); };
  window.addEventListener('causeway-analysis-event', update, {signal: routeAbortController.signal});
  update();
}

let routeAbortController = new AbortController();
let currentMode = 'generic';
const renderRoute = (mode, {focus = true} = {}) => {
  routeAbortController.abort();
  routeAbortController = new AbortController();
  adapter.nextGeneration();
  currentMode = mode;
  const route = document.querySelector('#analysis-route');
  route.innerHTML = mode === 'semantic' ? semanticCustomPage() : mode === 'raw' ? rawCustomPage() : genericPage();
  configureTypedFields(route);
  configureAction(route);
  configureRaw(route);
  configureEventLog(route);
  document.querySelectorAll('[data-mode]').forEach(button => button.setAttribute('aria-pressed', String(button.dataset.mode === mode)));
  adapter.record('route-ready', {mode, contextCount: route.querySelectorAll('causeway-object-context-stub').length});
  if (focus) route.focus();
};

document.querySelectorAll('[data-mode]').forEach(button => button.addEventListener('click', () => renderRoute(button.dataset.mode)));
window.addEventListener('popstate', event => renderRoute(event.state?.mode ?? 'generic'));
window.addEventListener('causeway-navigate', event => adapter.record('canonical-navigation', event.detail));
window.addEventListener('causeway-reference-change', event => adapter.record('semantic-reference-change', event.detail));
window.addEventListener('causeway-multi-reference-change', event => adapter.record('semantic-multi-reference-change', event.detail));

async function runRepresentativeStateTest() {
  const partialChoices = await adapter.autocomplete('__partial__');
  let choiceError;
  try { await adapter.autocomplete('__error__'); } catch (error) { choiceError = error.message; }
  const partialWindow = await adapter.collectionWindow({offset: 0, size: 5, failure: 'partial'});
  let collectionError;
  try { await adapter.collectionWindow({offset: 0, size: 5, failure: 'terminal'}); } catch (error) { collectionError = error.message; }
  const outcomes = Object.fromEntries(['scalar', 'object', 'collection', 'void', 'error'].map(kind => [kind, adapter.invoke({name: 'Evidence'}, kind).kind]));
  return {partialChoices: partialChoices.length, choiceError, partialWindow: partialWindow.partialError, collectionError, outcomes};
}

async function runStaleSearchTest() {
  const slow = new AbortController();
  const old = adapter.autocomplete('slow Owner 001', {signal: slow.signal}).catch(error => error.name);
  await wait(20);
  slow.abort();
  const current = await adapter.autocomplete('Owner 019');
  return {old: await old, current: current.map(item => item.id), staleApplied: current.some(item => item.label.includes('slow'))};
}

async function runLifecycle(iterations = 20) {
  const overlaySelector = 'vaadin-combo-box-overlay, vaadin-multi-select-combo-box-overlay, vaadin-dialog-overlay, [popover]';
  for (const mode of ['generic', 'semantic', 'raw', 'generic']) renderRoute(mode, {focus: false});
  await wait(80);
  const before = {bodyElements: document.querySelectorAll('*').length, overlays: document.querySelectorAll(overlaySelector).length};
  for (let index = 0; index < iterations; index += 1) {
    renderRoute(index % 3 === 0 ? 'raw' : index % 2 === 0 ? 'semantic' : 'generic', {focus: false});
    const controller = new AbortController();
    const request = adapter.autocomplete('slow', {signal: controller.signal}).catch(error => error.name);
    controller.abort();
    await request;
  }
  renderRoute('generic', {focus: false});
  await wait(80);
  const after = {bodyElements: document.querySelectorAll('*').length, overlays: document.querySelectorAll(overlaySelector).length};
  return {iterations, before, after, elementGrowth: after.bodyElements - before.bodyElements, overlayGrowth: after.overlays - before.overlays, contextCount: document.querySelectorAll('#analysis-route > causeway-object-context-stub').length};
}

window.analysis = {
  adapter,
  referenceItems,
  collectionRows,
  renderRoute,
  runStaleSearchTest,
  runRepresentativeStateTest,
  runLifecycle,
  get mode() { return currentMode; },
  standalone: true,
  flowRuntime: Boolean(window.Vaadin?.Flow?.clients)
};

await Promise.all(['vaadin-combo-box', 'vaadin-multi-select-combo-box', 'vaadin-grid', 'vaadin-date-picker', 'vaadin-dialog'].map(name => customElements.whenDefined(name)));
renderRoute('generic', {focus: false});
document.documentElement.dataset.analysisReady = 'true';
performance.mark('causeway-analysis-ready');
