import { useMemo } from 'react';
import { createPlugin } from '../utils/createPlugin';
import type { ScannerOptions, ScannerPlugin } from '../types';

export function useBarcodeScanner(options?: ScannerOptions): ScannerPlugin {
  return useMemo(() => createPlugin(options), [options]);
}
