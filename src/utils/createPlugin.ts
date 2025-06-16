import { VisionCameraProxy } from 'react-native-vision-camera';
import type { Barcode, Frame, ScannerOptions, ScannerPlugin } from '../types';

const LINKING_ERROR = `Can't load plugin scanBarcodes.Try cleaning cache or reinstall plugin.`;

export function createPlugin(options?: ScannerOptions): ScannerPlugin {
  const optionsSafe = options || ({} as ScannerOptions);

  const plugin = VisionCameraProxy.initFrameProcessorPlugin(
    'scanBarcodes',
    optionsSafe
  );
  if (!plugin) {
    throw new Error(LINKING_ERROR);
  }

  return {
    scanBarcodes: (frame: Frame): Barcode[] => {
      'worklet';
      // @ts-ignore
      return plugin.call(frame) as Barcode[];
    },
  };
}
