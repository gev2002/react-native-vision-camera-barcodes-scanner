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

export type CameraTypes = {
  callback: (data: Barcode[]) => void;
  options?: ScanBarcodeOptions;
} & CameraProps;

export type BarcodeScannerPlugin = {
  scanBarcodes: (frame: Frame) => Barcode[];
};
