import Foundation
import React
import MLKitBarcodeScanning
import MLKitVision

@objc(ImageScanner)
class ImageScanner: NSObject {
    private var scanner: BarcodeScanner = BarcodeScanner.barcodeScanner()
    private var scannerBuilder: BarcodeScannerOptions = BarcodeScannerOptions(formats: .all)

    @objc(process:options:withResolver:withRejecter:)
    private func process(
        uri: String,
        options: [AnyHashable: Any]! = [:],
        resolve: @escaping RCTPromiseResolveBlock,
        reject: @escaping RCTPromiseRejectBlock
    ) {
        let scannerBarcodeFormats = ScannerUtils.getOptionsBarcodeFormats(options: options)
        let scannerRatio = ScannerUtils.getOptionsRatio(options: options)
        let scannerOrientation = ScannerUtils.getOptionsOrientation(options: options)
        let scannerViewSize = ScannerUtils.getOptionsViewSize(options: options)

        let barcodeFormats = ScannerUtils.getSafeBarcodeFormats(formats: scannerBarcodeFormats)
        if barcodeFormats.contains(.all) {
            scannerBuilder = BarcodeScannerOptions(formats: .all)
        } else {
            scannerBuilder = BarcodeScannerOptions(formats: BarcodeFormat(barcodeFormats))
        }

        scanner = BarcodeScanner.barcodeScanner(options: scannerBuilder)

        var array: [Any] = []

        guard let imageUI = UIImage(contentsOfFile: uri) else {
            reject("Error", "Can't find photo.", nil)
            return
        }

        guard let imageCG = imageUI.cgImage else {
            reject("Error", "Couldn't get CGImage from UIImage.", nil)
            return
        }

        let image = VisionImage(image: imageUI)
        image.orientation = imageUI.imageOrientation
        let imageSize = Size(width: imageCG.width, height: imageCG.height)
        let viewSize = ScannerUtils.getSafeViewSize(imageSize: imageSize, viewSize: scannerViewSize)

        do {
            let barcodes = try scanner.results(in: image)
            let barcodesFiltered: [Barcode]
            if scannerRatio.width != 1.0 || scannerRatio.height != 1.0 {
                barcodesFiltered = ScannerUtils.filterBarcodes(
                    barcodes: barcodes,
                    imageSize: imageSize,
                    viewSize: viewSize,
                    ratio: scannerRatio,
                    imageRotation: image.orientation
                )
            } else {
                barcodesFiltered = barcodes
            }

            for barcode in barcodesFiltered {
                let map = ScannerUtils.formatBarcode(
                    barcode: barcode,
                    imageSize: imageSize,
                    viewSize: viewSize,
                    orientation: scannerOrientation,
                    imageRotation: image.orientation
                )
                array.append(map)
            }
            resolve(array)
        } catch {
            reject("Error", "Image processing failed.", nil)
        }
    }
}
