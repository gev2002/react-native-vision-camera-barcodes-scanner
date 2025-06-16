import React, { forwardRef } from 'react';
import {
  Camera as CameraVision,
  useFrameProcessor,
} from 'react-native-vision-camera';
import { useRunOnJS } from 'react-native-worklets-core';
import { useBarcodeScanner } from '../hooks/useBarcodeScanner';
import type {
  Barcode,
  CameraProps,
  Frame,
  ForwardedRef,
  ReadonlyFrameProcessor,
} from '../types';

export const Camera = forwardRef(function Camera(
  props: CameraProps,
  ref: ForwardedRef<any>
) {
  const { device, callback, options, ...p } = props;

  const { scanBarcodes } = useBarcodeScanner(options);

  const useWorklets = useRunOnJS(
    (data: Barcode[]): void => {
      callback(data);
    },
    [options]
  );

  const frameProcessor: ReadonlyFrameProcessor = useFrameProcessor(
    (frame: Frame) => {
      'worklet';
      const data: Barcode[] = scanBarcodes(frame);
      // eslint-disable-next-line react-hooks/rules-of-hooks
      useWorklets(data);
    },
    []
  );

  return (
    <>
      {!!device && (
        <CameraVision
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
