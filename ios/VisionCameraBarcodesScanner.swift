import Foundation
import VisionCamera
import MLKitVision


@objc(VisionCameraBarcodesScanner)
public class VisionCameraBarcodesScanner: FrameProcessorPlugin {
    public override init(proxy: VisionCameraProxyHolder, options: [AnyHashable: Any]! = [:]) {
        super.init(proxy: proxy, options: options)

    }
    public override func callback(
        _ frame: Frame,
        withArguments arguments: [AnyHashable: Any]?
    ) -> Any {
        var data:[Any] = []
        let buffer = frame.buffer
        let image = VisionImage(buffer: buffer);
        image.orientation = getOrientation(orientation: frame.orientation)
        let dispatchGroup = DispatchGroup()
        dispatchGroup.enter()
        barcodes.process(image) { labels, error in
            defer {
                dispatchGroup.leave()
            }
            guard error == nil, let barcodes = barcodes else { return }

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
