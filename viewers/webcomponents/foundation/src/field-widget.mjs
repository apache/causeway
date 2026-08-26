/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */

import {escapeHtml} from './rendering.mjs';
import {semanticTypeName} from './value-codecs.mjs';

export const CAUSEWAY_FIELD_EDITOR = 'causeway-field-editor';
export const CausewayFieldFamily = Object.freeze({
  BASIC: 'basic',
  NUMERIC: 'numeric',
  LOCAL_TEMPORAL: 'local-temporal'
});

const FAMILY_IDS = Object.freeze(Object.values(CausewayFieldFamily));
const DEFAULT_MODULE_URLS = Object.freeze({
  basic: new URL('./vaadin-fields/vaadin-basic.js', import.meta.url).href,
  numeric: new URL('./vaadin-fields/vaadin-numeric.js', import.meta.url).href,
  'local-temporal': new URL('./vaadin-fields/vaadin-local-temporal.js', import.meta.url).href
});
let configuration = Object.freeze({families: Object.freeze([...FAMILY_IDS]), moduleUrls: DEFAULT_MODULE_URLS});
const familyModules = new Map();
const failedFamilies = new Set();

const documentPolicy = globalThis.document?.documentElement?.dataset;
if (documentPolicy && Object.hasOwn(documentPolicy, 'causewayFieldFamilies')) {
  configureCausewayFieldWidgets({families: documentPolicy.causewayFieldFamilies});
}

export function configureCausewayFieldWidgets(options = {}) {
  const families = normalizeFamilies(options.families ?? FAMILY_IDS);
  const moduleUrls = {...DEFAULT_MODULE_URLS};
  for (const family of FAMILY_IDS) {
    const configured = options.moduleUrls?.[family];
    if (configured) moduleUrls[family] = safeModuleUrl(configured);
  }
  configuration = Object.freeze({
    families: Object.freeze(families),
    moduleUrls: Object.freeze(moduleUrls)
  });
  familyModules.clear();
  failedFamilies.clear();
  return configuration;
}

export function causewayFieldWidgetConfiguration() {
  return configuration;
}

export function failCausewayFieldFamily(family) {
  if (FAMILY_IDS.includes(family)) {
    failedFamilies.add(family);
    familyModules.delete(family);
  }
}

export function causewayFieldDescriptor(context) {
  if (context.autoComplete === true || context.codec?.id === 'unsupported' || context.codec?.id === 'reference') return null;
  const typeName = semanticTypeName(context);
  const choices = [...(context.choices ?? []), ...(context.enumValues ?? [])];
  let descriptor = null;
  if (context.codec?.id === 'exact-numeric') {
    descriptor = {family: CausewayFieldFamily.NUMERIC, control: 'text-field', inputMode: typeName === 'BigDecimal' ? 'decimal' : 'numeric'};
  } else if (context.codec?.id === 'machine-numeric') {
    descriptor = {family: CausewayFieldFamily.NUMERIC, control: ['Int', 'Short', 'Byte'].includes(typeName) ? 'integer-field' : 'number-field'};
  } else if (context.codec?.id === 'temporal'
      && ['LocalDate', 'LocalTime', 'LocalDateTime'].includes(typeName)
      && supportedPickerPrecision(context.value)) {
    descriptor = {family: CausewayFieldFamily.LOCAL_TEMPORAL, control: {
      LocalDate: 'date-picker',
      LocalTime: 'time-picker',
      LocalDateTime: 'date-time-picker'
    }[typeName]};
  } else if (context.codec?.id === 'boolean') {
    descriptor = {family: CausewayFieldFamily.BASIC, control: context.required ? 'checkbox' : 'select', boolean: true};
  } else if (context.codec?.id === 'protected') {
    descriptor = {family: CausewayFieldFamily.BASIC, control: 'password-field', sensitive: true, debounced: true};
  } else if (choices.length > 0 || context.inputType?.kind === 'ENUM' || innermostType(context.inputType)?.kind === 'ENUM') {
    descriptor = {family: CausewayFieldFamily.BASIC, control: 'select'};
  } else if (['scalar', 'url'].includes(context.codec?.id)) {
    descriptor = {
      family: CausewayFieldFamily.BASIC,
      control: typeName === 'String' && context.multiLine > 1 ? 'text-area' : 'text-field',
      inputMode: typeName === 'URL' || typeName === 'Url' ? 'url' : null,
      debounced: true
    };
  }
  if (!descriptor || !configuration.families.includes(descriptor.family) || failedFamilies.has(descriptor.family)) return null;
  return Object.freeze(descriptor);
}

export function supportsCausewayFieldWidget(context) {
  return causewayFieldDescriptor(context) !== null;
}

export function renderCausewayFieldWidget(context) {
  const descriptor = causewayFieldDescriptor(context);
  if (!descriptor) throw new Error('The semantic value is not eligible for a Vaadin field adapter.');
  const choices = descriptor.boolean && !context.required
    ? [{label: 'No value', value: ''}, {label: 'True', value: 'true'}, {label: 'False', value: 'false'}]
    : normalizeChoices(context);
  const value = descriptor.sensitive ? '' : String(context.controlValue ?? '');
  const attributes = [
    `id="${escapeHtml(context.inputId)}"`,
    `data-family="${descriptor.family}"`,
    `data-control="${descriptor.control}"`,
    `data-causeway-editor="${escapeHtml(context.name)}"`,
    `data-label="${escapeHtml(context.label ?? context.name)}"`,
    `data-value="${escapeHtml(value)}"`,
    `data-labelledby="${escapeHtml(context.labelId)}"`,
    `data-items="${escapeHtml(JSON.stringify(choices))}"`
  ];
  if (context.testId) attributes.push(`data-testid="${escapeHtml(context.testId)}"`);
  if (context.descriptionId || context.errorId) attributes.push(`data-describedby="${escapeHtml([context.descriptionId, context.errorId].filter(Boolean).join(' '))}"`);
  if (context.errorId) attributes.push('data-invalid="true"');
  if (context.required) attributes.push('required');
  if (context.disabled) attributes.push('disabled');
  if (descriptor.inputMode) attributes.push(`data-input-mode="${descriptor.inputMode}"`);
  if (descriptor.sensitive) attributes.push('data-sensitive="true"');
  if (context.multiLine > 1) attributes.push(`data-rows="${Math.min(context.multiLine, 50)}"`);
  return `<${CAUSEWAY_FIELD_EDITOR} ${attributes.join(' ')}><span role="status">Loading field editor…</span></${CAUSEWAY_FIELD_EDITOR}>`;
}

export const vaadinFieldEditorRegistration = Object.freeze({
  id: 'vaadin-field',
  priority: 350,
  supports: supportsCausewayFieldWidget,
  render: renderCausewayFieldWidget,
  parse: context => causewayFieldDescriptor(context)?.control === 'checkbox' ? Boolean(context.checked) : context.value,
  debounced: context => causewayFieldDescriptor(context)?.debounced === true
});

export class CausewayFieldEditorElement extends HTMLElement {
  constructor() {
    super();
    this._control = null;
    this._generation = 0;
    this._focusRequested = false;
    this.addEventListener('keydown', event => {
      if (event.key === 'Escape') {
        this.dispatchEvent(new CustomEvent('causeway-field-escape', {bubbles: true, composed: true}));
      }
    }, {capture: true});
  }

  connectedCallback() {
    const generation = ++this._generation;
    this.dataset.widgetState = 'loading';
    void this.#upgrade(generation);
  }

  disconnectedCallback() {
    this._generation += 1;
    this._control = null;
  }

  get value() {
    if (this.dataset.control === 'checkbox') return this.checked;
    return this._control?.value ?? this.dataset.value ?? '';
  }

  set value(value) {
    this.dataset.value = this.dataset.sensitive === 'true' ? '' : String(value ?? '');
    if (this._control && this.dataset.control !== 'checkbox') this._control.value = this.dataset.value;
  }

  get checked() {
    return Boolean(this._control?.checked);
  }

  set checked(value) {
    if (this._control) this._control.checked = Boolean(value);
  }

  get selectionStart() {
    return this._control?.inputElement?.selectionStart ?? null;
  }

  get selectionEnd() {
    return this._control?.inputElement?.selectionEnd ?? null;
  }

  setSelectionRange(start, end) {
    this._control?.inputElement?.setSelectionRange?.(start, end);
  }

  focus(options) {
    this._focusRequested = true;
    this._control?.focus?.(options);
  }

  async #upgrade(generation) {
    const family = this.dataset.family;
    const tagName = `vaadin-${this.dataset.control}`;
    try {
      let modulePromise = familyModules.get(family);
      if (!modulePromise) {
        modulePromise = import(configuration.moduleUrls[family]);
        familyModules.set(family, modulePromise);
      }
      await modulePromise;
      if (!this.isConnected || generation !== this._generation) return;
      await globalThis.customElements.whenDefined(tagName);
      if (!this.isConnected || generation !== this._generation) return;
      const control = document.createElement(tagName);
      const editorName = this.dataset.causewayEditor;
      this._control = control;
      control.id = `${this.id}-control`;
      control.setAttribute('data-causeway-editor', editorName);
      this.removeAttribute('data-causeway-editor');
      if (this.dataset.testid) {
        control.setAttribute('data-testid', this.dataset.testid);
        this.removeAttribute('data-testid');
      }
      if (this.dataset.labelledby) control.setAttribute('aria-labelledby', this.dataset.labelledby);
      if (this.dataset.describedby) control.setAttribute('aria-describedby', this.dataset.describedby);
      control.disabled = this.hasAttribute('disabled');
      control.required = this.hasAttribute('required');
      control.invalid = this.dataset.invalid === 'true';
      if ('clearButtonVisible' in control) control.clearButtonVisible = !control.required && this.dataset.control !== 'password-field';
      control.addEventListener('focusout', event => {
        if (!control.isConnected) {
          return;
        }
        event.stopPropagation();
        this.dispatchEvent(new CustomEvent('causeway-editor-commit', {
          bubbles: true,
          composed: true,
          detail: Object.freeze({
            name: editorName,
            nextEditor: focusAttribute(event.relatedTarget, 'data-causeway-editor'),
            nextAction: focusAttribute(event.relatedTarget, 'data-causeway-action')
          })
        }));
      });
      if (this.dataset.control === 'checkbox') {
        control.label = this.dataset.label;
        control.checked = this.dataset.value === 'true';
      } else {
        if (this.dataset.control === 'select') control.items = this.#items();
        if (['time-picker', 'date-time-picker'].includes(this.dataset.control)) control.step = 0.001;
        control.value = this.dataset.value ?? '';
        if (this.dataset.rows && 'maxRows' in control) {
          control.minRows = Math.min(2, Number(this.dataset.rows));
          control.maxRows = Number(this.dataset.rows);
        }
        if (this.dataset.control === 'password-field') control.setAttribute('autocomplete', 'new-password');
      }
      this.replaceChildren(control);
      await control.updateComplete;
      if (!this.isConnected || generation !== this._generation) return;
      if (this.dataset.inputMode && control.inputElement) {
        control.inputElement.inputMode = this.dataset.inputMode;
      }
      this.dataset.widgetState = 'ready';
      if (this._focusRequested) queueMicrotask(() => control.focus());
    } catch (error) {
      if (!this.isConnected || generation !== this._generation) return;
      failCausewayFieldFamily(family);
      this.dataset.widgetState = 'fallback';
      this.dataset.widgetError = 'The configured field family could not be loaded.';
      this.dispatchEvent(new CustomEvent('causeway-field-load-failed', {
        bubbles: true,
        composed: true,
        detail: Object.freeze({family, message: this.dataset.widgetError})
      }));
    }
  }

  #items() {
    try {
      const items = JSON.parse(this.dataset.items || '[]');
      return Array.isArray(items) ? items : [];
    } catch {
      return [];
    }
  }
}

function focusAttribute(target, name) {
  for (let current = target; current;) {
    const value = current.getAttribute?.(name);
    if (value) {
      return value;
    }
    current = current.parentNode ?? current.host ?? current.getRootNode?.()?.host ?? null;
  }
  return null;
}

function normalizeFamilies(value) {
  const candidates = Array.isArray(value) ? value : String(value ?? '').split(',').filter(Boolean);
  const normalized = candidates.map(candidate => String(candidate).trim());
  if (normalized.some(candidate => !FAMILY_IDS.includes(candidate))) throw new Error('Unknown Causeway field family.');
  if (new Set(normalized).size !== normalized.length) throw new Error('Causeway field families must not be repeated.');
  return FAMILY_IDS.filter(family => normalized.includes(family));
}

function safeModuleUrl(value) {
  const base = globalThis.document?.baseURI ?? import.meta.url;
  const url = new URL(value, base);
  if (globalThis.document && url.origin !== new URL(base).origin) throw new Error('Causeway field family modules must be same-origin.');
  if (!['http:', 'https:', 'file:'].includes(url.protocol)) throw new Error('Causeway field family module URL is not supported.');
  return url.href;
}

function supportedPickerPrecision(value) {
  const fraction = /\.(\d+)/.exec(String(value ?? ''))?.[1] ?? '';
  return fraction.length <= 3;
}

function normalizeChoices(context) {
  const values = context.choices?.length ? context.choices : context.enumValues ?? [];
  return values.map(value => {
    if (value && typeof value === 'object') {
      const raw = value.value ?? value.id ?? value._meta?.id ?? JSON.stringify(value);
      return {value: String(raw), label: String(value.label ?? value.title ?? value._meta?.title ?? raw)};
    }
    return {value: String(value ?? ''), label: String(value ?? '')};
  });
}

function innermostType(typeRef) {
  let current = typeRef;
  while (current?.ofType) current = current.ofType;
  return current;
}
