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

import assert from 'node:assert/strict';
import test from 'node:test';
import {installDomShim} from './dom-shim.mjs';

installDomShim();
const {
  CausewayValueCodecError,
  CausewayValueCodecRegistry,
  defaultValueCodecRegistry,
  parseCausewayValue,
  renderCausewayEditor,
  selectCausewayValueCodec
} = await import('../src/index.mjs');

const scalar = name => ({kind: 'SCALAR', name, ofType: null});
const required = type => ({kind: 'NON_NULL', name: null, ofType: type});
const context = (name, value = null, options = {}) => ({
  name: name.toLowerCase(),
  value,
  choices: [],
  suggestions: [],
  enumValues: [],
  inputType: options.required ? required(scalar(name)) : scalar(name),
  required: options.required ?? false,
  inputId: `${name}-input`,
  labelId: `${name}-label`,
  descriptionId: '',
  errorId: '',
  testId: ''
});

function parse(typeName, value, options = {}) {
  const codecContext = {...context(typeName, null, options), value, checked: options.checked};
  return parseCausewayValue(selectCausewayValueCodec(codecContext), codecContext);
}

test('exact numeric codecs preserve lexical precision scale boundaries and null', () => {
  const decimal = renderCausewayEditor(context('BigDecimal', '9007199254740993.1200'));
  assert.equal(decimal.editorId, 'exact-number');
  assert.equal(decimal.codecId, 'exact-numeric');
  assert.match(decimal.html, /type="text"/);
  assert.match(decimal.html, /value="9007199254740993.1200"/);
  assert.equal(decimal.editor.parse({value: '9007199254740993.1200'}), '9007199254740993.1200');

  const advertisedString = renderCausewayEditor({
    ...context('String', '9007199254740993.1200'),
    semanticType: 'rich__java_math_BigDecimal'
  });
  assert.equal(advertisedString.editorId, 'exact-number');
  assert.equal(advertisedString.editor.parse({value: '9007199254740993.1200'}), '9007199254740993.1200');

  assert.equal(parse('BigInteger', '123456789012345678901234567890'), '123456789012345678901234567890');
  assert.equal(parse('Long', '-9223372036854775808'), '-9223372036854775808');
  assert.equal(parse('Long', '9223372036854775807'), '9223372036854775807');
  assert.equal(parse('BigDecimal', '+1.2300e+40'), '+1.2300e+40');
  assert.equal(parse('BigDecimal', ''), null);
  assert.throws(() => parse('Long', '9223372036854775808'), CausewayValueCodecError);
  assert.throws(() => parse('BigInteger', '1.5'), CausewayValueCodecError);
  assert.throws(() => parse('BigDecimal', '1.2.3'), CausewayValueCodecError);
  assert.throws(() => parse('BigDecimal', '', {required: true}), /required/);
});

test('machine numeric codecs accept only representable values', () => {
  assert.equal(parse('Int', '2147483647'), 2147483647);
  assert.equal(parse('Byte', '-128'), -128);
  assert.equal(parse('Short', '32767'), 32767);
  assert.equal(parse('Float', '1.25'), 1.25);
  assert.throws(() => parse('Int', '2147483648'), /-2147483648 to 2147483647/);
  assert.throws(() => parse('Byte', '128'), /-128 to 127/);
  assert.throws(() => parse('Float', 'Infinity'), /finite/);
});

test('nullable Boolean remains distinct from false while required Boolean is two-state', () => {
  const nullable = renderCausewayEditor(context('Boolean', null));
  assert.equal(nullable.editorId, 'boolean');
  assert.equal(nullable.codecId, 'boolean');
  assert.match(nullable.html, /<select/);
  assert.match(nullable.html, /value="" selected/);
  assert.equal(nullable.editor.parse({value: ''}), null);
  assert.equal(nullable.editor.parse({value: 'false'}), false);
  assert.equal(nullable.editor.parse({value: 'true'}), true);

  const twoState = renderCausewayEditor(context('Boolean', false, {required: true}));
  assert.match(twoState.html, /type="checkbox"/);
  assert.equal(twoState.editor.parse({checked: false}), false);
  assert.equal(twoState.editor.parse({checked: true}), true);
});

test('temporal codecs preserve local fractional offset zoned and legacy lexical forms', () => {
  assert.equal(parse('LocalDate', '2024-02-29'), '2024-02-29');
  assert.equal(parse('LocalTime', '23:59:58.123456789'), '23:59:58.123456789');
  assert.equal(parse('LocalDateTime', '2026-08-23T10:15:30.123456'), '2026-08-23T10:15:30.123456');
  assert.equal(parse('OffsetTime', '10:15:30.123+05:30'), '10:15:30.123+05:30');
  assert.equal(parse('OffsetDateTime', '2026-08-23T10:15:30-04:00'), '2026-08-23T10:15:30-04:00');
  assert.equal(parse('DateTime', '2026-08-23T14:15:30Z'), '2026-08-23T14:15:30Z');
  assert.equal(parse('LegacyDateTime', '2026-08-23T14:15:30Z'), '2026-08-23T14:15:30Z');
  assert.equal(parse('ZonedDateTime', '2026-11-01T01:30:00-04:00[America/New_York]'),
    '2026-11-01T01:30:00-04:00[America/New_York]');

  const propertyContext = renderCausewayEditor({
    ...context('String', '2026-11-01T01:30:00-04:00[America/New_York]'),
    name: 'zonedProperty',
    semanticType: 'rich__java_time_ZonedDateTime'
  });
  const actionContext = renderCausewayEditor({
    ...context('Time', null),
    name: 'offsetParameter',
    semanticType: 'rich__java_time_OffsetTime'
  });
  assert.equal(propertyContext.editorId, 'temporal');
  assert.equal(propertyContext.editor.parse({value: '2026-11-01T01:30:00-05:00[America/New_York]'}),
    '2026-11-01T01:30:00-05:00[America/New_York]');
  assert.equal(actionContext.editor.parse({value: ''}), null);
  assert.equal(actionContext.editor.parse({value: '10:15:30.123456789+05:30'}),
    '10:15:30.123456789+05:30');
  assert.throws(() => propertyContext.editor.parse({value: '2026-11-01T01:30:00'}), /ZonedDateTime/);
  assert.throws(() => parse('LocalDate', '2023-02-29'), /LocalDate/);
  assert.throws(() => parse('OffsetDateTime', '2026-08-23T10:15:30'), /OffsetDateTime/);
  assert.throws(() => parse('ZonedDateTime', '2026-08-23T10:15:30Z'), /ZonedDateTime/);
});

test('URL is constrained and custom scalar input fails closed without registration', () => {
  assert.equal(parse('Url', 'https://causeway.apache.org/'), 'https://causeway.apache.org/');
  assert.throws(() => parse('Url', 'javascript:alert(1)'), /HTTP or HTTPS/);

  const protectedValue = renderCausewayEditor({
    ...context('String', 'prior secret', {required: true}),
    semanticType: 'rich__causeway_applib_value_Password'
  });
  assert.equal(protectedValue.codecId, 'protected');
  assert.match(protectedValue.html, /type="password"/);
  assert.match(protectedValue.html, /autocomplete="new-password"/);
  assert.doesNotMatch(protectedValue.html, /prior secret/);
  assert.equal(protectedValue.editor.parse({value: 'new secret'}), 'new secret');

  for (const resourceType of ['Blob', 'Clob', 'LocalResourcePath']) {
    const resource = renderCausewayEditor({
      ...context('String', null),
      semanticType: `rich__causeway_applib_value_${resourceType}`
    });
    assert.equal(resource.editorId, 'unsupported');
    assert.throws(() => resource.editor.parse({value: 'private resource content'}),
      /No reversible input codec/);
  }

  const unsupported = renderCausewayEditor(context('ApplicationMoney', '10 GBP'));
  assert.equal(unsupported.editorId, 'unsupported');
  assert.equal(unsupported.codecId, 'unsupported');
  assert.match(unsupported.html, /Unsupported editor/);
  assert.throws(() => unsupported.editor.parse({value: '11 GBP'}), /No reversible input codec/);

  const registry = new CausewayValueCodecRegistry(defaultValueCodecRegistry.registrations);
  registry.register({
    id: 'money',
    priority: 1000,
    supports: candidate => candidate.inputType?.name === 'ApplicationMoney',
    parse: candidate => ({amount: candidate.value, currency: 'GBP'}),
    toControlValue: value => value?.amount ?? '',
    toGraphQLValue: value => value
  });
  const codec = registry.select(context('ApplicationMoney'));
  assert.deepEqual(parseCausewayValue(codec, {...context('ApplicationMoney'), value: '11.25'}),
    {amount: '11.25', currency: 'GBP'});
});
