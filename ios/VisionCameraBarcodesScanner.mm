#import <Foundation/Foundation.h>
#import <VisionCamera/FrameProcessorPlugin.h>
#import <VisionCamera/FrameProcessorPluginRegistry.h>
#import <VisionCamera/Frame.h>


#if __has_include("VisionCameraBarcodesScanner/VisionCameraBarcodesScanner-Swift.h")
#import "VisionCameraBarcodesScanner/VisionCameraBarcodesScanner-Swift.h"
#else
#import "VisionCameraBarcodesScanner-Swift.h"
#endif

@interface VisionCameraBarcodesScanner (FrameProcessorPluginLoader)
@end

@implementation VisionCameraBarcodesScanner (FrameProcessorPluginLoader)
+ (void) load {
  [FrameProcessorPluginRegistry addFrameProcessorPlugin:@"scanBarcodes"
    withInitializer:^FrameProcessorPlugin*(VisionCameraProxyHolder* proxy, NSDictionary* options) {
    return [[VisionCameraBarcodesScanner alloc] initWithProxy:proxy withOptions:options];
  }];
}
@end


@interface RCT_EXTERN_MODULE(ImageScanner, NSObject)

RCT_EXTERN_METHOD(process:(NSString *)uri
                  options:(nullable NSArray *)options
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)


+ (BOOL)requiresMainQueueSetup
{
  return NO;
}

@end
