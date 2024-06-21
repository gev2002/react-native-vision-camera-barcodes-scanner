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
          image.orientation = getOrientation(orientation: frame.orientation)
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
                      objData["height"] = barcode.frame.height
                      objData["width"] = barcode.frame.width
                      objData["top"] = barcode.frame.minY
                      objData["bottom"] = barcode.frame.maxY
                      objData["left"] = barcode.frame.minX
                      objData["right"] = barcode.frame.maxX
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
                          break

                      }
                  data.append(objData)
              }
          }
          dispatchGroup.wait()
          return data
      }

      private func getOrientation(
          orientation: UIImage.Orientation
        ) -> UIImage.Orientation {
          switch orientation {
            case .right, .leftMirrored:
              return .up
            case .left, .rightMirrored:
              return .down
            case .up, .downMirrored:
              return .left
            case .down, .upMirrored:
              return .right
          default:
              return .up
          }
      }
  }
