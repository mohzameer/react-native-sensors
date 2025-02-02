import { NativeModules, Platform } from "react-native";

// const {
//   RNSensorsGyroscope: GyroNative,
//   RNSensorsAccelerometer: AccNative,
//   RNSensorsMagnetometer: MagnNative,
//   RNSensorsBarometer: BarNative,
//   RNSensorsOrientation: OrientNative,
//   RNSensorsGravity: GravNative,
//   RNSensorMagLessSensor: Magless,
// } = NativeModules;

let GyroNative = Platform.OS === "ios" ? NativeModules.Gyroscope : NativeModules.RNSensorsGyroscope;
let AccNative = Platform.OS === "ios" ? NativeModules.Accelerometer : NativeModules.RNSensorsAccelerometer;
let MagnNative = Platform.OS === "ios" ? NativeModules.Magnetometer : NativeModules.RNSensorsMagnetometer;
let BarNative = Platform.OS === "ios" ? NativeModules.Barometer : NativeModules.RNSensorsBarometer;
let OrientNative = Platform.OS === "ios" ? NativeModules.Orientation : NativeModules.RNSensorsOrientation;
let GravNative = Platform.OS === "ios" ? NativeModules.Gravity : NativeModules.RNSensorsGravity;
let Magless = Platform.OS === "ios" ? NativeModules.MagLessSensor : NativeModules.RNSensorMagLessSensor;

if (!GyroNative && !AccNative && !MagnNative && !BarNative && !OrientNative && !GravNative && !Magless) {
  throw new Error("Native modules for sensors not available. Did react-native link run successfully?");
}

const nativeApis = new Map([
  ["accelerometer", AccNative],
  ["gyroscope", GyroNative],
  ["magnetometer", MagnNative],
  ["barometer", BarNative],
  ["orientation", OrientNative],
  ["gravity", GravNative],
  ["magless", Magless],
]);

// Cache the availability of sensors
const availableSensors = {};

export function start(type) {
  const api = nativeApis.get(type.toLocaleLowerCase());
  api.startUpdates();
}

export function isAvailable(type) {
  if (availableSensors[type]) {
    return availableSensors[type];
  }

  const api = nativeApis.get(type.toLocaleLowerCase());
  const promise = api.isAvailable();
  availableSensors[type] = promise;

  return promise;
}

export function isSensorAvailable(type) {
  if (availableSensors[type]) {
    return true;
  } else {
    return false;
  }
}

export function stop(type) {
  const api = nativeApis.get(type.toLocaleLowerCase());
  api.stopUpdates();
}

export function setUpdateInterval(type, updateInterval) {
  const api = nativeApis.get(type.toLocaleLowerCase());
  api.setUpdateInterval(updateInterval);
}

export function setLogLevelForType(type, level) {
  const api = nativeApis.get(type.toLocaleLowerCase());
  api.setLogLevel(level);
}
