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
import {
  CausewayTemporalRangeStatus,
  compareCausewayLocalTemporal,
  resolveCausewayTemporalRange,
  validateCausewayTemporalRange
} from '../src/temporal-range.mjs';

const now = new Date(2026, 7, 31, 13, 14, 15, 123);

test('normalizes absolute closed ranges for each supported local temporal type', () => {
  assert.deepEqual(resolveCausewayTemporalRange({
    semanticType: 'LocalDate', min: '2026-01-01', max: '2026-12-31', now
  }), {
    status: CausewayTemporalRangeStatus.VALID,
    semanticType: 'LocalDate',
    min: '2026-01-01',
    max: '2026-12-31',
    diagnostic: null
  });
  assert.equal(resolveCausewayTemporalRange({
    semanticType: 'LocalTime', min: '08:00', max: '18:00:00.000000001', now
  }).status, CausewayTemporalRangeStatus.VALID);
  assert.equal(resolveCausewayTemporalRange({
    semanticType: 'LocalDateTime', min: '2026-08-31T08:00', max: '2026-08-31T18:00:00.1', now
  }).status, CausewayTemporalRangeStatus.VALID);
});

test('resolves relative bounds once from local calendar and clock fields', () => {
  const date = resolveCausewayTemporalRange({
    semanticType: 'LocalDate', min: 'today', max: 'tomorrow', now
  });
  assert.deepEqual({min: date.min, max: date.max}, {min: '2026-08-31', max: '2026-09-01'});
  const dateTime = resolveCausewayTemporalRange({semanticType: 'LocalDateTime', min: 'now', now});
  assert.equal(dateTime.min, '2026-08-31T13:14:15.123');
  assert.equal(dateTime.max, null);
});

test('rejects blank malformed incompatible and inverted ranges atomically', () => {
  const cases = [
    [{semanticType: 'LocalDate', min: ''}, 'blank-bound'],
    [{semanticType: 'LocalDate', min: '31/08/2026'}, 'invalid-min'],
    [{semanticType: 'LocalTime', min: 'today'}, 'invalid-min'],
    [{semanticType: 'LocalDateTime', max: 'tomorrow'}, 'invalid-max'],
    [{semanticType: 'LocalTime', min: '18:00', max: '08:00'}, 'inverted']
  ];
  for (const [candidate, diagnostic] of cases) {
    const range = resolveCausewayTemporalRange({...candidate, now});
    assert.equal(range.status, CausewayTemporalRangeStatus.INVALID);
    assert.equal(range.diagnostic, diagnostic);
    assert.equal(range.min, null);
    assert.equal(range.max, null);
  }
  assert.equal(resolveCausewayTemporalRange({semanticType: 'String', min: '08:00', now}).status,
    CausewayTemporalRangeStatus.INAPPLICABLE);
  assert.equal(resolveCausewayTemporalRange({semanticType: 'LocalDate', now}).status,
    CausewayTemporalRangeStatus.ABSENT);
});

test('compares equivalent precision and date-time parts without lexical or timezone drift', () => {
  assert.equal(compareCausewayLocalTemporal('LocalTime', '08:00', '08:00:00'), 0);
  assert.equal(compareCausewayLocalTemporal('LocalTime', '08:00:00.1', '08:00:00.100000000'), 0);
  assert.equal(compareCausewayLocalTemporal('LocalTime', '08:00:00.000000001', '08:00:00.000000002'), -1);
  assert.equal(compareCausewayLocalTemporal('LocalDateTime', '2026-08-31T23:59:59.999999999', '2026-09-01T00:00'), -1);
  assert.equal(compareCausewayLocalTemporal('LocalDate', '2028-02-29', '2028-02-29'), 0);
  assert.throws(() => compareCausewayLocalTemporal('LocalDate', '2026-02-30', '2026-03-01'), TypeError);
});

test('validates both inclusive boundaries and retains a bounded correction reason', () => {
  const range = resolveCausewayTemporalRange({semanticType: 'LocalTime', min: '08:00', max: '18:00', now});
  assert.equal(validateCausewayTemporalRange('', range), null);
  assert.equal(validateCausewayTemporalRange('08:00:00', range), null);
  assert.equal(validateCausewayTemporalRange('18:00:00.000000000', range), null);
  assert.deepEqual(validateCausewayTemporalRange('07:59:59.999999999', range), {
    code: 'TEMPORAL_RANGE_MIN',
    message: 'Enter a value on or after 08:00.'
  });
  assert.deepEqual(validateCausewayTemporalRange('18:00:00.000000001', range), {
    code: 'TEMPORAL_RANGE_MAX',
    message: 'Enter a value on or before 18:00.'
  });
  assert.equal(validateCausewayTemporalRange('not-a-time', range), null);
});
