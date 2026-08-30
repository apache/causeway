/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0.
 */

import assert from 'node:assert/strict';
import test from 'node:test';

import {
  ActionPromptStyle,
  composeActionTooltip,
  normalizeActionPresentation,
  normalizeAuthoredActionPromptStyle,
  normalizeFontAwesomeIcon,
  renderActionContent
} from '../src/action-presentation.mjs';

test('normalizes bounded distinct action presentation', () => {
  assert.deepEqual(normalizeActionPresentation({
    friendlyName: 'Place order',
    description: 'Creates an order',
    cssClassFa: 'cart-shopping',
    cssClassFaPosition: 'RIGHT'
  }), {
    name: 'Place order',
    description: 'Creates an order',
    areYouSure: false,
    promptStyle: 'DIALOG_MODAL',
    icon: {classes: ['fa-solid', 'fa-cart-shopping'], position: 'RIGHT'}
  });
  assert.equal(normalizeActionPresentation({name: 'Delete', description: 'delete'}).description, '');
  assert.equal(normalizeActionPresentation({name: 'Delete', areYouSure: true}).areYouSure, true);
  assert.equal(normalizeActionPresentation({name: 'Delete', areYouSure: 'true'}).areYouSure, false);
});

test('normalizes canonical and authored action prompt styles', () => {
  assert.equal(normalizeActionPresentation({promptStyle: 'inline_as_if_edit'}).promptStyle, ActionPromptStyle.INLINE);
  assert.equal(normalizeActionPresentation({promptStyle: 'dialog-sidebar'}).promptStyle, ActionPromptStyle.DIALOG_SIDEBAR);
  assert.equal(normalizeActionPresentation({promptStyle: 'DIALOG'}).promptStyle, ActionPromptStyle.DIALOG_MODAL);
  assert.equal(normalizeActionPresentation({promptStyle: 'unexpected'}).promptStyle, ActionPromptStyle.DIALOG_MODAL);
  assert.equal(normalizeAuthoredActionPromptStyle('dialog_sidebar'), ActionPromptStyle.DIALOG_SIDEBAR);
  assert.equal(normalizeAuthoredActionPromptStyle('inline-as-if-edit'), null);
});

test('rejects malformed Font Awesome hints and normalizes accepted classes and position', () => {
  assert.deepEqual(normalizeFontAwesomeIcon('fa-regular fa-calendar-check', 'LEFT'), {
    classes: ['fa-regular', 'fa-calendar-check'],
    position: 'LEFT'
  });
  assert.equal(normalizeFontAwesomeIcon('fa-user\" onclick="alert(1)', 'RIGHT'), null);
  assert.equal(normalizeFontAwesomeIcon('', 'LEFT'), null);
});

test('composes tooltip sections and renders escaped positioned icon content', () => {
  assert.equal(composeActionTooltip('Creates an order', 'Credit is unavailable'),
    'Creates an order\n\nCredit is unavailable');
  const right = normalizeFontAwesomeIcon('cart-shopping', 'RIGHT');
  assert.equal(renderActionContent('Place <order>', right),
    '<span class="causeway-action-label">Place &lt;order&gt;</span><i class="causeway-action-icon fa-solid fa-cart-shopping" aria-hidden="true"></i>');
});
