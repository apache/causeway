/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0.
 */

export const CAUSEWAY_COLLECTION_GRID = 'cw-collection-grid';
export const CAUSEWAY_GRID_WIDGET_POLICY_EVENT = 'causeway-grid-widget-policy';
const DEFAULT_MODULE_URL = new URL('./vaadin-grid/vaadin-grid.js', import.meta.url).href;
const DEFAULT_DEFINITION_TIMEOUT_MS = 5000;
const MAX_DEFINITION_TIMEOUT_MS = 60000;
const APPROVED_PROTOCOLS = new Set(['http:', 'https:', 'file:']);
let configuration = Object.freeze({
  enabled: true,
  moduleUrl: DEFAULT_MODULE_URL,
  definitionTimeoutMs: DEFAULT_DEFINITION_TIMEOUT_MS
});
let modulePromise = null;
let failed = false;
let failure = null;
let configurationRevision = 0;

const documentPolicy = globalThis.document?.documentElement?.dataset;
if (documentPolicy && Object.hasOwn(documentPolicy, 'causewayCollectionGrid')) {
  configureCausewayGridWidgets({enabled: documentPolicy.causewayCollectionGrid === 'vaadin'});
}

export function configureCausewayGridWidgets(options = {}) {
  configuration = Object.freeze({
    enabled: options.enabled !== false,
    moduleUrl: options.moduleUrl ? safeModuleUrl(options.moduleUrl) : DEFAULT_MODULE_URL,
    definitionTimeoutMs: boundedTimeout(options.definitionTimeoutMs)
  });
  modulePromise = null;
  failed = false;
  failure = null;
  configurationRevision += 1;
  announcePolicyChange({family: 'grid', reason: 'configuration', revision: configurationRevision});
  return configuration;
}

export function causewayGridWidgetConfiguration() {
  return Object.freeze({...configuration, failed, failure, revision: configurationRevision});
}

export function useCausewayGridWidget() {
  return configuration.enabled && !failed;
}

export function failCausewayGridWidget({phase = 'adapter', classification = 'GRID_ADAPTER_UNAVAILABLE'} = {}) {
  if (failed) return false;
  failed = true;
  modulePromise = null;
  failure = Object.freeze({phase: boundedToken(phase), classification: boundedToken(classification)});
  announcePolicyChange({family: 'grid', reason: 'failure', revision: configurationRevision, ...failure});
  return true;
}

export class CausewayCollectionGridElement extends HTMLElement {
  constructor() {
    super();
    this._control = null;
    this._generation = 0;
    this._presentationRevision = 0;
    this._presentation = null;
    this._focusRequested = false;
    this._semanticFocusIntent = null;
    this._policyListener = () => this.#applyCurrentPolicy();
  }

  connectedCallback() {
    globalThis.document?.addEventListener?.(CAUSEWAY_GRID_WIDGET_POLICY_EVENT, this._policyListener);
    this.#applyCurrentPolicy();
  }

  #applyCurrentPolicy() {
    const generation = ++this._generation;
    const revision = configurationRevision;
    this.#releaseControl();
    this.replaceChildren();
    if (!configuration.enabled || failed) {
      this.dataset.widgetState = failed ? 'fallback' : 'native';
      return;
    }
    this.dataset.widgetState = 'loading';
    void this.#upgrade(generation, revision);
  }

  disconnectedCallback() {
    globalThis.document?.removeEventListener?.(CAUSEWAY_GRID_WIDGET_POLICY_EVENT, this._policyListener);
    this._generation += 1;
    this._presentationRevision += 1;
    this.#releaseControl();
  }

  set presentation(value) {
    this._presentation = freezePresentation(value);
    this._presentationRevision += 1;
    if (this._control && this.isConnected) this.#applyPresentation();
  }

  get presentation() {
    return this._presentation;
  }

  focus(options) {
    this._focusRequested = true;
    this._control?.focus?.(options);
  }

  restoreSemanticFocus(intent) {
    if (!intent?.rowKey || !intent?.member) return false;
    this._semanticFocusIntent = Object.freeze({
      rowKey: String(intent.rowKey),
      member: String(intent.member),
      role: String(intent.role ?? 'cell')
    });
    const index = this._presentation?.rows?.findIndex(row => row?.key === this._semanticFocusIntent.rowKey) ?? -1;
    if (index >= 0) {
      this._control?.scrollToIndex?.(index);
      this._control?.requestContentUpdate?.();
    }
    return true;
  }

  async #upgrade(generation, revision) {
    let phase = 'module';
    try {
      modulePromise ??= import(configuration.moduleUrl);
      await modulePromise;
      if (!this.#isCurrent(generation, revision)) return;
      phase = 'definition';
      await waitForDefinitions(configuration.definitionTimeoutMs);
      if (!this.#isCurrent(generation, revision)) return;
      phase = 'adapter';
      const control = document.createElement('vaadin-grid');
      control.setAttribute('data-causeway-grid-control', '');
      this._control = control;
      this.replaceChildren(control);
      this.#applyPresentation();
      await control.updateComplete;
      if (!this.#isCurrent(generation, revision) || control !== this._control) return;
      this.dataset.widgetState = 'ready';
      this.dispatchEvent(new CustomEvent('causeway-grid-ready', {
        bubbles: true,
        composed: true,
        detail: Object.freeze({family: 'grid', revision: configurationRevision})
      }));
      if (this._focusRequested) queueMicrotask(() => control.focus());
    } catch (_error) {
      if (!this.#isCurrent(generation, revision)) return;
      this.#releaseControl();
      this.dataset.widgetState = 'fallback';
      this.dataset.widgetError = 'The configured collection Grid could not be loaded.';
      const classification = phase === 'module'
        ? 'GRID_MODULE_UNAVAILABLE'
        : phase === 'definition' ? 'GRID_DEFINITION_UNAVAILABLE' : 'GRID_ADAPTER_UNAVAILABLE';
      this.dispatchEvent(new CustomEvent('causeway-grid-load-failed', {
        bubbles: true,
        composed: true,
        detail: Object.freeze({family: 'grid', phase, classification, revision: configurationRevision})
      }));
      failCausewayGridWidget({phase, classification});
    }
  }

  #applyPresentation() {
    const control = this._control;
    const presentation = this._presentation;
    if (!control || !presentation) return;
    const presentationRevision = this._presentationRevision;
    control.replaceChildren();
    control.setAttribute('aria-labelledby', presentation.labelledBy);
    if (presentation.describedBy) control.setAttribute('aria-describedby', presentation.describedBy);
    else control.removeAttribute('aria-describedby');
    if (presentation.testId) control.setAttribute('data-testid', presentation.testId);
    else control.removeAttribute('data-testid');
    control.pageSize = presentation.pageSize;
    control.activeItem = null;
    control.selectedItems = [];
    control.rowDetailsRenderer = null;
    for (const descriptor of presentation.columns) {
      const column = document.createElement('vaadin-grid-column');
      column.header = descriptor.label;
      column.resizable = false;
      column.sortable = false;
      column.frozen = false;
      column.renderer = (root, _column, model) => {
        if (presentationRevision !== this._presentationRevision || control !== this._control) return;
        try {
          root.replaceChildren();
          descriptor.render(root, model.item, model.index);
          this.#restoreRenderedFocus(root, descriptor, model.item);
        } catch (_error) {
          failCausewayGridWidget({phase: 'renderer', classification: 'GRID_RENDERER_UNAVAILABLE'});
        }
      };
      control.appendChild(column);
    }
    if (presentation.mode === 'virtual') {
      control.items = undefined;
      control.size = presentation.totalCount;
      control.dataProvider = ({page, pageSize}, callback) => {
        const offset = page * pageSize;
        Promise.resolve(presentation.rangeProvider({offset, size: pageSize}))
          .then(result => {
            if (presentationRevision !== this._presentationRevision || control !== this._control || !this.isConnected) return;
            callback(Array.isArray(result?.rows) ? result.rows : [], presentation.totalCount);
          })
          .catch(error => {
            if (error?.name === 'AbortError') return;
            if (presentationRevision === this._presentationRevision && control === this._control && this.isConnected) {
              failCausewayGridWidget({phase: 'data-provider', classification: 'GRID_RANGE_UNAVAILABLE'});
            }
          });
      };
      control.clearCache?.();
    } else {
      control.dataProvider = undefined;
      control.size = presentation.rows.length;
      control.items = presentation.rows;
    }
  }

  #restoreRenderedFocus(root, descriptor, item) {
    const intent = this._semanticFocusIntent;
    if (!intent || item?.key !== intent.rowKey || descriptor.member !== intent.member) return;
    this._semanticFocusIntent = null;
    const candidate = root.querySelector?.('cw-object-link, a, button, [tabindex]') ?? root;
    queueMicrotask(() => {
      if (this.isConnected && root.isConnected !== false) candidate.focus?.();
    });
  }

  #releaseControl() {
    if (this._control) {
      this._control.dataProvider = undefined;
      this._control.items = [];
    }
    this._control = null;
  }

  #isCurrent(generation, revision) {
    return this.isConnected && generation === this._generation && revision === configurationRevision;
  }
}

function freezePresentation(value = {}) {
  const mode = value.mode === 'virtual' ? 'virtual' : 'bounded';
  const rows = Object.freeze([...(Array.isArray(value.rows) ? value.rows : [])]);
  const columns = Object.freeze([...(Array.isArray(value.columns) ? value.columns : [])].map(column => Object.freeze({...column})));
  const totalCount = mode === 'virtual' && Number.isSafeInteger(value.totalCount) && value.totalCount >= 0
    ? value.totalCount
    : rows.length;
  const pageSize = Number.isSafeInteger(value.pageSize) && value.pageSize > 0 ? value.pageSize : Math.max(rows.length, 1);
  return Object.freeze({
    mode,
    rows,
    columns,
    totalCount,
    pageSize,
    rangeProvider: typeof value.rangeProvider === 'function' ? value.rangeProvider : async () => ({rows: []}),
    labelledBy: String(value.labelledBy ?? ''),
    describedBy: String(value.describedBy ?? ''),
    testId: String(value.testId ?? '')
  });
}

function announcePolicyChange(detail) {
  globalThis.document?.dispatchEvent?.(new CustomEvent(CAUSEWAY_GRID_WIDGET_POLICY_EVENT, {
    detail: Object.freeze(detail)
  }));
}

function safeModuleUrl(value) {
  const url = new URL(value, globalThis.document?.baseURI ?? import.meta.url);
  if (!APPROVED_PROTOCOLS.has(url.protocol)) throw new Error('Grid module URL must use an approved module protocol.');
  return url.href;
}

function boundedToken(value) {
  return String(value ?? '').replace(/[^A-Za-z0-9_.-]/g, '_').slice(0, 80);
}

function boundedTimeout(value) {
  if (value == null) return DEFAULT_DEFINITION_TIMEOUT_MS;
  if (!Number.isSafeInteger(value) || value < 1 || value > MAX_DEFINITION_TIMEOUT_MS) {
    throw new Error(`Grid definition timeout must be an integer from 1 through ${MAX_DEFINITION_TIMEOUT_MS}.`);
  }
  return value;
}

async function waitForDefinitions(timeoutMs) {
  let timeoutId;
  try {
    await Promise.race([
      Promise.all([
        globalThis.customElements.whenDefined('vaadin-grid'),
        globalThis.customElements.whenDefined('vaadin-grid-column')
      ]),
      new Promise((_, reject) => {
        timeoutId = globalThis.setTimeout(
          () => reject(new Error('Grid custom-element definition timed out.')),
          timeoutMs
        );
      })
    ]);
  } finally {
    globalThis.clearTimeout(timeoutId);
  }
}
