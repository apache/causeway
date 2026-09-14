/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */

const ROLES = Object.freeze(['primary', 'secondary', 'tertiary']);
const MAX_MENUS = 128;
const MAX_SECTIONS = 128;
const MAX_ACTIONS = 1024;
const MAX_TEXT = 512;

export function projectCausewayMenuBar(bar, {
  generation = 0,
  excludeAction = null,
  menuLabel = null,
  actionLabel = null,
  actionAppearance = null
} = {}) {
  const role = ROLES.includes(bar?.role) ? bar.role : null;
  if (!role) return rejected('role-unsupported', generation);
  if (!Number.isSafeInteger(generation) || generation < 0) return rejected('generation-invalid', 0, role);
  if (!Array.isArray(bar?.menus)) return rejected('menus-unsupported', generation, role);
  if (bar.menus.length === 0) return rejected('empty', generation, role);
  if (bar.menus.length > MAX_MENUS) return rejected('hierarchy-too-large', generation, role);
  const actions = Object.create(null);
  const seenActions = new Set();
  let actionCount = 0;
  try {
    const menus = bar.menus.map((menu, menuIndex) => {
      if (!Array.isArray(menu?.sections) || menu.sections.length > MAX_SECTIONS) throw projectionError('sections-unsupported');
      const sections = menu.sections.map((section, sectionIndex) => {
        if (!Array.isArray(section?.actions)) throw projectionError('actions-unsupported');
        const projectedActions = section.actions
          .filter(action => !isExcludedAction(excludeAction, action))
          .map((action, actionIndex) => {
            actionCount += 1;
            if (actionCount > MAX_ACTIONS) throw projectionError('hierarchy-too-large');
            const serviceLogicalTypeName = boundedIdentity(action?.serviceLogicalTypeName);
            const actionId = boundedIdentity(action?.actionId);
            if (!serviceLogicalTypeName || !actionId) throw projectionError('action-identity-invalid');
            const semanticIdentity = `${serviceLogicalTypeName}#${actionId}`;
            if (seenActions.has(semanticIdentity)) throw projectionError('action-identity-duplicate');
            seenActions.add(semanticIdentity);
            const key = `${generation}:${role}:${menuIndex}:${sectionIndex}:${actionIndex}`;
            const presentationDetail = Object.freeze({
              serviceLogicalTypeName,
              actionId,
              label: boundedText(action?.label || actionId)
            });
            const label = projectedActionLabel(actionLabel, presentationDetail);
            const appearance = projectedActionAppearance(actionAppearance, presentationDetail);
            const descriptor = Object.freeze({
              kind: 'action',
              key,
              role,
              menuIndex,
              sectionIndex,
              actionIndex,
              serviceLogicalTypeName,
              actionId,
              label,
              ...(appearance ? {appearance} : {}),
              description: boundedText(action?.description),
              iconHint: boundedText(action?.iconHint, 128),
              iconPosition: action?.iconPosition === 'RIGHT' ? 'RIGHT' : 'LEFT',
              areYouSure: action?.areYouSure === true,
              promptStyle: action?.promptStyle ?? null,
              ...(action?.resultElementLogicalTypeName
                ? {resultElementLogicalTypeName: action.resultElementLogicalTypeName}
                : {}),
              disabled: Boolean(action?.disabled),
              disabledReason: boundedText(action?.disabled),
              generation
            });
            actions[key] = descriptor;
            return descriptor;
          });
        return Object.freeze({
          kind: 'section',
          role,
          menuIndex,
          sectionIndex,
          label: boundedText(section?.label),
          actions: Object.freeze(projectedActions),
          generation
        });
      }).filter(section => section.actions.length > 0);
      const authoredLabel = boundedText(menu?.label || `Menu ${menuIndex + 1}`);
      return Object.freeze({
        kind: 'menu',
        role,
        menuIndex,
        label: projectedMenuLabel(menuLabel, Object.freeze({role, menuIndex, label: authoredLabel})),
        description: boundedText(menu?.description),
        iconHint: boundedText(menu?.iconHint, 128),
        sections: Object.freeze(sections),
        generation
      });
    }).filter(menu => menu.sections.length > 0);
    if (menus.length === 0) return rejected('empty', generation, role);
    return Object.freeze({
      accepted: true,
      reason: null,
      role,
      generation,
      menus: Object.freeze(menus),
      actions: Object.freeze(actions),
      actionCount
    });
  } catch (error) {
    return rejected(error?.code ?? 'hierarchy-unsupported', generation, role);
  }
}

export function resolveCausewayMenuAction(projection, key) {
  if (!projection?.accepted || typeof key !== 'string') return null;
  const descriptor = projection.actions?.[key];
  if (!descriptor || descriptor.generation !== projection.generation || descriptor.disabled) return null;
  return descriptor;
}

export function createVaadinMenuItems(projection) {
  if (!projection?.accepted) return Object.freeze([]);
  return Object.freeze(projection.menus.map(menu => Object.freeze({
    text: menu.label,
    title: menu.description,
    causewayKind: 'menu',
    causewayRole: menu.role,
    children: Object.freeze(menu.sections.flatMap(section => {
      const leaves = section.actions.map(action => Object.freeze({
        text: action.label,
        title: boundedTooltip(action.description, action.disabledReason),
        description: action.description,
        disabled: action.disabled,
        causewayKind: 'action',
        causewayKey: action.key,
        causewayActionAppearance: action.appearance,
        causewayIconHint: action.iconHint,
        causewayIconPosition: action.iconPosition,
        causewayDisabledReason: action.disabledReason
      }));
      if (!section.label) return leaves;
      return [Object.freeze({
        text: section.label,
        disabled: true,
        causewayKind: 'section',
        causewaySectionLabel: true
      }), ...leaves];
    }))
  })));
}

function isExcludedAction(predicate, action) {
  if (typeof predicate !== 'function') return false;
  try {
    return predicate(Object.freeze({
      serviceLogicalTypeName: String(action?.serviceLogicalTypeName ?? ''),
      actionId: String(action?.actionId ?? '')
    })) === true;
  } catch (_error) {
    return true;
  }
}

function projectedMenuLabel(mapper, detail) {
  if (typeof mapper !== 'function') return detail.label;
  try {
    const mapped = mapper(detail);
    return typeof mapped === 'string' ? boundedText(mapped) || detail.label : detail.label;
  } catch (_error) {
    return detail.label;
  }
}

function projectedActionLabel(mapper, detail) {
  if (typeof mapper !== 'function') return detail.label;
  try {
    const mapped = mapper(detail);
    return typeof mapped === 'string' ? boundedText(mapped) || detail.label : detail.label;
  } catch (_error) {
    return detail.label;
  }
}

function projectedActionAppearance(mapper, detail) {
  if (typeof mapper !== 'function') return '';
  try {
    const mapped = mapper(detail);
    return typeof mapped === 'string' && /^[-_A-Za-z0-9]{1,64}$/.test(mapped) ? mapped : '';
  } catch (_error) {
    return '';
  }
}

function rejected(reason, generation, role = null) {
  return Object.freeze({accepted: false, reason, role, generation, menus: Object.freeze([]), actions: Object.freeze({}), actionCount: 0});
}

function projectionError(code) {
  const error = new Error(code);
  error.code = code;
  return error;
}

function boundedTooltip(description, disabledReason) {
  return [boundedText(description), boundedText(disabledReason)].filter(Boolean).join('\n\n');
}

function boundedIdentity(value) {
  const text = String(value ?? '');
  return /^[-._A-Za-z0-9]{1,256}$/.test(text) ? text : '';
}

function boundedText(value, maximum = MAX_TEXT) {
  return String(value ?? '').replace(/[\u0000-\u001f\u007f]/g, '').slice(0, maximum);
}
