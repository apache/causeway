/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0.
 */

import assert from 'node:assert/strict';
import test from 'node:test';

import {
  createVaadinMenuItems,
  projectCausewayMenuBar,
  resolveCausewayMenuAction
} from '../src/menubar-projection.mjs';
import {qualifyCausewayMenuBar} from '../src/menubar-qualification.mjs';

function bar() {
  return {
    role: 'primary',
    menus: [{
      label: 'Administration',
      description: 'Manage application data',
      iconHint: 'admin',
      sections: [{
        label: 'People',
        actions: [
          {serviceLogicalTypeName: 'demo.People', actionId: 'find', label: 'Find people', iconHint: 'search', areYouSure: true, promptStyle: 'DIALOG_SIDEBAR', resultElementLogicalTypeName: 'demo.Person'},
          {serviceLogicalTypeName: 'demo.People', actionId: 'deleteAll', label: 'Delete all', disabled: 'Not permitted'}
        ]
      }, {
        label: '',
        actions: [{serviceLogicalTypeName: 'demo.Reports', actionId: 'daily', label: 'Daily report'}]
      }]
    }]
  };
}

test('projects immutable ordered menus sections actions and opaque identity', () => {
  const projection = projectCausewayMenuBar(bar(), {generation: 7});
  assert.equal(projection.accepted, true);
  assert.equal(projection.role, 'primary');
  assert.equal(projection.actionCount, 3);
  assert.equal(Object.isFrozen(projection), true);
  assert.equal(Object.isFrozen(projection.menus[0].sections[0].actions[0]), true);
  const first = projection.menus[0].sections[0].actions[0];
  assert.equal(first.key, '7:primary:0:0:0');
  assert.equal(first.serviceLogicalTypeName, 'demo.People');
  assert.equal(first.areYouSure, true);
  assert.equal(first.promptStyle, 'DIALOG_SIDEBAR');
  assert.equal(first.resultElementLogicalTypeName, 'demo.Person');
  assert.equal(resolveCausewayMenuAction(projection, first.key), first);
  const disabled = projection.menus[0].sections[0].actions[1];
  assert.equal(disabled.disabled, true);
  assert.equal(disabled.disabledReason, 'Not permitted');
  assert.equal(resolveCausewayMenuAction(projection, disabled.key), null);
});

test('creates one-level Vaadin menus with non-action section headings and activatable leaves', () => {
  const projection = projectCausewayMenuBar(bar(), {generation: 3});
  const items = createVaadinMenuItems(projection);
  assert.equal(items.length, 1);
  assert.equal(items[0].text, 'Administration');
  assert.equal(items[0].children[0].text, 'People');
  assert.equal(items[0].children[0].causewayKind, 'section');
  assert.equal(items[0].children[0].disabled, true);
  assert.equal(items[0].children[0].children, undefined);
  assert.equal(items[0].children[1].causewayKey, '3:primary:0:0:0');
  assert.equal(items[0].children[2].disabled, true);
  assert.equal(items[0].children[3].text, 'Daily report');
  assert.equal(items[0].children.every(item => item.children === undefined), true);
  assert.equal(JSON.stringify(items).includes('function'), false);
  assert.equal(JSON.stringify(items).includes('Not permitted'), true);
});

test('rejects empty malformed duplicate oversized and invalid projections', () => {
  assert.equal(projectCausewayMenuBar({role: 'other', menus: []}).reason, 'role-unsupported');
  assert.equal(projectCausewayMenuBar({role: 'primary', menus: []}).reason, 'empty');
  assert.equal(projectCausewayMenuBar({role: 'primary', menus: [{sections: null}]}).reason, 'sections-unsupported');
  const duplicate = bar();
  duplicate.menus[0].sections[1].actions[0] = {...duplicate.menus[0].sections[0].actions[0]};
  assert.equal(projectCausewayMenuBar(duplicate).reason, 'action-identity-duplicate');
  const invalid = bar();
  invalid.menus[0].sections[0].actions[0].actionId = '<script>';
  assert.equal(projectCausewayMenuBar(invalid).reason, 'action-identity-invalid');
});

test('host exclusion removes authentication actions before item projection', () => {
  const source = bar();
  source.menus[0].sections[1].actions.push({serviceLogicalTypeName: 'causeway.security.LogoutMenu', actionId: 'logout', label: 'Log out'});
  const projection = projectCausewayMenuBar(source, {
    generation: 4,
    excludeAction: action => action.serviceLogicalTypeName === 'causeway.security.LogoutMenu'
  });
  assert.equal(projection.accepted, true);
  assert.equal(Object.values(projection.actions).some(action => action.actionId === 'logout'), false);
  assert.equal(JSON.stringify(createVaadinMenuItems(projection)).includes('Log out'), false);
});

test('host label mapping changes presentation without changing semantic action structure', () => {
  const source = bar();
  source.menus[0].sections[1].actions.push({
    serviceLogicalTypeName: 'causeway.security.LogoutMenu',
    actionId: 'logout',
    label: 'Logout'
  });
  const projection = projectCausewayMenuBar(source, {
    generation: 5,
    menuLabel: menu => menu.role === 'primary' && menu.label === 'Administration' ? 'sven' : undefined,
    actionLabel: action => action.serviceLogicalTypeName === 'causeway.security.LogoutMenu'
      && action.actionId === 'logout' ? 'Sign out' : undefined,
    actionAppearance: action => action.serviceLogicalTypeName === 'causeway.security.LogoutMenu'
      && action.actionId === 'logout' ? 'sign-out' : undefined
  });
  const logout = Object.values(projection.actions).find(action => action.actionId === 'logout');
  assert.equal(projection.menus[0].label, 'sven');
  assert.equal(logout.label, 'Sign out');
  assert.equal(logout.appearance, 'sign-out');
  assert.equal(logout.serviceLogicalTypeName, 'causeway.security.LogoutMenu');
  assert.equal(logout.role, 'primary');
  assert.equal(projection.menus[0].sections[1].actions.at(-1), logout);
  const vaadinItems = createVaadinMenuItems(projection);
  assert.equal(JSON.stringify(vaadinItems).includes('Sign out'), true);
  assert.equal(vaadinItems[0].children.at(-1).causewayActionAppearance, 'sign-out');
  assert.equal(projection.menus[0].sections[0].actions[0].label, 'Find people');
  assert.equal(projection.menus[0].sections[0].actions[0].appearance, undefined);
});

test('defective host presentation mapping preserves bounded authoritative presentation', () => {
  const thrown = projectCausewayMenuBar(bar(), {
    menuLabel: () => { throw new Error('host failure'); },
    actionLabel: () => { throw new Error('host failure'); },
    actionAppearance: () => { throw new Error('host failure'); }
  });
  const unsupported = projectCausewayMenuBar(bar(), {
    menuLabel: () => ({label: '<script>'}),
    actionLabel: () => ({label: '<script>'}),
    actionAppearance: () => 'not valid!'
  });
  const bounded = projectCausewayMenuBar(bar(), {
    menuLabel: () => `sven\u0000${'!'.repeat(600)}`,
    actionLabel: () => `Sign\u0000 out${'!'.repeat(600)}`
  });
  assert.equal(thrown.menus[0].label, 'Administration');
  assert.equal(thrown.menus[0].sections[0].actions[0].label, 'Find people');
  assert.equal(thrown.menus[0].sections[0].actions[0].appearance, undefined);
  assert.equal(unsupported.menus[0].label, 'Administration');
  assert.equal(unsupported.menus[0].sections[0].actions[0].label, 'Find people');
  assert.equal(unsupported.menus[0].sections[0].actions[0].appearance, undefined);
  assert.equal(bounded.menus[0].label.includes('\u0000'), false);
  assert.equal(bounded.menus[0].label.length, 512);
  assert.equal(bounded.menus[0].sections[0].actions[0].label.includes('\u0000'), false);
  assert.equal(bounded.menus[0].sections[0].actions[0].label.length, 512);
});

test('qualification is deterministic across policy family lifecycle and width', () => {
  const projection = projectCausewayMenuBar(bar(), {generation: 2});
  const base = {role: 'primary', generation: 2, policy: 'vaadin', familyAvailable: true, connected: true, visible: true, current: true, projection};
  assert.equal(qualifyCausewayMenuBar({...base, width: 1200}).presentation, 'vaadin-wide');
  assert.equal(qualifyCausewayMenuBar({...base, width: 390}).presentation, 'vaadin-overflow');
  assert.equal(qualifyCausewayMenuBar({...base, width: 0}).reason, 'width-unavailable');
  assert.equal(qualifyCausewayMenuBar({...base, policy: 'native', width: 1200}).reason, 'policy-native');
  assert.equal(qualifyCausewayMenuBar({...base, familyAvailable: false, width: 1200}).reason, 'family-failed');
  assert.equal(qualifyCausewayMenuBar({...base, current: false, width: 1200}).reason, 'stale-generation');
});
