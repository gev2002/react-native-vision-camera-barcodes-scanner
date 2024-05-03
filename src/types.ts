import type { CameraProps } from 'react-native-vision-camera';
export type {
  Frame,
  ReadonlyFrameProcessor,
  FrameProcessorPlugin,
} from 'react-native-vision-camera';
import type { Frame } from 'react-native-vision-camera';
export type { ForwardedRef } from 'react';

type BarCodeType = Readonly<{
  aztec: string;
  code128: string;
  code39: string;
  code39mod43: string;
  code93: string;
  ean13: string;
  ean8: string;
  pdf417: string;
  qr: string;
  upc_e: string;
  interleaved2of5: string;
  itf14: string;
  datamatrix: string;
  all: string;
}>;

export type ScanBarcodeOptions = Array<keyof BarCodeType>;

export type Barcode = {
  bottom: number;
  height: number;
  left: number;
  rawValue: string;
  right: number;
  top: number;
  width: number;
};

export type BarcodeData = {
  [key: number]: Barcode;
};

export type CameraTypes = {
  callback: (data: BarcodeData) => void;
  options: ScanBarcodeOptions;
} & CameraProps;

export type BarcodeScannerPlugin = {
  scanBarcodes: (frame: Frame) => Barcode;
};
