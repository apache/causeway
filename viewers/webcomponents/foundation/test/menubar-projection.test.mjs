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
          {serviceLogicalTypeName: 'demo.People', actionId: 'find', label: 'Find people', iconHint: 'search'},
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
