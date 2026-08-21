/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */

import {createHash} from 'node:crypto';
import {existsSync, mkdirSync, readFileSync, readdirSync, statSync, writeFileSync} from 'node:fs';
import {dirname, resolve} from 'node:path';
import {fileURLToPath} from 'node:url';

const directory = fileURLToPath(new URL('.', import.meta.url));
const lock = JSON.parse(readFileSync(resolve(directory, 'package-lock.json'), 'utf8'));
const direct = {...lock.packages[''].dependencies};
const expectedVersion = '25.2.8';
const packages = [];

for (const [key, entry] of Object.entries(lock.packages)) {
  if (!key.startsWith('node_modules/')) continue;
  const packageDirectory = resolve(directory, key);
  const metadataPath = resolve(packageDirectory, 'package.json');
  const metadata = existsSync(metadataPath) ? JSON.parse(readFileSync(metadataPath, 'utf8')) : {};
  packages.push({
    name: metadata.name ?? key.slice('node_modules/'.length),
    version: metadata.version ?? entry.version,
    license: metadata.license ?? entry.license ?? null,
    integrity: entry.integrity ?? null,
    dev: entry.dev === true,
    repository: metadata.repository ?? null,
    installed: existsSync(metadataPath),
    direct: Object.hasOwn(direct, metadata.name ?? key.slice('node_modules/'.length))
  });
}

const failures = [];
for (const [name, version] of Object.entries(direct)) {
  const found = packages.find(item => item.name === name && item.direct);
  if (!found) failures.push(`Missing direct package ${name}`);
  if (name.startsWith('@vaadin/') && (version !== expectedVersion || found?.version !== expectedVersion)) failures.push(`${name} is not pinned to ${expectedVersion}`);
  if (name.startsWith('@vaadin/') && found?.license !== 'Apache-2.0') failures.push(`${name} license is ${found?.license}`);
}
for (const item of packages.filter(item => !item.dev && item.name.startsWith('@vaadin/'))) {
  if (item.license !== 'Apache-2.0') failures.push(`${item.name}@${item.version} runtime license is ${item.license}`);
}
for (const prohibited of ['@vaadin/grid-pro', '@vaadin/rich-text-editor', '@vaadin/charts', '@vaadin/spreadsheet']) {
  if (packages.some(item => item.name === prohibited)) failures.push(`Commercial package present: ${prohibited}`);
}

const generatedFiles = walk(resolve(directory, 'generated')).filter(path => !path.endsWith('split-metafile.json')).map(path => ({
  path: path.slice(resolve(directory).length + 1),
  bytes: statSync(path).size,
  sha256: sha256(readFileSync(path))
}));
const licenseCounts = Object.groupBy(packages, item => `${item.dev ? 'development' : 'runtime'}:${item.license ?? 'UNKNOWN'}`);
const result = {
  generatedAt: new Date().toISOString(),
  lockfileVersion: lock.lockfileVersion,
  directPackages: packages.filter(item => item.direct),
  packageCount: packages.length,
  runtimePackageCount: packages.filter(item => !item.dev).length,
  licenseCounts: Object.fromEntries(Object.entries(licenseCounts).map(([name, rows]) => [name, rows.length])),
  unknownLicenses: packages.filter(item => !item.license),
  prohibitedPackages: packages.filter(item => ['@vaadin/grid-pro', '@vaadin/rich-text-editor', '@vaadin/charts', '@vaadin/spreadsheet'].includes(item.name)),
  generatedFiles,
  externalRuntimeDependencyPolicy: 'All resolved @vaadin runtime packages must be Apache-2.0; complete ASF dependency review remains required before adoption.',
  failures
};

if (process.argv.includes('--write')) {
  const output = resolve(directory, '..', 'results', 'asset-verification.json');
  mkdirSync(dirname(output), {recursive: true});
  writeFileSync(output, `${JSON.stringify(result, null, 2)}\n`);
}
console.log(JSON.stringify({packageCount: result.packageCount, runtimePackageCount: result.runtimePackageCount, licenseCounts: result.licenseCounts, generatedFiles: generatedFiles.length, failures}, null, 2));
if (failures.length) process.exitCode = 1;

function walk(root) {
  const result = [];
  for (const entry of readdirSync(root, {withFileTypes: true})) {
    const path = resolve(root, entry.name);
    if (entry.isDirectory()) result.push(...walk(path));
    else result.push(path);
  }
  return result;
}

function sha256(bytes) { return createHash('sha256').update(bytes).digest('hex'); }
