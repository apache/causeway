/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */

import {escapeHtml} from './rendering.mjs';

const DEFAULT_MODULE_URL = new URL('./vaadin-reference/vaadin-reference.js', import.meta.url).href;
const DEFAULT_CONFIGURATION = Object.freeze({
  enabled: false,
  maximumResults: 50,
  minimumSearchLength: 2,
  moduleUrl: DEFAULT_MODULE_URL
});
let configuration = DEFAULT_CONFIGURATION;
let candidateModulePromise;

if (globalThis.document?.documentElement?.dataset?.causewayReferenceWidgets === 'vaadin') {
  configureCausewayReferenceWidgets({
    enabled: true,
    minimumSearchLength: Number(globalThis.document.documentElement.dataset.causewayReferenceMinimumSearchLength),
    maximumResults: Number(globalThis.document.documentElement.dataset.causewayReferenceMaximumResults)
  });
}

export function configureCausewayReferenceWidgets(options = {}) {
  configuration = Object.freeze({
    enabled: options.enabled === true,
    maximumResults: positiveInteger(options.maximumResults, DEFAULT_CONFIGURATION.maximumResults),
    minimumSearchLength: nonNegativeInteger(options.minimumSearchLength, DEFAULT_CONFIGURATION.minimumSearchLength),
    moduleUrl: options.moduleUrl ? new URL(options.moduleUrl, globalThis.document?.baseURI ?? import.meta.url).href : DEFAULT_MODULE_URL
  });
  candidateModulePromise = undefined;
  return configuration;
}

export function causewayReferenceWidgetConfiguration() {
  return configuration;
}

export function supportsCausewayReferenceWidget(context) {
  if (!configuration.enabled) {
    return false;
  }
  const candidateChoices = [...(context.choices ?? []), ...(context.suggestions ?? [])];
  const referenceInput = isReferenceInput(context.inputType) || candidateChoices.some(isReferenceValue);
  const bounded = context.autoComplete === true || candidateChoices.length <= configuration.maximumResults;
  return referenceInput && bounded && (context.autoComplete === true || candidateChoices.length > 0);
}

export function renderCausewayReferenceWidget(context) {
  const choices = deduplicateReferences([...(context.choices ?? []), ...(context.suggestions ?? [])]);
  const multiple = isListType(context.inputType);
  const attributes = [
    `id="${escapeHtml(context.inputId)}"`,
    `data-causeway-editor="${escapeHtml(context.name)}"`,
    `data-items="${escapeHtml(JSON.stringify(choices))}"`,
    `data-value="${escapeHtml(JSON.stringify(normalizePendingValue(context.value, multiple)))}"`,
    `data-label="${escapeHtml(context.label ?? context.name)}"`,
    `data-minimum-search-length="${configuration.minimumSearchLength}"`,
    `data-maximum-results="${configuration.maximumResults}"`,
    `data-labelledby="${escapeHtml(context.labelId)}"`
  ];
  if (context.testId) attributes.push(`data-testid="${escapeHtml(context.testId)}"`);
  if (context.descriptionId || context.errorId) attributes.push(`data-describedby="${escapeHtml([context.descriptionId, context.errorId].filter(Boolean).join(' '))}"`);
  if (context.errorId) attributes.push('data-invalid="true"');
  if (context.disabled) attributes.push('disabled');
  if (context.required) attributes.push('required');
  if (context.autoComplete === true) attributes.push('data-autocomplete="true"');
  if (multiple) attributes.push('multiple');
  return `<causeway-reference-editor ${attributes.join(' ')}><span role="status">Loading reference editor…</span></causeway-reference-editor>`;
}

export class CausewayReferenceEditorElement extends HTMLElement {
  constructor() {
    super();
    this._control = null;
    this._generation = 0;
    this._searchTimer = null;
    this._syncing = false;
    this._value = null;
    this.addEventListener('keydown', event => {
      if (event.key === 'Escape' && this._control?.opened !== true) {
        this.dispatchEvent(new CustomEvent('causeway-reference-escape', {bubbles: true, composed: true}));
      }
    }, {capture: true});
  }

  connectedCallback() {
    const generation = ++this._generation;
    this._value = this.#initialValue();
    this.dataset.widgetState = 'loading';
    void this.#upgrade(generation);
  }

  disconnectedCallback() {
    this._generation += 1;
    clearTimeout(this._searchTimer);
    this._control = null;
  }

  get value() {
    return this._value;
  }

  set value(value) {
    this._value = value;
    this.#synchronizeSelection();
  }

  focus(options) {
    this._control?.focus?.(options);
  }

  async #upgrade(generation) {
    try {
      candidateModulePromise ??= import(configuration.moduleUrl);
      await candidateModulePromise;
      if (!this.isConnected || generation !== this._generation) return;
      const tagName = this.hasAttribute('multiple') ? 'vaadin-multi-select-combo-box' : 'vaadin-combo-box';
      await globalThis.customElements.whenDefined(tagName);
      if (!this.isConnected || generation !== this._generation) return;
      const control = document.createElement(tagName);
      this._control = control;
      this._syncing = true;
      control.id = `${this.id}-control`;
      control.label = this.dataset.label || this.getAttribute('data-causeway-editor') || 'Reference';
      control.itemLabelPath = 'title';
      control.itemValuePath = 'id';
      control.items = this.#items();
      if (this.hasAttribute('multiple')) control.autoExpandVertically = true;
      control.clearButtonVisible = !this.hasAttribute('required');
      control.required = this.hasAttribute('required');
      control.disabled = this.hasAttribute('disabled');
      const describedBy = this.dataset.describedby;
      if (describedBy) control.setAttribute('aria-describedby', describedBy);
      if (this.dataset.invalid === 'true') control.setAttribute('invalid', '');
      control.addEventListener('filter-changed', event => this.#filterChanged(event));
      const selectionEvent = this.hasAttribute('multiple') ? 'selected-items-changed' : 'selected-item-changed';
      control.addEventListener(selectionEvent, event => this.#selectionChanged(event));
      this.replaceChildren(control);
      this._value = this.#initialValue();
      this.#synchronizeSelection();
      queueMicrotask(() => { this._syncing = false; });
      this.dataset.widgetState = 'ready';
    } catch (error) {
      if (!this.isConnected || generation !== this._generation) return;
      configuration = Object.freeze({...configuration, enabled: false});
      candidateModulePromise = undefined;
      this.dataset.widgetState = 'fallback';
      this.dataset.widgetError = error?.message ?? 'Candidate module failed to load.';
      this.dispatchEvent(new CustomEvent('causeway-reference-load-failed', {
        bubbles: true,
        composed: true,
        detail: Object.freeze({message: this.dataset.widgetError})
      }));
      if (this.isConnected) this.#renderFallback();
    }
  }

  #items() {
    try {
      const items = JSON.parse(this.dataset.items || '[]');
      return Array.isArray(items) ? items.slice(0, configuration.maximumResults) : [];
    } catch {
      return [];
    }
  }

  #initialValue() {
    try {
      return JSON.parse(this.dataset.value || 'null');
    } catch {
      return this.hasAttribute('multiple') ? [] : null;
    }
  }

  #synchronizeSelection() {
    if (!this._control) return;
    const items = this.#items();
    this._syncing = true;
    if (this.hasAttribute('multiple')) {
      const values = Array.isArray(this._value) ? this._value : [];
      this._control.selectedItems = values.map(value => items.find(item => sameReference(item, value)) ?? value);
    } else {
      this._control.selectedItem = this._value == null ? null : items.find(item => sameReference(item, this._value)) ?? this._value;
    }
    queueMicrotask(() => { this._syncing = false; });
  }

  #filterChanged(event) {
    if (this._syncing || this.dataset.autocomplete !== 'true') return;
    clearTimeout(this._searchTimer);
    const search = String(event.detail?.value ?? '').trim();
    if (search.length < configuration.minimumSearchLength) return;
    this._searchTimer = setTimeout(() => {
      if (!this.isConnected) return;
      this.dispatchEvent(new CustomEvent('causeway-reference-search', {
        bubbles: true,
        composed: true,
        detail: Object.freeze({name: this.getAttribute('data-causeway-editor'), search})
      }));
    }, 250);
  }

  #selectionChanged(event) {
    if (this._syncing) return;
    const selected = event.detail?.value;
    this._value = this.hasAttribute('multiple') ? Object.freeze([...(selected ?? [])]) : selected ?? null;
    this.dispatchEvent(new Event('change', {bubbles: true, composed: true}));
  }

  #renderFallback() {
    const items = this.#items();
    const multiple = this.hasAttribute('multiple');
    const select = document.createElement('select');
    select.id = `${this.id}-fallback`;
    select.multiple = multiple;
    select.disabled = this.hasAttribute('disabled');
    select.setAttribute('aria-label', this.dataset.label || 'Reference');
    for (const item of items) {
      const option = document.createElement('option');
      option.value = item.id;
      option.textContent = item.title;
      option.selected = multiple
        ? (Array.isArray(this._value) && this._value.some(value => sameReference(value, item)))
        : sameReference(this._value, item);
      select.append(option);
    }
    select.addEventListener('change', () => {
      this._value = multiple
        ? [...select.selectedOptions].map(option => items.find(item => item.id === option.value))
        : items.find(item => item.id === select.value) ?? null;
      this.dispatchEvent(new Event('change', {bubbles: true, composed: true}));
    });
    this._control = select;
    this.replaceChildren(select);
  }
}

function isReferenceInput(typeRef) {
  const type = innermostType(typeRef);
  return type?.kind === 'OBJECT' || type?.kind === 'INPUT_OBJECT' || /__gqlv_input$/.test(type?.name ?? '');
}

function isListType(typeRef) {
  let current = typeRef;
  while (current) {
    if (current.kind === 'LIST') return true;
    current = current.ofType;
  }
  return false;
}

function innermostType(typeRef) {
  let current = typeRef;
  while (current?.ofType) current = current.ofType;
  return current;
}

function isReferenceValue(value) {
  const metadata = value?._meta ?? value;
  return Boolean(value && typeof value === 'object' && metadata?.id);
}

function deduplicateReferences(values) {
  const references = [];
  const identities = new Set();
  for (const value of values) {
    const reference = normalizeReference(value);
    if (!reference || identities.has(referenceKey(reference))) continue;
    identities.add(referenceKey(reference));
    references.push(reference);
  }
  return references;
}

function normalizePendingValue(value, multiple) {
  if (multiple) return (Array.isArray(value) ? value : []).map(normalizeReference).filter(Boolean);
  return normalizeReference(value);
}

function normalizeReference(value) {
  if (!isReferenceValue(value)) return null;
  const metadata = value._meta ?? value;
  return {
    id: String(metadata.id),
    ...(metadata.logicalTypeName ? {logicalTypeName: metadata.logicalTypeName} : {}),
    title: String(metadata.title ?? metadata.id)
  };
}

function sameReference(left, right) {
  const leftMetadata = left?._meta ?? left;
  const rightMetadata = right?._meta ?? right;
  if (leftMetadata?.id == null || rightMetadata?.id == null || String(leftMetadata.id) !== String(rightMetadata.id)) return false;
  return !leftMetadata.logicalTypeName || !rightMetadata.logicalTypeName || leftMetadata.logicalTypeName === rightMetadata.logicalTypeName;
}

function referenceKey(value) {
  const metadata = value?._meta ?? value;
  return `${metadata?.logicalTypeName ?? ''}:${metadata?.id ?? ''}`;
}

function positiveInteger(value, fallback) {
  return Number.isSafeInteger(Number(value)) && Number(value) > 0 ? Number(value) : fallback;
}

function nonNegativeInteger(value, fallback) {
  return Number.isSafeInteger(Number(value)) && Number(value) >= 0 ? Number(value) : fallback;
}
