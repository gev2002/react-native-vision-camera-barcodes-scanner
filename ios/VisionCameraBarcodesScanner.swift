import Foundation
import VisionCamera
import MLKitVision
import MLKitBarcodeScanning

@objc(VisionCameraBarcodesScanner)
public class VisionCameraBarcodesScanner: FrameProcessorPlugin {
    private var formats: [BarcodeFormat] = []
    private var barcodesOptions: BarcodeScannerOptions
    public override init(proxy: VisionCameraProxyHolder, options: [AnyHashable: Any]! = [:]) {
        barcodesOptions = BarcodeScannerOptions(formats: .all)
        super.init(proxy: proxy, options: options)
        options?.values.forEach { value in
            if let valueList = value as? [Any] {
                valueList.forEach { format in
                    if let formatString = format as? String {
                        if(formatString == "code_128"){
                            formats.append(.code128)
                        }
                        if(formatString == "code_39"){
                            formats.append(.code39)
                        }
                        if(formatString == "code_93"){
                            formats.append(.code93)
                        }
                        if(formatString == "codabar"){
                            formats.append(.codaBar)
                        }
                        if(formatString == "ean_13"){
                            formats.append(.EAN13)
                        }
                        if(formatString == "ean_8"){
                            formats.append(.EAN8)
                        }
                        if(formatString == "itf"){
                            formats.append(.ITF)
                        }
                        if(formatString == "upc_e"){
                            formats.append(.UPCE)
                        }
                        if(formatString == "upc_a"){
                            formats.append(.UPCA)
                        }
                        if(formatString == "qr"){
                            formats.append(.qrCode)
                        }
                        if(formatString == "pdf_417"){
                            formats.append(.PDF417)
                        }
                        if(formatString == "aztec"){
                            formats.append(.aztec)
                        }
                        if(formatString == "data_matrix"){
                            formats.append(.dataMatrix)
                        }
                        if(formatString == "all"){
                            formats.append(.all)
                        }
                    }
                }
            }
            let concatenatedFormats = BarcodeFormat(formats)
            barcodesOptions = BarcodeScannerOptions(formats: concatenatedFormats)
        }
    }

    public override func callback(
        _ frame: Frame,
        withArguments arguments: [AnyHashable: Any]?
    ) -> Any {
        var data:[Any] = []
        let buffer = frame.buffer
        let image = VisionImage(buffer: buffer);

        let barcodeScanner = BarcodeScanner.barcodeScanner(options: barcodesOptions)
        let dispatchGroup = DispatchGroup()
        dispatchGroup.enter()

        barcodeScanner.process(image) { barcodes, error in
            defer {
                dispatchGroup.leave()
            }
            guard error == nil, let barcodes = barcodes else { return }
            for barcode in barcodes {
                var objData : [String:Any] = [:]
                if(frame.orientation == .left) {
                    print("left");
                    // Rotate 90 degrees to the right
                    objData["height"] = barcode.frame.width
                    objData["width"] = barcode.frame.height
                    objData["left"] = CGFloat(frame.height) - barcode.frame.maxY
                    objData["right"] = CGFloat(frame.height) - barcode.frame.minY
                    objData["top"] = barcode.frame.minX
                    objData["bottom"] = barcode.frame.maxX
                } else if (frame.orientation == .right) {
                    // Rotate 90 degrees to the left
                    objData["height"] = barcode.frame.width
                    objData["width"] = barcode.frame.height
                    objData["left"] = barcode.frame.minY
                    objData["right"] = barcode.frame.maxY
                    objData["top"] = CGFloat(frame.width) - barcode.frame.maxX
                    objData["bottom"] = CGFloat(frame.width) - barcode.frame.minX
                } else if (frame.orientation == .up) {
                    // Do nothing
                    objData["width"] = barcode.frame.width
                    objData["height"] = barcode.frame.height
                    objData["left"] = barcode.frame.minX
                    objData["right"] = barcode.frame.maxX
                    objData["top"] = barcode.frame.minY
                    objData["bottom"] = barcode.frame.maxY
                } else if (frame.orientation == .down) {
                    // Reverse everything
                    objData["width"] = barcode.frame.width
                    objData["height"] = barcode.frame.height
                    objData["left"] = CGFloat(frame.width) - barcode.frame.maxX
                    objData["right"] = CGFloat(frame.width) - barcode.frame.minX
                    objData["top"] = CGFloat(frame.height) - barcode.frame.maxY
                    objData["bottom"] = CGFloat(frame.height) - barcode.frame.minY
                }

                let displayValue = barcode.displayValue
                objData["displayValue"] = displayValue
                let rawValue = barcode.rawValue
                objData["rawValue"] = rawValue

                let valueType = barcode.valueType
                switch valueType {
                case .wiFi:
                    let ssid = barcode.wifi?.ssid
                    objData["ssid"] = ssid
                    let password = barcode.wifi?.password
                    objData["password"] = password
                    let encryptionType = barcode.wifi?.type
                    objData["encryptionType"] = encryptionType
                case .URL:
                    let title = barcode.url!.title
                    objData["title"] = title
                    let url = barcode.url!.url
                    objData["url"] = url
                default:
                    print("value case")

                }
                data.append(objData)
            }
        }
        dispatchGroup.wait()
        return data
    }
}
