A plugin to scan barcodes using ML Kit Barcode Scanning

# 🚨 Required Modules

react-native-vision-camera => 4.0.0 <br />
react-native-worklets-core = 1.3.0

## 💻 Installation

```sh
npm install react-native-vision-camera-barcodes-scanner
yarn add react-native-vision-camera-barcodes-scanner
```
## 👷Features
    Easy To Use.
    Works Just Writing few lines of Code.
    Works With React Native Vision Camera.
    Works for Both Cameras.
    Works Fast.
    Works With Android 🤖 and IOS.📱
    Writen With Kotlin and Swift.

## 💡 Usage

```js
import React, { useState } from 'react'
import { useCameraDevice } from 'react-native-vision-camera'
import { Camera } from 'react-native-vision-camera-barcodes-scanner';

function App (){
  const [data,setData] = useState(null)
  const device = useCameraDevice('back');
  console.log(data)
  return(
    <>
      {!!device && (
        <Camera
          style={StyleSheet.absoluteFill}
          device={device}
          isActive
          // optional
          options={["qr"]}
          callback={(d) => setData(d)}
        />
      )}
    </>
  )
}

```
### Also You Can Use Like This

```js
import React from 'react';
import { StyleSheet } from "react-native";
import {
  Camera,
  useCameraDevice,
  useFrameProcessor,
} from "react-native-vision-camera";
import { useBarcodeScanner } from "react-native-vision-camera-barcodes-scanner";

function App() {
  const device = useCameraDevice('back');
  const options = ["qr"]
  const {scanBarcodes} = useBarcodeScanner(options)
  const frameProcessor = useFrameProcessor((frame) => {
    'worklet'
    const data = scanBarcodes(frame)
	console.log(data, 'data')
  }, [])
  return (
      <>
      {!!device && (
        <Camera
          style={StyleSheet.absoluteFill}
          device={device}
          isActive
          frameProcessor={frameProcessor}
        />
      )}
      </>
  );
}
export default App;
```
---

## ⚙️ Options

| Name |  Type    |  Values  | Default |
| :---:   | :---: | :---: |  :---: |
| codeType | String  | all, code-39, code-93, codabar, ean-13, ean-8, itf, upc-e, upc-a, qr, pdf-417, aztec, data-matrix, code-128 | all |















