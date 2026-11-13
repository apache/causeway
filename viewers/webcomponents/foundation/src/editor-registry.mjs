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

import {vaadinFieldEditorRegistration} from './field-widget.mjs';
import {escapeHtml} from './rendering.mjs';
import {renderCausewayReferenceWidget, supportsCausewayReferenceWidget} from './reference-widget.mjs';
import {defaultValueCodecRegistry, parseCausewayValue, semanticTypeName} from './value-codecs.mjs';

export class CausewayEditorRegistry {
  constructor(registrations = []) {
    this.registrations = [];
    registrations.forEach(registration => this.register(registration));
  }

  register(registration) {
    if (!registration?.id || typeof registration.supports !== 'function' || typeof registration.render !== 'function') {
      throw new Error('An editor registration requires id, supports, and render.');
    }
    const normalized = Object.freeze({
      priority: 0,
      parse: ({value}) => value,
      ...registration
    });
    this.registrations.push(normalized);
    this.registrations.sort((left, right) => right.priority - left.priority);
    return () => {
      const index = this.registrations.indexOf(normalized);
      if (index >= 0) {
        this.registrations.splice(index, 1);
      }
    };
  }

  select(context) {
    return this.registrations.find(registration => registration.supports(context)) ?? unsupportedEditor;
  }
}

const referenceWidgetEditor = Object.freeze({
  id: 'vaadin-reference',
  priority: 400,
  supports: supportsCausewayReferenceWidget,
  render: renderCausewayReferenceWidget,
  parse: ({value}) => value
});

const choiceEditor = Object.freeze({
  id: 'choice',
  priority: 300,
  supports: context => (context.choices?.length ?? 0) > 0,
  render: context => {
    const options = context.choices.map(choice => {
      const normalized = normalizeChoice(choice);
      const selected = sameValue(normalized.value, context.value) ? ' selected' : '';
      return `<option value="${escapeHtml(normalized.encoded)}"${selected}>${escapeHtml(normalized.label)}</option>`;
    }).join('');
    return `<select ${inputAttributes(context)}>${options}</select>`;
  },
  parse: ({value, choices}) => {
    const match = choices.map(normalizeChoice).find(choice => choice.encoded === String(value));
    return match?.value ?? value;
  }
});

const autoCompleteEditor = Object.freeze({
  id: 'autocomplete',
  priority: 280,
  supports: context => context.autoComplete === true,
  render: context => {
    const listId = `${context.inputId}-suggestions`;
    const options = (context.suggestions ?? []).map(choice => {
      const normalized = normalizeChoice(choice);
      return `<option value="${escapeHtml(normalized.encoded)}">${escapeHtml(normalized.label)}</option>`;
    }).join('');
    const current = context.value && typeof context.value === 'object'
      ? normalizeChoice(context.value).encoded
      : context.value ?? '';
    const continuation = context.hasMoreSuggestions === true
      ? '<span class="causeway-autocomplete-continuation" role="status">More matches are available; refine the search.</span>'
      : '';
    return `<input type="text" ${inputAttributes(context)} value="${escapeHtml(current)}" list="${escapeHtml(listId)}"><datalist id="${escapeHtml(listId)}">${options}</datalist>${continuation}`;
  },
  parse: ({value, suggestions = []}) => {
    const match = suggestions.map(normalizeChoice).find(choice => choice.encoded === String(value));
    return match?.value ?? value;
  }
});

const booleanEditor = Object.freeze({
  id: 'boolean',
  priority: 200,
  supports: context => semanticTypeName(context) === 'Boolean',
  render: context => context.required
    ? `<input type="checkbox" ${inputAttributes(context)}${context.controlValue === 'true' ? ' checked' : ''}>`
    : `<select ${inputAttributes(context)}><option value=""${context.controlValue === '' ? ' selected' : ''}>No value</option><option value="true"${context.controlValue === 'true' ? ' selected' : ''}>True</option><option value="false"${context.controlValue === 'false' ? ' selected' : ''}>False</option></select>`,
  parse: context => context.required ? Boolean(context.checked) : context.value
});

const temporalEditor = Object.freeze({
  id: 'temporal',
  priority: 195,
  supports: context => [
    'LocalDate', 'LocalDateTime', 'LocalTime',
    'OffsetDateTime', 'OffsetTime', 'DateTime', 'LegacyDateTime', 'ZonedDateTime'
  ].includes(semanticTypeName(context)),
  render: context => {
    const typeName = semanticTypeName(context);
    const inputType = typeName === 'LocalDate'
      ? 'date'
      : typeName === 'LocalTime' ? 'time' : typeName === 'LocalDateTime' ? 'datetime-local' : 'text';
    const step = ['LocalTime', 'LocalDateTime'].includes(typeName) ? ' step="any"' : '';
    const min = context.min == null ? '' : ` min="${escapeHtml(context.min)}"`;
    const max = context.max == null ? '' : ` max="${escapeHtml(context.max)}"`;
    return `<input type="${inputType}" ${inputAttributes(context)} value="${escapeHtml(context.controlValue)}"${step}${min}${max}>`;
  },
  parse: ({value}) => value
});

const exactNumberEditor = Object.freeze({
  id: 'exact-number',
  priority: 192,
  supports: context => ['BigDecimal', 'BigInteger', 'Long'].includes(semanticTypeName(context)),
  render: context => `<input type="text" inputmode="${semanticTypeName(context) === 'BigDecimal' ? 'decimal' : 'numeric'}" ${inputAttributes(context)} value="${escapeHtml(context.controlValue)}">`,
  parse: ({value}) => value
});

const numberEditor = Object.freeze({
  id: 'number',
  priority: 190,
  supports: context => ['Int', 'Short', 'Byte', 'Float', 'Double'].includes(semanticTypeName(context)),
  render: context => `<input type="number" ${inputAttributes(context)} value="${escapeHtml(context.controlValue)}">`,
  parse: ({value}) => value
});

const multilineEditor = Object.freeze({
  id: 'multiline',
  priority: 110,
  supports: context => semanticTypeName(context) === 'String' && Number.isSafeInteger(context.multiLine) && context.multiLine > 1,
  render: context => `<textarea ${inputAttributes(context)} rows="${Math.min(context.multiLine, 50)}">${escapeHtml(context.value ?? '')}</textarea>`,
  parse: ({value}) => value
});

const textEditor = Object.freeze({
  id: 'text',
  priority: 100,
  supports: context => ['String', 'ID', 'UUID', 'Locale', 'Char', 'URL', 'Url', 'Password', 'ProtectedValue'].includes(semanticTypeName(context)),
  render: context => {
    const typeName = semanticTypeName(context);
    const inputType = ['Password', 'ProtectedValue'].includes(typeName)
      ? 'password'
      : ['URL', 'Url'].includes(typeName) ? 'url' : 'text';
    const autocomplete = inputType === 'password' ? ' autocomplete="new-password"' : '';
    return `<input type="${inputType}"${autocomplete} ${inputAttributes(context)} value="${escapeHtml(context.controlValue)}">`;
  },
  parse: ({value}) => value
});

const enumEditor = Object.freeze({
  id: 'enum',
  priority: 180,
  supports: context => innermostType(context.inputType)?.kind === 'ENUM',
  render: context => {
    const options = (context.enumValues ?? []).map(value => `<option value="${escapeHtml(value)}"${value === context.value ? ' selected' : ''}>${escapeHtml(value)}</option>`).join('');
    return `<select ${inputAttributes(context)}>${options}</select>`;
  },
  parse: ({value}) => value
});

const unsupportedEditor = Object.freeze({
  id: 'unsupported',
  priority: -1000,
  supports: () => true,
  render: context => `<span class="causeway-unsupported" role="alert">Unsupported editor for ${escapeHtml(semanticTypeName(context) ?? 'unknown input type')}</span>`,
  parse: ({value}) => value
});

export const defaultEditorRegistry = new CausewayEditorRegistry([
  referenceWidgetEditor,
  vaadinFieldEditorRegistration,
  choiceEditor,
  autoCompleteEditor,
  booleanEditor,
  temporalEditor,
  exactNumberEditor,
  numberEditor,
  enumEditor,
  multilineEditor,
  textEditor
]);

export function renderCausewayEditor(context, registry = defaultEditorRegistry, codecRegistry = defaultValueCodecRegistry) {
  const codec = codecRegistry.select(context);
  const codecContext = {...context, codec};
  const selectedEditor = codec.id === 'unsupported' ? unsupportedEditor : registry.select(codecContext);
  const editor = Object.freeze({
    ...selectedEditor,
    codec,
    debounced: typeof selectedEditor.debounced === 'function'
      ? selectedEditor.debounced(codecContext)
      : selectedEditor.debounced === true,
    render: currentContext => {
      const effectiveContext = {...context, ...currentContext, codec};
      return selectedEditor.render({
        ...effectiveContext,
        controlValue: codec.toControlValue(effectiveContext.value, effectiveContext)
      });
    },
    parse: currentContext => {
      const effectiveContext = {...context, ...currentContext, codec};
      const rawValue = selectedEditor.parse(effectiveContext);
      return parseCausewayValue(codec, {...effectiveContext, value: rawValue});
    }
  });
  return Object.freeze({
    editor,
    editorId: editor.id,
    codec,
    codecId: codec.id,
    html: editor.render(context)
  });
}

export function parseCausewayEditorValue(editor, context) {
  return editor.parse(context);
}

function inputAttributes(context) {
  const attributes = [
    `id="${escapeHtml(context.inputId)}"`,
    `name="${escapeHtml(context.name)}"`,
    `aria-labelledby="${escapeHtml(context.labelId)}"`,
    `data-causeway-editor="${escapeHtml(context.name)}"`
  ];
  if (context.testId) {
    attributes.push(`data-testid="${escapeHtml(context.testId)}"`);
  }
  if (context.descriptionId || context.errorId) {
    attributes.push(`aria-describedby="${escapeHtml([context.descriptionId, context.errorId].filter(Boolean).join(' '))}"`);
  }
  if (context.errorId) {
    attributes.push('aria-invalid="true"');
  }
  if (context.disabled) {
    attributes.push('disabled aria-disabled="true"');
  }
  return attributes.join(' ');
}

function normalizeChoice(choice) {
  if (choice && typeof choice === 'object') {
    const metadata = choice._meta ?? choice;
    const value = metadata.id
      ? {id: metadata.id, ...(metadata.logicalTypeName ? {logicalTypeName: metadata.logicalTypeName} : {})}
      : choice;
    return {
      value,
      encoded: metadata.id ?? JSON.stringify(choice),
      label: metadata.title ?? metadata.id ?? JSON.stringify(choice)
    };
  }
  return {value: choice, encoded: String(choice ?? ''), label: String(choice ?? '')};
}

function sameValue(left, right) {
  if (left === right) {
    return true;
  }
  if (left && right && typeof left === 'object' && typeof right === 'object') {
    return (left.id ?? left._meta?.id) === (right.id ?? right._meta?.id);
  }
  return false;
}

function innermostType(typeRef) {
  let current = typeRef;
  while (current?.ofType) {
    current = current.ofType;
  }
  return current;
}
