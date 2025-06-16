import Foundation
import MLKitBarcodeScanning

struct Size {
    let width: Int
    let height: Int
}

struct Ratio {
    let width: CGFloat
    let height: CGFloat
}

struct BoxRatio {
    let leftRatio: CGFloat
    let topRatio: CGFloat
    let widthRatio: CGFloat
    let heightRatio: CGFloat

    init(_ leftRatio: CGFloat, _ topRatio: CGFloat, _ widthRatio: CGFloat, _ heightRatio: CGFloat) {
        self.leftRatio = leftRatio
        self.topRatio = topRatio
        self.widthRatio = widthRatio
        self.heightRatio = heightRatio
    }
}

struct Scaling {
    let scale: CGFloat
    let dx: CGFloat
    let dy: CGFloat

    func destructured() -> (CGFloat, CGFloat, CGFloat) {
        return (scale, dx, dy)
    }
}

struct BoundingBox {
    var width: CGFloat
    var height: CGFloat
    var left: CGFloat
    var top: CGFloat
    var right: CGFloat
    var bottom: CGFloat
}

class ScannerUtils {
    static func getOptionsRatio(options: [AnyHashable: Any]?) -> Ratio {
        guard
            let ratio = options?["ratio"] as? [String: Any]
        else {
            return Ratio(width: 1.0, height: 1.0)
        }

        let width = ratio["width"] as? CGFloat ?? 1.0
        let height = ratio["height"] as? CGFloat ?? 1.0
        return Ratio(
            width: min(max(width, 0.0), 1.0),
            height: min(max(height, 0.0), 1.0)
        )
    }

    static func getOptionsOrientation(options: [AnyHashable: Any]?) -> UIDeviceOrientation {
        guard
            let orientation = options?["orientation"] as? String
        else {
            return .portrait
        }

        switch orientation {
            case "portrait": return .portrait
            case "landscape-left": return .landscapeLeft
            case "portrait-upside-down": return .portraitUpsideDown
            case "landscape-right": return .landscapeRight
            default: return .portrait
        }
    }

    static func getOptionsViewSize(options: [AnyHashable: Any]?) -> Size? {
        guard let viewSize = options?["viewSize"] as? [String: Any],
              let width = viewSize["width"] as? NSNumber,
              let height = viewSize["height"] as? NSNumber else {
            return nil
        }
        return Size(width: width.intValue, height: height.intValue)
    }

    static func getOptionsBarcodeFormats(options: [AnyHashable: Any]?) -> [Any] {
        if let formats = options?["formats"] as? [Any], !formats.isEmpty {
            return formats
        } else {
            return ["all"]
        }
    }

    static func getSafeBarcodeFormats(formats: [Any]) -> [BarcodeFormat] {
        return formats.compactMap { format -> BarcodeFormat in
            if let format = format as? String {
                switch format {
                    case "code_128": return .code128
                    case "code_39": return .code39
                    case "code_93": return .code93
                    case "codabar": return .codaBar
                    case "ean_13": return .EAN13
                    case "ean_8": return .EAN8
                    case "itf": return .ITF
                    case "upc_e": return .UPCE
                    case "upc_a": return .UPCA
                    case "qr": return .qrCode
                    case "pdf_417": return .PDF417
                    case "aztec": return .aztec
                    case "data_matrix": return .dataMatrix
                    case "all": return .all
                    default: return .all
                }
            } else {
                return .all
            }
        }
    }

    // ViewSize is used for scaling in the code.
    // The UI side should always be assumed as portrait.
    static func getSafeViewSize(imageSize: Size, viewSize: Size?) -> Size {
        if let viewSize = viewSize {
            let width = min(viewSize.width, viewSize.height)
            let height = max(viewSize.width, viewSize.height)
            return Size(width: width, height: height)
        } else {
            return imageSize
        }
    }

    static func getImageRotation(imageRotation: UIImage.Orientation) -> UIImage.Orientation {
        switch imageRotation {
            case .up: return .up
            case .left: return .right
            case .down: return .down
            case .right: return .left
            default: return .up
        }
    }

    static func getImageSizeBasedOnRotation(imageSizeRaw: Size, imageRotation: UIImage.Orientation) -> Size {
        switch imageRotation {
            case .left, .leftMirrored, .right, .rightMirrored:
                return Size(width: imageSizeRaw.height, height: imageSizeRaw.width)
            default:
                return imageSizeRaw
        }
    }

    static private func getImageScalingBasedOnViewSize(imageSize: Size, viewSize: Size) -> Scaling {
        let imageWidth = CGFloat(imageSize.width)
        let imageHeight = CGFloat(imageSize.height)
        let imageAspect = imageWidth / imageHeight

        let viewWidth = CGFloat(viewSize.width)
        let viewHeight = CGFloat(viewSize.height)
        let viewAspect = viewWidth / viewHeight

        let scale: CGFloat
        let scaledImageWidth: CGFloat
        let scaledImageHeight: CGFloat
        let dx: CGFloat
        let dy: CGFloat
        if imageAspect > viewAspect {
            // Image is wider -> crop left/right
            scale = viewHeight / imageHeight
            scaledImageWidth = imageWidth * scale
            scaledImageHeight = viewHeight
            dx = (scaledImageWidth - viewWidth) / 2.0
            dy = 0.0
        } else if viewAspect > imageAspect {
            // Image is taller → crop top/bottom
            scale = viewWidth / imageWidth
            scaledImageWidth = viewWidth
            scaledImageHeight = imageHeight * scale
            dx = 0.0
            dy = (scaledImageHeight - viewHeight) / 2.0
        } else {
            // Full compatibility -> crop nothing
            // Default if user does not provide viewSize value
            scale = 1.0
            scaledImageWidth = viewWidth
            scaledImageHeight = viewHeight
            dx = 0.0
            dy = 0.0
        }

        return Scaling(scale: scale, dx: dx, dy: dy)
    }


    // This transformation is necessary.
    // Because the default image always comes as landscapeRight.
    // On the Swift side, MLKit does not produce coordinates for a different orientation.
    // This step is done automatically on the Kotlin side.
    static func getRectBasedOnRotation(
        rect: CGRect,
        imageSize: Size,
        imageRotation: UIImage.Orientation
    ) -> CGRect {
        // By default, the camera captures data in Landscape mode.
        // Here it should always be width > height.
        // imageSize to imageRawSize conversion.
        let imageWidth = CGFloat(max(imageSize.width, imageSize.height))
        let imageHeight = CGFloat(min(imageSize.width, imageSize.height))

        switch imageRotation {
        case .up:
            return rect
        case .left:
            return CGRect(
                x: rect.minY,
                y: imageWidth - rect.maxX,
                width: rect.height,
                height: rect.width
            )
        case .right:
            return CGRect(
                x: imageHeight - rect.maxY,
                y: rect.minX,
                width: rect.height,
                height: rect.width
            )
        case .down:
            return CGRect(
                x: imageWidth - rect.maxX,
                y: imageHeight - rect.maxY,
                width: rect.width,
                height: rect.height
            )
        default:
            return rect
        }
    }

    static func getBoundingBoxFromRect(rect: CGRect) -> BoundingBox {
        return BoundingBox(
            width: rect.width,
            height: rect.height,
            left: rect.minX,
            top: rect.minY,
            right: rect.maxX,
            bottom: rect.maxY
        )
    }

    static func getBoxRatioBasedOnOrientationForUI(
        boxScaled: BoundingBox,
        viewSize: Size,
        orientation: UIDeviceOrientation
    ) -> BoxRatio {
        let viewWidth = CGFloat(viewSize.width)
        let viewHeight = CGFloat(viewSize.height)

        // Normalize to viewSize
        let leftRatio = min(max(boxScaled.left / viewWidth, 0.0), 1.0)
        let topRatio = min(max(boxScaled.top / viewHeight, 0.0), 1.0)
        let widthRatio = min(max(boxScaled.width / viewWidth, 0.0), 1.0 - leftRatio)
        let heightRatio = min(max(boxScaled.height / viewHeight, 0.0), 1.0 - topRatio)

        switch orientation {
        case .portrait:
            return BoxRatio(leftRatio, topRatio, widthRatio, heightRatio)
        case .landscapeRight:
            return BoxRatio(
                topRatio,
                1 - leftRatio - widthRatio,
                heightRatio,
                widthRatio
            )
        case .portraitUpsideDown:
            return BoxRatio(
                1 - leftRatio - widthRatio,
                1 - topRatio - heightRatio,
                widthRatio,
                heightRatio
            )
        case .landscapeLeft:
            return BoxRatio(
                1 - topRatio - heightRatio,
                leftRatio,
                heightRatio,
                widthRatio
            )
        default:
            return BoxRatio(leftRatio, topRatio, widthRatio, heightRatio)
        }
    }

    static func scaleBoxBasedOnViewSize(box: BoundingBox, imageSize: Size, viewSize: Size) -> BoundingBox {
        let imageScaling = getImageScalingBasedOnViewSize(imageSize: imageSize, viewSize: viewSize)
        let (scale, dx, dy) = imageScaling.destructured()

        let left = CGFloat(box.left) * scale - dx
        let top = CGFloat(box.top) * scale - dy
        let right = CGFloat(box.right) * scale - dx
        let bottom = CGFloat(box.bottom) * scale - dy
        let width = right - left
        let height = bottom - top

        return BoundingBox(
            width: width,
            height: height,
            left: left,
            top: top,
            right: right,
            bottom: bottom
        )
    }

    static func filterBarcodes(
        barcodes: [Barcode],
        imageSize: Size,
        viewSize: Size,
        ratio: Ratio,
        imageRotation: UIImage.Orientation
    ) -> [Barcode] {
        let viewWidth = CGFloat(viewSize.width)
        let viewHeight = CGFloat(viewSize.height)

        let scanViewWidth = ratio.width * viewWidth
        let scanViewHeight = ratio.height * viewHeight

        // Calculate the current scan area for viewSize.
        let scanLeft = (viewWidth - scanViewWidth) / 2.0
        let scanTop = (viewHeight - scanViewHeight) / 2.0
        let scanRight = scanLeft + scanViewWidth
        let scanBottom = scanTop + scanViewHeight

        return barcodes.filter { barcode in
            let rect = self.getRectBasedOnRotation(rect: barcode.frame, imageSize: imageSize, imageRotation: imageRotation)
            let box = self.getBoundingBoxFromRect(rect: rect)
            let boxScaled = self.scaleBoxBasedOnViewSize(box: box, imageSize: imageSize, viewSize: viewSize)
            return boxScaled.left >= scanLeft &&
                   boxScaled.top >= scanTop &&
                   boxScaled.right <= scanRight &&
                   boxScaled.bottom <= scanBottom
        }
    }

    static func formatBarcode(
        barcode: Barcode,
        imageSize: Size,
        viewSize: Size,
        orientation: UIDeviceOrientation,
        imageRotation: UIImage.Orientation
    ) -> [String:Any] {
        var map : [String:Any] = [:]
        let rect = self.getRectBasedOnRotation(rect: barcode.frame, imageSize: imageSize, imageRotation: imageRotation)
        let box = self.getBoundingBoxFromRect(rect: rect)
        let boxScaled = self.scaleBoxBasedOnViewSize(box: box, imageSize: imageSize, viewSize: viewSize)
        let boxRatio = self.getBoxRatioBasedOnOrientationForUI(
            boxScaled: boxScaled,
            viewSize: viewSize,
            orientation: orientation
        )

        // Raw values
        map["width"] = boxScaled.width
        map["height"] = boxScaled.height
        map["left"] = boxScaled.left
        map["top"] = boxScaled.top
        map["right"] = boxScaled.right
        map["bottom"] = boxScaled.bottom

        // Normalized values
        map["leftRatio"] = boxRatio.leftRatio
        map["topRatio"] = boxRatio.topRatio
        map["widthRatio"] = boxRatio.widthRatio
        map["heightRatio"] = boxRatio.heightRatio

        let displayValue = barcode.displayValue
        map["displayValue"] = displayValue
        let rawValue = barcode.rawValue
        map["rawValue"] = rawValue

        let valueType = barcode.valueType
        switch valueType {
            case .wiFi:
                let ssid = barcode.wifi?.ssid
                map["ssid"] = ssid
                let password = barcode.wifi?.password
                map["password"] = password
                let encryptionType = barcode.wifi?.type
                map["encryptionType"] = encryptionType

            case .URL:
                let title = barcode.url!.title
                map["title"] = title
                let url = barcode.url!.url
                map["url"] = url

            default:
                break;
        }

        return map
    }
}
