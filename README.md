A plugin to scan barcodes using ML Kit Barcode Scanning

# 🚨 Required Modules

react-native-vision-camera => 4.5.2 <br />
react-native-worklets-core = 1.3.3

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
    Works With Android 🤖 and iOS .
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
          options={["qr","data_matrix"]}
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
  const options = ["qr", "data_matrix"]
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
##  Scan By Image 📸

```js
import { ImageScanner } from "react-native-vision-camera-barcodes-scanner";

const result = await ImageScanner({
    uri:assets.uri
})
console.log(result);

```

![Static Badge](https://img.shields.io/badge/-%25?style=&logo=typescript&color=rgba(0%2C0%2C0%2C0)) \
Type **`Options`** = aztec, code_128, code_39, code_93, codabar, ean_13,
ean_8, pdf_417, qr, upc_e, upc_a, itf, data_matrix, all

|  Name   |   Type   | Values  | Required | Default |
|:-------:|:--------:|:-------:|:--------:|:-------:|
|   uri   |  string  | String  |   yes    |    -    |
| options | string[] | Options |    no    |   all   |

---

## ⚙️ Options

|  Name   |   Type   | Values  | Default |
|:-------:|:--------:|:-------:|:-------:|
| options | string[] | Options |   all   |















