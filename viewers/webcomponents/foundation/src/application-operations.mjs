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

import {assertGraphQLName} from './schema-names.mjs';

const MENU_DESCRIPTOR_FIELDS = Object.freeze(['href', 'mediaType', 'formatVersion', 'generation', 'cacheControl']);
const ISSUE_FIELDS = Object.freeze(['code', 'message']);

export function buildApplicationEntryReadOperation({description, schemaNames}) {
  if (!description?.supported || !description.applicationField || !description.menuBarsField) {
    throw new Error('The rich GraphQL application menu capability is unavailable.');
  }
  const applicationField = assertGraphQLName(description.applicationField.name, 'application field');
  const menuBarsField = assertGraphQLName(description.menuBarsField.name, 'menu bars field');
  const availableMenuFields = new Set(description.menuBarsType?.fields?.map(field => field.name) ?? []);
  const menuSelection = MENU_DESCRIPTOR_FIELDS
    .filter(field => availableMenuFields.has(field))
    .map(field => `        ${assertGraphQLName(field, 'menu descriptor field')}`);
  if (!availableMenuFields.has('href')) {
    throw new Error('The rich GraphQL application menu descriptor does not expose href.');
  }
  const applicationSelection = [
    `      ${menuBarsField} {`,
    ...menuSelection,
    '      }'
  ];
  const homeSelection = buildHomeSelection(description);
  if (homeSelection.length > 0) {
    applicationSelection.push(...homeSelection);
  }
  if (description.issuesField && description.issueType) {
    const availableIssueFields = new Set(description.issueType.fields?.map(field => field.name) ?? []);
    const issueSelection = ISSUE_FIELDS
      .filter(field => availableIssueFields.has(field))
      .map(field => `        ${assertGraphQLName(field, 'application issue field')}`);
    if (issueSelection.length > 0) {
      applicationSelection.push(`      ${assertGraphQLName(description.issuesField.name, 'application issues field')} {`);
      applicationSelection.push(...issueSelection);
      applicationSelection.push('      }');
    }
  }
  const richRoot = schemaNames.richRootField;
  const applicationRead = richRoot
    ? `  ${assertGraphQLName(richRoot, 'rich root field')} {\n    ${applicationField} {\n${applicationSelection.join('\n')}\n    }\n  }`
    : `  ${applicationField} {\n${applicationSelection.map(line => line.slice(2)).join('\n')}\n  }`;
  return Object.freeze({
    document: `query CausewayReadApplicationEntry {\n${applicationRead}\n}`,
    variables: Object.freeze({}),
    operationName: 'CausewayReadApplicationEntry',
    applicationPath: richRoot ? [richRoot, applicationField] : [applicationField]
  });
}

function buildHomeSelection(description) {
  const homeField = description.homeField;
  const homeType = description.homeType;
  const homeObjectField = description.homeObjectField;
  const possibleTypes = description.homeObjectUnion?.possibleTypes ?? [];
  if (!homeField || !homeType || !homeObjectField || possibleTypes.length === 0) {
    return [];
  }
  const homeFields = new Set(homeType.fields?.map(field => field.name) ?? []);
  const selection = [`      ${assertGraphQLName(homeField.name, 'application home field')} {`];
  for (const fieldName of ['kind', 'logicalTypeName']) {
    if (homeFields.has(fieldName)) {
      selection.push(`        ${assertGraphQLName(fieldName, 'application home value field')}`);
    }
  }
  selection.push(`        ${assertGraphQLName(homeObjectField.name, 'application home object field')} {`);
  selection.push('          __typename');
  for (const possibleType of possibleTypes) {
    const typeName = assertGraphQLName(possibleType.name, 'application home possible type');
    selection.push(`          ... on ${typeName} {`);
    selection.push('            _meta { id logicalTypeName title }');
    selection.push('          }');
  }
  selection.push('        }');
  selection.push('      }');
  return selection;
}
