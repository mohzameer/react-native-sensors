//
//  RNSensorMagLessSensor.m
//  RNSensors
//
//  Created by Mohammed Zameer on 2021-08-20.
//  Copyright © 2021 Facebook. All rights reserved.
//

#import "RNSensorMagLessSensor.h"
#import <React/RCTBridge.h>
#import <React/RCTEventDispatcher.h>
#import "Magnetometer.h"
#import "Utils.h"

@implementation MagLessSensor

@synthesize bridge = _bridge;

RCT_EXPORT_MODULE();

- (id) init {
    self = [super init];
    NSLog(@"Magless");

    if (self) {
      
    }
    return self;
}

- (NSArray<NSString *> *)supportedEvents
{
  return @[@"Magless"];
}

+ (BOOL)requiresMainQueueSetup
{
    return NO;
}

RCT_REMAP_METHOD(isAvailable,
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
   
}


RCT_EXPORT_METHOD(setUpdateInterval:(double) interval) {
   
}

RCT_EXPORT_METHOD(setLogLevel:(int) level) {
   
}

RCT_EXPORT_METHOD(getUpdateInterval:(RCTResponseSenderBlock) cb) {
   
}

RCT_EXPORT_METHOD(getData:(RCTResponseSenderBlock) cb) {
    
}

RCT_EXPORT_METHOD(startUpdates) {
   

}

RCT_EXPORT_METHOD(stopUpdates) {
 
}

@end

