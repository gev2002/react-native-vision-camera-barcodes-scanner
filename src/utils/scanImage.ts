import { NativeModules, Platform } from 'react-native';
import type { Barcode, ScannerOptions } from '../types';

export async function scanImage(
  uri: String,
  options?: ScannerOptions
): Promise<Barcode[]> {
  const { ImageScanner } = NativeModules;

  const optionsSafe = options || ({} as ScannerOptions);

  if (!uri) {
    throw Error("Can't resolve img uri");
  }

  if (Platform.OS === 'ios') {
    return await ImageScanner.process(uri.replace('file://', ''), optionsSafe);
  } else {
    return await ImageScanner.process(uri, optionsSafe);
  }
}

/**
 * @deprecated
 * @description Use scanImage() instead.
 */
export const ImageScanner = scanImage;
