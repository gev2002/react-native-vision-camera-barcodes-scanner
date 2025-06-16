import type {
  CameraProps as CameraVisionProps,
  Frame,
  Orientation,
} from 'react-native-vision-camera';

export type {
  Frame,
  FrameProcessorPlugin,
  Orientation,
  ReadonlyFrameProcessor,
} from 'react-native-vision-camera';

export type { ForwardedRef } from 'react';

export type CameraProps = {
  callback: (data: Barcode[]) => void;
  options?: ScannerOptions;
} & CameraVisionProps;

export type ScannerPlugin = {
  scanBarcodes: (frame: Frame) => Barcode[];
};

export type ScannerOptions = {
  /**
   * If not given, it will read all barcodes.
   * Native side default: all
   */
  formats?: Array<keyof BarcodeType>;
  /**
   * If you want to limit the area that can be scanned by the camera.
   * Must be used with viewSize for Pixel Perfect operation.
   * Native side default: { width: 1.0, height: 1.0 }
   */
  ratio?: Ratio;
  /**
   * If not given, it is always in portrait mode.
   * Provide this value if you have an application that switches between orientations.
   * Native side default: portrait
   */
  orientation?: Orientation;
  /**
   * If you provide this value, the barcode coordinate corners will work Pixel Perfect.
   * This value should be the width and height of the camera component.
   * Native side default: null
   */
  viewSize?: Size;
};

export type BarcodeType = Readonly<{
  aztec: string;
  code_128: string;
  code_39: string;
  code_93: string;
  codabar: string;
  ean_13: string;
  ean_8: string;
  pdf_417: string;
  qr: string;
  upc_e: string;
  upc_a: string;
  itf: string;
  data_matrix: string;
  all: string;
}>;

export type Barcode = {
  rawValue: string;
  width: number;
  height: number;
  left: number;
  top: number;
  right: number;
  bottom: number;
  leftRatio: number;
  topRatio: number;
  widthRatio: number;
  heightRatio: number;
};

export type Ratio = {
  width?: number;
  height?: number;
};

export type Size = {
  width: number;
  height: number;
};
