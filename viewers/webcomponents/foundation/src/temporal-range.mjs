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
  isValidCausewayLocalDate,
  isValidCausewayLocalDateTime,
  isValidCausewayLocalTime
} from './value-codecs.mjs';

export const CausewayTemporalRangeStatus = Object.freeze({
  VALID: 'valid',
  INVALID: 'invalid',
  ABSENT: 'absent',
  INAPPLICABLE: 'inapplicable'
});

const SUPPORTED_TYPES = new Set(['LocalDate', 'LocalTime', 'LocalDateTime']);
const LOCAL_TIME_PARTS = /^(\d{2}):(\d{2})(?::(\d{2})(?:\.(\d{1,9}))?)?$/;
const LOCAL_DATE_PARTS = /^(\d{4})-(\d{2})-(\d{2})$/;

export function resolveCausewayTemporalRange({semanticType, min = null, max = null, now = new Date()} = {}) {
  if (!SUPPORTED_TYPES.has(semanticType)) {
    return temporalRange(CausewayTemporalRangeStatus.INAPPLICABLE, semanticType);
  }
  if (min == null && max == null) {
    return temporalRange(CausewayTemporalRangeStatus.ABSENT, semanticType);
  }
  if ((min != null && String(min).trim() === '') || (max != null && String(max).trim() === '')) {
    return temporalRange(CausewayTemporalRangeStatus.INVALID, semanticType, {diagnostic: 'blank-bound'});
  }

  const resolvedMin = resolveBound(semanticType, min, now);
  const resolvedMax = resolveBound(semanticType, max, now);
  if (min != null && resolvedMin == null) {
    return temporalRange(CausewayTemporalRangeStatus.INVALID, semanticType, {diagnostic: 'invalid-min'});
  }
  if (max != null && resolvedMax == null) {
    return temporalRange(CausewayTemporalRangeStatus.INVALID, semanticType, {diagnostic: 'invalid-max'});
  }
  if (resolvedMin != null && resolvedMax != null
      && compareCausewayLocalTemporal(semanticType, resolvedMin, resolvedMax) > 0) {
    return temporalRange(CausewayTemporalRangeStatus.INVALID, semanticType, {diagnostic: 'inverted'});
  }
  return temporalRange(CausewayTemporalRangeStatus.VALID, semanticType, {
    min: resolvedMin,
    max: resolvedMax
  });
}

export function validateCausewayTemporalRange(value, range) {
  if (range?.status !== CausewayTemporalRangeStatus.VALID || value == null || value === '') {
    return null;
  }
  if (localTemporalParts(range.semanticType, String(value)) == null) {
    return null;
  }
  if (range.min != null && compareCausewayLocalTemporal(range.semanticType, String(value), range.min) < 0) {
    return Object.freeze({
      code: 'TEMPORAL_RANGE_MIN',
      message: `Enter a value on or after ${range.min}.`
    });
  }
  if (range.max != null && compareCausewayLocalTemporal(range.semanticType, String(value), range.max) > 0) {
    return Object.freeze({
      code: 'TEMPORAL_RANGE_MAX',
      message: `Enter a value on or before ${range.max}.`
    });
  }
  return null;
}

export function compareCausewayLocalTemporal(semanticType, left, right) {
  const leftParts = localTemporalParts(semanticType, left);
  const rightParts = localTemporalParts(semanticType, right);
  if (!leftParts || !rightParts) {
    throw new TypeError(`Cannot compare invalid ${semanticType ?? 'local temporal'} values.`);
  }
  for (let index = 0; index < leftParts.length; index += 1) {
    if (leftParts[index] !== rightParts[index]) {
      return leftParts[index] < rightParts[index] ? -1 : 1;
    }
  }
  return 0;
}

function resolveBound(semanticType, candidate, now) {
  if (candidate == null) return null;
  const token = String(candidate).trim();
  if (semanticType === 'LocalDate' && ['today', 'tomorrow'].includes(token)) {
    const date = token === 'tomorrow'
      ? new Date(now.getFullYear(), now.getMonth(), now.getDate() + 1)
      : now;
    return formatLocalDate(date);
  }
  if (semanticType === 'LocalDateTime' && token === 'now') {
    return formatLocalDateTime(now);
  }
  return localTemporalParts(semanticType, token) ? token : null;
}

function localTemporalParts(semanticType, value) {
  if (semanticType === 'LocalDate') {
    return dateParts(value);
  }
  if (semanticType === 'LocalTime') {
    return timeParts(value);
  }
  if (semanticType === 'LocalDateTime' && isValidCausewayLocalDateTime(value)) {
    const separator = value.indexOf('T');
    return [...dateParts(value.slice(0, separator)), ...timeParts(value.slice(separator + 1))];
  }
  return null;
}

function dateParts(value) {
  if (!isValidCausewayLocalDate(value)) return null;
  return LOCAL_DATE_PARTS.exec(value).slice(1).map(Number);
}

function timeParts(value) {
  if (!isValidCausewayLocalTime(value)) return null;
  const match = LOCAL_TIME_PARTS.exec(value);
  return [
    Number(match[1]),
    Number(match[2]),
    Number(match[3] ?? 0),
    Number(String(match[4] ?? '').padEnd(9, '0') || 0)
  ];
}

function formatLocalDate(date) {
  return `${pad(date.getFullYear(), 4)}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}

function formatLocalDateTime(date) {
  const datePart = formatLocalDate(date);
  const timePart = `${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
  const milliseconds = date.getMilliseconds();
  return `${datePart}T${timePart}${milliseconds ? `.${pad(milliseconds, 3)}` : ''}`;
}

function pad(value, length = 2) {
  return String(value).padStart(length, '0');
}

function temporalRange(status, semanticType, {min = null, max = null, diagnostic = null} = {}) {
  return Object.freeze({status, semanticType: semanticType ?? null, min, max, diagnostic});
}
