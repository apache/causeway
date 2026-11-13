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

import {
  MAX_STRUCTURAL_DIAGNOSTICS,
  parseStructuralXml,
  structuralTextContent
} from './structural-xml.mjs';

export const CAUSEWAY_MENU_BARS_NAMESPACE = 'https://causeway.apache.org/applib/layout/menubars/bootstrap3';
export const CAUSEWAY_MENU_COMPONENT_NAMESPACE = 'https://causeway.apache.org/applib/layout/component';
export const MENU_BAR_ROLES = Object.freeze(['primary', 'secondary', 'tertiary']);
export const MAX_MENU_DIAGNOSTICS = MAX_STRUCTURAL_DIAGNOSTICS;

const MENU_ATTRIBUTES = new Set(['cssClassFa', 'unreferencedActions']);
const ACTION_ATTRIBUTES = new Set(['objectType', 'id', 'bookmarking', 'cssClass', 'cssClassFa', 'cssClassFaPosition', 'namedEscaped']);
const DOCUMENT_ATTRIBUTES = new Set(['schemaLocation']);

export class CausewayMenuError extends Error {
  constructor(code, message) {
    super(message);
    this.name = 'CausewayMenuError';
    this.code = code;
  }
}

export function parseCausewayMenuBarsXml(xml, {maxDiagnostics = MAX_MENU_DIAGNOSTICS} = {}) {
  const diagnostics = createDiagnosticCollector(maxDiagnostics);
  const root = parseStructuralXml(xml, {
    codePrefix: 'MENU',
    resourceLabel: 'effective menu resource',
    ErrorType: CausewayMenuError
  });
  if (!isMenuNode(root, 'menuBars')) {
    throw new CausewayMenuError('MENU_ROOT_REQUIRED', 'The effective menu resource does not contain a supported menuBars root.');
  }
  diagnoseAttributes(root, DOCUMENT_ATTRIBUTES, diagnostics, 'menuBars');
  const bars = {};
  for (const role of MENU_BAR_ROLES) {
    const barNode = root.children.find(child => isMenuNode(child, role)) ?? null;
    bars[role] = barNode ? parseBar(barNode, role, diagnostics) : barPlan(role, []);
  }
  for (const child of root.children) {
    if (!MENU_BAR_ROLES.some(role => isMenuNode(child, role)) && !isMenuNode(child, 'metadataError')) {
      diagnostics.add('UNSUPPORTED_MENU_NODE', `Unsupported menu node '${child.localName}' was ignored.`, 'menuBars');
    }
  }
  return deepFreeze({
    usable: true,
    plan: {
      source: 'application-menu',
      bars
    },
    diagnostics: diagnostics.values()
  });
}

export function applyServiceActionStates(plan, actionStates, diagnostics = []) {
  const collector = createDiagnosticCollector(MAX_MENU_DIAGNOSTICS);
  for (const diagnostic of [...(plan?.diagnostics ?? []), ...diagnostics]) {
    collector.add(diagnostic.code, diagnostic.message, diagnostic.path);
  }
  const bars = {};
  for (const role of MENU_BAR_ROLES) {
    const sourceBar = plan?.bars?.[role] ?? barPlan(role, []);
    const menus = [];
    for (const menu of sourceBar.menus ?? []) {
      const sections = [];
      for (const section of menu.sections ?? []) {
        const actions = [];
        for (const action of section.actions ?? []) {
          const state = actionStates.get(serviceActionKey(action.serviceLogicalTypeName, action.actionId));
          if (!state || state.error) {
            collector.add('SERVICE_ACTION_UNAVAILABLE', 'A menu service action is unavailable.', action.path);
            continue;
          }
          if (state.hidden === true) {
            continue;
          }
          actions.push({
            ...action,
            disabled: state.disabled || null,
            areYouSure: state.metadata?.areYouSure === true,
            promptStyle: state.metadata?.promptStyle ?? null,
            ...(state.metadata?.resultElementLogicalTypeName
              ? {resultElementLogicalTypeName: state.metadata.resultElementLogicalTypeName}
              : {})
          });
        }
        if (actions.length > 0) {
          sections.push({...section, actions});
        }
      }
      if (sections.length > 0) {
        menus.push({...menu, sections});
      }
    }
    bars[role] = barPlan(role, menus);
  }
  return deepFreeze({source: plan?.source ?? 'application-menu', bars, diagnostics: collector.values()});
}

export function serviceActionKey(serviceLogicalTypeName, actionId) {
  return `${serviceLogicalTypeName}#${actionId}`;
}

export function menuPlanActionReferences(plan) {
  const references = [];
  for (const role of MENU_BAR_ROLES) {
    for (const menu of plan?.bars?.[role]?.menus ?? []) {
      for (const section of menu.sections ?? []) {
        references.push(...(section.actions ?? []));
      }
    }
  }
  return Object.freeze(references);
}

function parseBar(node, role, diagnostics) {
  diagnoseAttributes(node, new Set(), diagnostics, role);
  const menus = [];
  for (const child of node.children) {
    if (!isMenuNode(child, 'menu')) {
      diagnostics.add('UNSUPPORTED_BAR_NODE', `Unsupported ${role} bar node '${child.localName}' was ignored.`, role);
      continue;
    }
    const menu = parseMenu(child, role, menus.length, diagnostics);
    if (menu) {
      menus.push(menu);
    }
  }
  return barPlan(role, menus);
}

function parseMenu(node, role, index, diagnostics) {
  const path = `${role}.menu[${index}]`;
  diagnoseAttributes(node, MENU_ATTRIBUTES, diagnostics, path);
  const namedNode = node.children.find(child => isMenuNode(child, 'named')) ?? null;
  const label = boundedText(namedNode, 512, diagnostics, path, 'MENU_LABEL_TOO_LONG') || `Menu ${index + 1}`;
  const sections = [];
  for (const child of node.children) {
    if (isMenuNode(child, 'named')) {
      continue;
    }
    if (!isMenuNode(child, 'section')) {
      diagnostics.add('UNSUPPORTED_MENU_CONTENT', `Unsupported menu content '${child.localName}' was ignored.`, path);
      continue;
    }
    const section = parseSection(child, role, index, sections.length, diagnostics);
    if (section) {
      sections.push(section);
    }
  }
  return {
    kind: 'menu',
    label,
    iconHint: boundedAttribute(node, 'cssClassFa', 256, diagnostics, path),
    sections,
    path
  };
}

function parseSection(node, role, menuIndex, sectionIndex, diagnostics) {
  const path = `${role}.menu[${menuIndex}].section[${sectionIndex}]`;
  diagnoseAttributes(node, new Set(), diagnostics, path);
  const namedNode = node.children.find(child => isMenuNode(child, 'named')) ?? null;
  const label = boundedText(namedNode, 512, diagnostics, path, 'SECTION_LABEL_TOO_LONG');
  const actions = [];
  for (const child of node.children) {
    if (isMenuNode(child, 'named')) {
      continue;
    }
    if (!isMenuNode(child, 'serviceAction')) {
      diagnostics.add('UNSUPPORTED_SECTION_CONTENT', `Unsupported section content '${child.localName}' was ignored.`, path);
      continue;
    }
    const action = parseAction(child, path, actions.length, diagnostics);
    if (action) {
      actions.push(action);
    }
  }
  return {kind: 'section', label, actions, path};
}

function parseAction(node, sectionPath, index, diagnostics) {
  const path = `${sectionPath}.action[${index}]`;
  diagnoseAttributes(node, ACTION_ATTRIBUTES, diagnostics, path);
  const serviceLogicalTypeName = node.attributes.get('objectType') ?? '';
  const actionId = node.attributes.get('id') ?? '';
  if (!isSafeLogicalType(serviceLogicalTypeName) || !/^[_A-Za-z][_0-9A-Za-z]{0,127}$/.test(actionId)) {
    diagnostics.add('INVALID_SERVICE_ACTION_REFERENCE', 'A menu service action reference is malformed.', path);
    return null;
  }
  const namedNode = node.children.find(child => isComponentNode(child, 'named') || isMenuNode(child, 'named')) ?? null;
  const describedNode = node.children.find(child => isComponentNode(child, 'describedAs') || isMenuNode(child, 'describedAs')) ?? null;
  for (const child of node.children) {
    if (child === namedNode || child === describedNode || isComponentNode(child, 'link')) {
      continue;
    }
    diagnostics.add('UNSUPPORTED_ACTION_CONTENT', `Unsupported service-action content '${child.localName}' was ignored.`, path);
  }
  return {
    kind: 'service-action',
    serviceLogicalTypeName,
    actionId,
    label: boundedText(namedNode, 512, diagnostics, path, 'ACTION_LABEL_TOO_LONG'),
    description: boundedText(describedNode, 2_048, diagnostics, path, 'ACTION_DESCRIPTION_TOO_LONG'),
    cssHint: boundedAttribute(node, 'cssClass', 256, diagnostics, path),
    iconHint: boundedAttribute(node, 'cssClassFa', 256, diagnostics, path),
    iconPosition: boundedIconPosition(node.attributes.get('cssClassFaPosition'), diagnostics, path),
    disabled: null,
    path
  };
}

function diagnoseAttributes(node, supported, diagnostics, path) {
  for (const name of node.attributes.keys()) {
    if (name === 'xmlns' || name.startsWith('xmlns:') || DOCUMENT_ATTRIBUTES.has(localAttributeName(name))) {
      continue;
    }
    if (!supported.has(localAttributeName(name))) {
      diagnostics.add('UNSUPPORTED_MENU_ATTRIBUTE', `Unsupported attribute '${localAttributeName(name)}' was ignored.`, path);
    }
  }
}

function boundedText(node, maximum, diagnostics, path, code) {
  if (!node) {
    return '';
  }
  const value = structuralTextContent(node).replace(/\s+/g, ' ').trim();
  if (value.length <= maximum) {
    return value;
  }
  diagnostics.add(code, `Menu text was truncated to ${maximum} characters.`, path);
  return value.slice(0, maximum);
}

function boundedIconPosition(value, diagnostics, path) {
  const position = String(value ?? 'LEFT').trim().toUpperCase();
  if (position === 'LEFT' || position === 'RIGHT') return position;
  diagnostics.add('UNSUPPORTED_ICON_POSITION', 'Unsupported service-action icon position was ignored.', path);
  return 'LEFT';
}

function boundedAttribute(node, name, maximum, diagnostics, path) {
  const value = (node.attributes.get(name) ?? '').trim();
  if (value.length <= maximum) {
    return value;
  }
  diagnostics.add('MENU_HINT_TOO_LONG', `Menu presentation data was truncated to ${maximum} characters.`, path);
  return value.slice(0, maximum);
}

function isSafeLogicalType(value) {
  return typeof value === 'string'
    && value.length > 0
    && value.length <= 256
    && !/[\s<>&"'\\]/.test(value);
}

function isMenuNode(node, name) {
  return node?.namespaceURI === CAUSEWAY_MENU_BARS_NAMESPACE && node.localName === name;
}

function isComponentNode(node, name) {
  return node?.namespaceURI === CAUSEWAY_MENU_COMPONENT_NAMESPACE && node.localName === name;
}

function localAttributeName(name) {
  const separator = name.indexOf(':');
  return separator < 0 ? name : name.slice(separator + 1);
}

function barPlan(role, menus) {
  return {kind: 'bar', role, menus};
}

function createDiagnosticCollector(maximum) {
  const diagnostics = [];
  let truncated = false;
  return {
    add(code, message, path = null) {
      if (diagnostics.length < maximum) {
        diagnostics.push(Object.freeze({code, message, path}));
      } else {
        truncated = true;
      }
    },
    values() {
      const values = truncated && diagnostics.length > 0
        ? [...diagnostics.slice(0, maximum - 1), Object.freeze({
          code: 'MENU_DIAGNOSTICS_TRUNCATED',
          message: `Additional menu diagnostics were omitted after ${maximum} entries.`,
          path: null
        })]
        : diagnostics;
      return Object.freeze(values);
    }
  };
}

function deepFreeze(value) {
  if (!value || typeof value !== 'object' || Object.isFrozen(value)) {
    return value;
  }
  for (const child of Object.values(value)) {
    deepFreeze(child);
  }
  return Object.freeze(value);
}
