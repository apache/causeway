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

export const DescriptionPresentation = Object.freeze({
  LABEL: 'label',
  TOOLTIP: 'tooltip'
});

export function normalizeDescriptionPresentation(value) {
  return String(value ?? '').trim().toLocaleLowerCase() === DescriptionPresentation.TOOLTIP
    ? DescriptionPresentation.TOOLTIP
    : DescriptionPresentation.LABEL;
}

export function boundedTooltipSection(value, maximum = 240) {
  const text = String(value ?? '').trim();
  return text.length > maximum ? `${text.slice(0, maximum - 1)}…` : text;
}

export function composeMemberTooltip(description, disabledReason) {
  return [boundedTooltipSection(description), boundedTooltipSection(disabledReason)]
    .filter(Boolean)
    .join('\n\n');
}
