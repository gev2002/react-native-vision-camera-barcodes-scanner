import React, { forwardRef, useMemo } from 'react';
import {
  Camera as VisionCamera,
  useFrameProcessor,
} from 'react-native-vision-camera';
import { createBarcodeScannerPlugin } from './scanBarcodes';
import type {
  ReadonlyFrameProcessor,
  Frame,
  CameraTypes,
  ForwardedRef,
  BarcodeData,
  BarcodeScannerPlugin,
  ScanBarcodeOptions,
} from './types';
import { useRunOnJS } from 'react-native-worklets-core';

export const Camera = forwardRef(function Camera(
  props: CameraTypes,
  ref: ForwardedRef<any>
) {
  const { device, callback, options = {}, ...p } = props;
  // @ts-ignore
  const { scanBarcodes } = useBarcodeScanner(options);
  const useWorklets = useRunOnJS(
    (data: BarcodeData): void => {
      callback(data);
    },
    [options]
  );
  const frameProcessor: ReadonlyFrameProcessor = useFrameProcessor(
    (frame: Frame) => {
      'worklet';
      const data: BarcodeData = scanBarcodes(frame);
      // @ts-ignore
      // eslint-disable-next-line react-hooks/rules-of-hooks
      useWorklets(data);
    },
    []
  );
  return (
    <>
      {!!device && (
        <VisionCamera
          pixelFormat="yuv"
          ref={ref}
          frameProcessor={frameProcessor}
          device={device}
          {...p}
        />
      )}
    </>
  );
});

export function useBarcodeScanner(
  options?: ScanBarcodeOptions
): BarcodeScannerPlugin {
  return useMemo(
    () => createBarcodeScannerPlugin(options || ['all']),
    [options]
  );
}
