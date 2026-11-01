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

import {namedType} from './introspection.mjs';

const INTEGER_PATTERN = /^[+-]?\d+$/;
const DECIMAL_PATTERN = /^[+-]?(?:\d+(?:\.\d*)?|\.\d+)(?:[eE][+-]?\d+)?$/;
const LOCAL_DATE_PATTERN = /^(\d{4})-(\d{2})-(\d{2})$/;
const LOCAL_TIME_PATTERN = /^(?:[01]\d|2[0-3]):[0-5]\d(?::[0-5]\d(?:\.\d{1,9})?)?$/;
const OFFSET_PATTERN = /(?:Z|[+-](?:0\d|1\d|2[0-3]):[0-5]\d)$/;
const ZONED_DATE_TIME_PATTERN = /^(.+)(?:Z|[+-](?:0\d|1\d|2[0-3]):[0-5]\d)\[([^\]]+)]$/;
const LONG_MIN = -9223372036854775808n;
const LONG_MAX = 9223372036854775807n;
const INT_MIN = -2147483648;
const INT_MAX = 2147483647;

export class CausewayValueCodecError extends Error {
  constructor(message, code = 'INVALID_VALUE') {
    super(message);
    this.name = 'CausewayValueCodecError';
    this.code = code;
  }
}

export function semanticTypeName(context) {
  const advertised = String(context?.semanticType ?? '').trim();
  if (advertised) {
    const typeName = advertised.split('_').filter(Boolean).at(-1) ?? advertised;
    return {
      int: 'Int', Integer: 'Int',
      long: 'Long',
      boolean: 'Boolean',
      byte: 'Byte',
      short: 'Short',
      float: 'Float',
      double: 'Double',
      char: 'Char', Character: 'Char'
    }[typeName] ?? typeName;
  }
  return namedType(context?.inputType);
}

export class CausewayValueCodecRegistry {
  constructor(registrations = []) {
    this.registrations = [];
    registrations.forEach(registration => this.register(registration));
  }

  register(registration) {
    if (!registration?.id || typeof registration.supports !== 'function' || typeof registration.parse !== 'function') {
      throw new Error('A value codec registration requires id, supports, and parse.');
    }
    const normalized = Object.freeze({
      priority: 0,
      normalize: value => value,
      toControlValue: value => value ?? '',
      toGraphQLValue: value => value,
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
    return this.registrations.find(registration => registration.supports(context)) ?? unsupportedValueCodec;
  }
}

const exactNumericCodec = Object.freeze({
  id: 'exact-numeric',
  priority: 300,
  supports: context => ['Long', 'BigInteger', 'BigDecimal'].includes(semanticTypeName(context)),
  normalize: (value, context) => value == null ? null : parseExactNumeric(String(value), context),
  toControlValue: value => value ?? '',
  parse: context => parseExactNumeric(context.value, context),
  toGraphQLValue: value => value
});

const machineNumericCodec = Object.freeze({
  id: 'machine-numeric',
  priority: 290,
  supports: context => ['Int', 'Short', 'Byte', 'Float', 'Double'].includes(semanticTypeName(context)),
  normalize: (value, context) => value == null ? null : parseMachineNumeric(String(value), context),
  toControlValue: value => value ?? '',
  parse: context => parseMachineNumeric(context.value, context),
  toGraphQLValue: value => value
});

const booleanCodec = Object.freeze({
  id: 'boolean',
  priority: 280,
  supports: context => semanticTypeName(context) === 'Boolean',
  normalize: value => value == null ? null : Boolean(value),
  toControlValue: value => value == null ? '' : String(Boolean(value)),
  parse: context => {
    if (context.required) {
      return Boolean(context.checked);
    }
    if (context.value === '' || context.value == null) {
      return null;
    }
    if (context.value === true || context.value === 'true') {
      return true;
    }
    if (context.value === false || context.value === 'false') {
      return false;
    }
    throw new CausewayValueCodecError('Choose true, false, or no value.');
  },
  toGraphQLValue: value => value
});

const temporalCodec = Object.freeze({
  id: 'temporal',
  priority: 270,
  supports: context => [
    'LocalDate', 'LocalTime', 'LocalDateTime',
    'OffsetTime', 'OffsetDateTime', 'DateTime', 'LegacyDateTime', 'ZonedDateTime'
  ].includes(semanticTypeName(context)),
  normalize: (value, context) => value == null ? null : parseTemporal(String(value), context),
  toControlValue: value => value ?? '',
  parse: context => parseTemporal(context.value, context),
  toGraphQLValue: value => value
});

const protectedCodec = Object.freeze({
  id: 'protected',
  priority: 265,
  sensitive: true,
  supports: context => ['Password', 'ProtectedValue'].includes(semanticTypeName(context)),
  normalize: () => null,
  toControlValue: () => '',
  parse: context => requiredValue(context.value, context),
  toGraphQLValue: value => value
});

const urlCodec = Object.freeze({
  id: 'url',
  priority: 260,
  supports: context => ['URL', 'Url'].includes(semanticTypeName(context)),
  normalize: (value, context) => value == null ? null : parseUrl(String(value), context),
  toControlValue: value => value ?? '',
  parse: context => parseUrl(context.value, context),
  toGraphQLValue: value => value
});

const scalarCodec = Object.freeze({
  id: 'scalar',
  priority: 100,
  supports: context => ['String', 'ID', 'UUID', 'Locale', 'Char'].includes(semanticTypeName(context))
    || innermostType(context.inputType)?.kind === 'ENUM',
  normalize: value => value,
  toControlValue: value => value ?? '',
  parse: context => requiredValue(context.value, context),
  toGraphQLValue: value => value
});

const referenceCodec = Object.freeze({
  id: 'reference',
  priority: 90,
  supports: context => ['OBJECT', 'INTERFACE', 'UNION', 'LIST', 'INPUT_OBJECT'].includes(innermostType(context.inputType)?.kind)
    || semanticTypeName(context)?.startsWith('rich__')
    || semanticTypeName(context)?.startsWith('simple__'),
  normalize: value => value,
  toControlValue: value => value,
  parse: context => context.value,
  toGraphQLValue: value => value
});

const unsupportedValueCodec = Object.freeze({
  id: 'unsupported',
  priority: -1000,
  supports: () => true,
  normalize: value => value,
  toControlValue: () => '',
  parse: context => {
    throw new CausewayValueCodecError(
      `No reversible input codec is registered for '${semanticTypeName(context) ?? 'unknown input type'}'.`,
      'UNSUPPORTED_VALUE_CODEC');
  },
  toGraphQLValue: () => {
    throw new CausewayValueCodecError('Unsupported values cannot be submitted.', 'UNSUPPORTED_VALUE_CODEC');
  }
});

const unsupportedResourceCodec = Object.freeze({
  ...unsupportedValueCodec,
  priority: 400,
  supports: context => ['Blob', 'Clob', 'LocalResourcePath', 'LocalResourcePathValue', 'LocalResourcePathInput']
    .includes(semanticTypeName(context))
});

export const defaultValueCodecRegistry = new CausewayValueCodecRegistry([
  unsupportedResourceCodec,
  exactNumericCodec,
  machineNumericCodec,
  booleanCodec,
  temporalCodec,
  protectedCodec,
  urlCodec,
  scalarCodec,
  referenceCodec
]);

export function selectCausewayValueCodec(context, registry = defaultValueCodecRegistry) {
  return registry.select(context);
}

export function parseCausewayValue(codec, context) {
  const parsed = codec.parse(context);
  return codec.toGraphQLValue(parsed, context);
}

function parseExactNumeric(value, context) {
  const lexical = nullableLexical(value, context);
  if (lexical == null) {
    return null;
  }
  const typeName = semanticTypeName(context);
  if (typeName === 'BigDecimal') {
    if (!DECIMAL_PATTERN.test(lexical)) {
      throw new CausewayValueCodecError('Enter an exact decimal number.');
    }
    return lexical;
  }
  if (!INTEGER_PATTERN.test(lexical)) {
    throw new CausewayValueCodecError('Enter a whole number.');
  }
  if (typeName === 'Long') {
    const valueAsBigInt = BigInt(lexical);
    if (valueAsBigInt < LONG_MIN || valueAsBigInt > LONG_MAX) {
      throw new CausewayValueCodecError('Enter a whole number within the signed 64-bit range.');
    }
  }
  return lexical;
}

function parseMachineNumeric(value, context) {
  const lexical = nullableLexical(value, context);
  if (lexical == null) {
    return null;
  }
  const typeName = semanticTypeName(context);
  if (['Int', 'Short', 'Byte'].includes(typeName)) {
    if (!INTEGER_PATTERN.test(lexical)) {
      throw new CausewayValueCodecError('Enter a whole number.');
    }
    const parsed = Number(lexical);
    const [minimum, maximum] = typeName === 'Byte'
      ? [-128, 127]
      : typeName === 'Short' ? [-32768, 32767] : [INT_MIN, INT_MAX];
    if (!Number.isSafeInteger(parsed) || parsed < minimum || parsed > maximum) {
      throw new CausewayValueCodecError(`Enter a whole number from ${minimum} to ${maximum}.`);
    }
    return parsed;
  }
  const parsed = Number(lexical);
  if (!Number.isFinite(parsed)) {
    throw new CausewayValueCodecError('Enter a finite number.');
  }
  return parsed;
}

function parseTemporal(value, context) {
  const lexical = nullableLexical(value, context);
  if (lexical == null) {
    return null;
  }
  const typeName = semanticTypeName(context);
  const valid = typeName === 'LocalDate'
    ? isValidCausewayLocalDate(lexical)
    : typeName === 'LocalTime'
      ? isValidCausewayLocalTime(lexical)
      : typeName === 'LocalDateTime'
        ? isValidCausewayLocalDateTime(lexical)
        : typeName === 'OffsetTime'
          ? validOffsetTime(lexical)
          : typeName === 'ZonedDateTime'
            ? validZonedDateTime(lexical)
            : validOffsetDateTime(lexical);
  if (!valid) {
    throw new CausewayValueCodecError(`Enter a valid ${typeName} value.`);
  }
  return lexical;
}

function parseUrl(value, context) {
  const lexical = nullableLexical(value, context);
  if (lexical == null) {
    return null;
  }
  try {
    const parsed = new URL(lexical);
    if (!['http:', 'https:'].includes(parsed.protocol)) {
      throw new Error('unsupported protocol');
    }
  } catch {
    throw new CausewayValueCodecError('Enter an absolute HTTP or HTTPS URL.');
  }
  return lexical;
}

function nullableLexical(value, context) {
  const lexical = String(value ?? '');
  if (lexical === '') {
    if (context.required) {
      throw new CausewayValueCodecError('A value is required.', 'REQUIRED_VALUE');
    }
    return null;
  }
  return lexical;
}

function requiredValue(value, context) {
  if ((value == null || value === '') && context.required) {
    throw new CausewayValueCodecError('A value is required.', 'REQUIRED_VALUE');
  }
  return value == null || value === '' ? null : value;
}

export function isValidCausewayLocalDate(value) {
  const match = LOCAL_DATE_PATTERN.exec(value);
  if (!match) {
    return false;
  }
  const year = Number(match[1]);
  const month = Number(match[2]);
  const day = Number(match[3]);
  const date = new Date(Date.UTC(year, month - 1, day));
  return date.getUTCFullYear() === year && date.getUTCMonth() === month - 1 && date.getUTCDate() === day;
}

export function isValidCausewayLocalTime(value) {
  return LOCAL_TIME_PATTERN.test(value);
}

export function isValidCausewayLocalDateTime(value) {
  const separator = value.indexOf('T');
  return separator > 0
    && isValidCausewayLocalDate(value.slice(0, separator))
    && isValidCausewayLocalTime(value.slice(separator + 1));
}

function validOffsetTime(value) {
  const offsetStart = Math.max(value.lastIndexOf('+'), value.lastIndexOf('-'), value.endsWith('Z') ? value.length - 1 : -1);
  return offsetStart > 0 && LOCAL_TIME_PATTERN.test(value.slice(0, offsetStart)) && OFFSET_PATTERN.test(value.slice(offsetStart));
}

function validOffsetDateTime(value) {
  const zoneStart = Math.max(value.lastIndexOf('+'), value.lastIndexOf('-'), value.endsWith('Z') ? value.length - 1 : -1);
  return zoneStart > 10 && isValidCausewayLocalDateTime(value.slice(0, zoneStart)) && OFFSET_PATTERN.test(value.slice(zoneStart));
}

function validZonedDateTime(value) {
  const match = ZONED_DATE_TIME_PATTERN.exec(value);
  const bracket = value.lastIndexOf('[');
  return Boolean(match?.[2]) && bracket > 0 && validOffsetDateTime(value.slice(0, bracket));
}

function innermostType(typeRef) {
  let current = typeRef;
  while (current?.ofType) {
    current = current.ofType;
  }
  return current;
}
