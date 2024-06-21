import {
  type FrameProcessorPlugin,
  VisionCameraProxy,
} from 'react-native-vision-camera';
import type {
  Frame,
  ScanBarcodeOptions,
  BarcodeScannerPlugin,
  BarcodeData,
} from './types';
import { Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-vision-camera-barcodes-scanner' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

export function createBarcodeScannerPlugin(
  options: ScanBarcodeOptions
): BarcodeScannerPlugin {
  const plugin: FrameProcessorPlugin | undefined =
    VisionCameraProxy.initFrameProcessorPlugin('scanBarcodes', {
      options,
    });
  if (!plugin) {
    throw new Error(LINKING_ERROR);
  }
  return {
    scanBarcodes: (frame: Frame): BarcodeData => {
      'worklet';
      return plugin.call(frame) as unknown as BarcodeData;
    },
  };
}
