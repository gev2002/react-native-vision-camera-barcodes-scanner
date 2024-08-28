import Foundation
import React
import MLKitBarcodeScanning
import MLKitVision

@objc(ImageScanner)
class ImageScanner: NSObject {
    
    private var formats: [BarcodeFormat] = []
    
    @objc(process:options:withResolver:withRejecter:)
    private func process(uri: String,options:NSArray?, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock){
       var barcodesOptions = BarcodeScannerOptions(formats: .all)

        if(options != nil){
            options!.forEach { value in
                if(value as? String == "code_128"){ formats.append(.code128) }
                if(value as? String == "code_39"){ formats.append(.code39) }
                if(value as? String == "code_93"){ formats.append(.code93) }
                if(value as? String == "codabar"){ formats.append(.codaBar) }
                if(value as? String == "ean_13"){ formats.append(.EAN13) }
                if(value as? String == "ean_8"){ formats.append(.EAN8) }
                if(value as? String == "itf"){ formats.append(.ITF) }
                if(value as? String == "upc_e"){ formats.append(.UPCE) }
                if(value as? String == "upc_a"){ formats.append(.UPCA) }
                if(value as? String == "qr"){ formats.append(.qrCode) }
                if(value as? String == "pdf_417"){ formats.append(.PDF417) }
                if(value as? String == "aztec"){ formats.append(.aztec)}
                if(value as? String == "data_matrix"){ formats.append(.dataMatrix) }
                if(value as? String == "all"){ formats.append(.all)} }
            let concatenatedFormats = BarcodeFormat(formats)
            barcodesOptions = BarcodeScannerOptions(formats: concatenatedFormats)
        }
        
        let barcodeScanner = BarcodeScanner.barcodeScanner(options: barcodesOptions)

        let image =  UIImage(contentsOfFile: uri)
        var data:[Any] = []
        if image != nil {
            do {
                let visionImage = VisionImage(image: image!)
                let result = try barcodeScanner.results(in: visionImage)
                for barcode in result {
                    let objData = VisionCameraBarcodesScanner.processData(barcode: barcode)
                    data.append(objData)
                }
                resolve(data)
                
            }catch{
                reject("Error","Processing Image",nil)
            }
        }else{
            reject("Error","Can't Find Photo",nil)
        }
        
    }
}

