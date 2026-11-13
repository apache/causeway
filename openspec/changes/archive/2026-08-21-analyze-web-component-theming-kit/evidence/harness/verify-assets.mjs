/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */

import {createHash} from 'node:crypto';
import {mkdirSync, readFileSync, writeFileSync} from 'node:fs';
import {dirname, resolve} from 'node:path';
import {fileURLToPath} from 'node:url';

const directory = fileURLToPath(new URL('.', import.meta.url));
const lock = JSON.parse(readFileSync(resolve(directory, 'package-lock.json'), 'utf8'));
const expected = Object.freeze({
  'node_modules/bootstrap': {version: '5.3.8', license: 'MIT', licenseFile: 'LICENSE'},
  'node_modules/@awesome.me/webawesome': {version: '3.11.0', license: 'MIT', licenseFile: 'LICENSE.md'},
  'node_modules/open-props': {version: '1.7.23', license: 'MIT', licenseFile: 'LICENSE'},
  'node_modules/esbuild': {version: '0.28.2', license: 'MIT', licenseFile: 'LICENSE.md'},
  'node_modules/playwright': {version: '1.61.0', license: 'Apache-2.0', licenseFile: 'LICENSE'}
});

const packages = [];
for (const [lockPath, expectation] of Object.entries(expected)) {
  const entry = lock.packages?.[lockPath];
  if (!entry) throw new Error(`Missing lock entry ${lockPath}`);
  const packageDirectory = resolve(directory, lockPath);
  const metadata = JSON.parse(readFileSync(resolve(packageDirectory, 'package.json'), 'utf8'));
  if (entry.version !== expectation.version || metadata.version !== expectation.version) {
    throw new Error(`${lockPath} version mismatch: lock=${entry.version}, package=${metadata.version}, expected=${expectation.version}`);
  }
  if (metadata.license !== expectation.license) {
    throw new Error(`${lockPath} license mismatch: ${metadata.license} != ${expectation.license}`);
  }
  const licenseBytes = readFileSync(resolve(packageDirectory, expectation.licenseFile));
  packages.push({
    package: metadata.name,
    version: metadata.version,
    license: metadata.license,
    resolved: entry.resolved,
    integrity: entry.integrity,
    directDependencies: Object.keys(metadata.dependencies ?? {}).sort(),
    licenseFile: expectation.licenseFile,
    licenseSha256: sha256(licenseBytes)
  });
}

const selectedAssets = [
  'node_modules/bootstrap/dist/css/bootstrap.min.css',
  'node_modules/bootstrap/dist/js/bootstrap.bundle.min.js',
  'node_modules/@awesome.me/webawesome/dist-cdn/styles/webawesome.css',
  'node_modules/@awesome.me/webawesome/dist-cdn/webawesome.loader.js',
  'node_modules/open-props/open-props.min.css',
  'node_modules/open-props/normalize.min.css',
  'node_modules/open-props/buttons.min.css'
].map(relativePath => {
  const bytes = readFileSync(resolve(directory, relativePath));
  return {path: relativePath, bytes: bytes.length, sha256: sha256(bytes)};
});

const result = {
  generatedAt: new Date().toISOString(),
  command: 'npm ci --ignore-scripts --no-audit --no-fund && npm run verify-assets -- --write',
  packages,
  selectedAssets
};
console.log(JSON.stringify(result, null, 2));
if (process.argv.includes('--write')) {
  const target = resolve(directory, '../results/asset-verification.json');
  mkdirSync(dirname(target), {recursive: true});
  writeFileSync(target, `${JSON.stringify(result, null, 2)}\n`);
}

function sha256(bytes) {
  return createHash('sha256').update(bytes).digest('hex');
}
