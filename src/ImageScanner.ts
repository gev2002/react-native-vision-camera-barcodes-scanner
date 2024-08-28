import type { Barcode, ScanBarcodeOptions } from './types';
import { NativeModules, Platform } from 'react-native';

export async function ImageScanner(
  uri: String,
  options?: ScanBarcodeOptions
): Promise<Barcode[]> {
  const { ImageScanner } = NativeModules;
  if (!uri) {
    throw Error("Can't resolve img uri");
  }
  if (Platform.OS === 'ios') {
    return await ImageScanner.process(uri.replace('file://', ''), options);
  } else {
    return await ImageScanner.process(uri, options);
  }
}
