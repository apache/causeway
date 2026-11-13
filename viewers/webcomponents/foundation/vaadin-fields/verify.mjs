/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */

import {createHash} from 'node:crypto';
import {readFileSync, readdirSync} from 'node:fs';
import {resolve} from 'node:path';
import {fileURLToPath} from 'node:url';
import {gzipSync} from 'node:zlib';

const directory = fileURLToPath(new URL('.', import.meta.url));
const policy = json('policy.json');
const manifest = json('package.json');
const lock = json('package-lock.json');
const metadata = json('generated/licenses/THIRD-PARTY.json');
const forbidden = [/vaadin-usage-statistics-collect/, /tools\.vaadin\.com\/usage-stats/, /Vaadin\.Flow\.clients/, /@vaadin\/grid/, /@vaadin\/upload/, /vaadin-pro/i];
const htmxController = readFileSync(resolve(directory, '../../htmx/src/main/java/org/apache/causeway/viewer/webcomponents/htmx/HtmxViewerController.java'), 'utf8');
let aggregateGzipBytes = 0;

for (const [family, familyPolicy] of Object.entries(policy.families)) {
  const bundle = readFileSync(resolve(directory, 'generated/assets', `vaadin-${family}.js`));
  const sha256 = createHash('sha256').update(bundle).digest('hex');
  const gzipBytes = gzipSync(bundle, {level: 9}).length;
  aggregateGzipBytes += gzipBytes;
  if (sha256 !== familyPolicy.bundleSha256) throw new Error(`${family} checksum differs from policy.`);
  if (gzipBytes > familyPolicy.maximumGzipBytes) throw new Error(`${family} exceeds its gzip budget.`);
  const generatedPolicy = json(`generated/assets/csp-policy-${family}.json`);
  if (JSON.stringify(generatedPolicy.styleHashes) !== JSON.stringify(familyPolicy.cspStyleHashes)) {
    throw new Error(`${family} generated CSP hashes differ from policy.`);
  }
  const source = bundle.toString('utf8');
  for (const pattern of forbidden) {
    if (pattern.test(source)) throw new Error(`${family} bundle contains forbidden pattern ${pattern}.`);
  }
  if (/\b(?:import|export)\s*(?:\([^)]*)?\s*['\"]https?:\/\//.test(source)) {
    throw new Error(`${family} contains an external runtime module reference.`);
  }
  if (JSON.stringify(metadata.families[family].entryPoints) !== JSON.stringify(familyPolicy.entryPoints)) {
    throw new Error(`${family} entry points differ from policy.`);
  }
  for (const hash of familyPolicy.cspStyleHashes) {
    if (!htmxController.includes(`\"${hash}\"`)) throw new Error(`${family} CSP hash is absent from HTMX policy.`);
  }
}
if (aggregateGzipBytes > policy.aggregateMaximumGzipBytes || aggregateGzipBytes !== metadata.aggregateGzipBytes) {
  throw new Error('Aggregate gzip policy differs from generated metadata.');
}
for (const [name, version] of Object.entries(manifest.dependencies)) {
  if (lock.packages?.[`node_modules/${name}`]?.version !== version) throw new Error(`${name} is not pinned consistently.`);
}
const legalFiles = readdirSync(resolve(directory, 'generated/licenses')).filter(file => file !== 'THIRD-PARTY.json');
for (const pkg of metadata.packages) {
  if (!pkg.license || !pkg.integrity || !legalFiles.some(file => file.startsWith(sanitize(pkg.name) + '-'))) {
    throw new Error(`Incomplete package policy for ${pkg.name}.`);
  }
}
console.log(JSON.stringify({families: Object.keys(policy.families), aggregateGzipBytes, packages: metadata.packages.length}, null, 2));

function json(path) {
  return JSON.parse(readFileSync(resolve(directory, path), 'utf8'));
}

function sanitize(value) {
  return value.replace(/^@/, '').replaceAll('/', '__');
}
