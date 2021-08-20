import { NativeEventEmitter, NativeModules, Platform } from "react-native";
import { Observable } from "rxjs";
import { publish, refCount } from "rxjs/operators";
import * as RNSensors from "./rnsensors";

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

const listenerKeys = new Map([
  ["accelerometer", Platform.OS === "ios" ? "Accelerometer" : "RNSensorsAccelerometer"],
  ["gyroscope", Platform.OS === "ios" ? "Gyroscope" : "RNSensorsGyroscope"],
  ["magnetometer", Platform.OS === "ios" ? "Magnetometer" : "RNSensorsMagnetometer"],
  ["barometer", Platform.OS === "ios" ? "Barometer" : "RNSensorsBarometer"],
  ["orientation", Platform.OS === "ios" ? "Orientation" : "RNSensorsOrientation"],
  ["gravity", Platform.OS === "ios" ? "Gravity" : "RNSensorsGravity"],
  ["magless", Platform.OS === "ios" ? "MagLessSensor" : "RNSensorMagLessSensor"],
]);

const nativeApis = new Map([
  ["accelerometer", AccNative],
  ["gyroscope", GyroNative],
  ["magnetometer", MagnNative],
  ["barometer", BarNative],
  ["orientation", OrientNative],
  ["gravity", GravNative],
  ["magless", Magless],
]);

const eventEmitterSubscription = new Map([
  ["accelerometer", null],
  ["gyroscope", null],
  ["magnetometer", null],
  ["barometer", null],
  ["orientation", null],
  ["gravity", null],
  ["magless", null],
]);

function createSensorObservable(sensorType) {
  return Observable.create(function subscribe(observer) {
    this.isSensorAvailable = false;

    this.unsubscribeCallback = () => {
      if (!this.isSensorAvailable) return;
      if (eventEmitterSubscription.get(sensorType)) eventEmitterSubscription.get(sensorType).remove();
      // stop the sensor
      RNSensors.stop(sensorType);
    };

    RNSensors.isAvailable(sensorType).then(
      () => {
        this.isSensorAvailable = true;

        const emitter = new NativeEventEmitter(nativeApis.get(sensorType));

        eventEmitterSubscription.set(
          sensorType,
          emitter.addListener(listenerKeys.get(sensorType), (data) => {
            observer.next(data);
          })
        );

        // Start the sensor manager
        RNSensors.start(sensorType);
      },
      () => {
        observer.error(`Sensor ${sensorType} is not available`);
      }
    );

    return this.unsubscribeCallback;
  }).pipe(makeSingleton());
}

// As we only have one sensor we need to share it between the different consumers
function makeSingleton() {
  return (source) => source.pipe(publish(), refCount());
}

const accelerometer = createSensorObservable("accelerometer");
const gyroscope = createSensorObservable("gyroscope");
const magnetometer = createSensorObservable("magnetometer");
const barometer = createSensorObservable("barometer");
const orientation = createSensorObservable("orientation");
const gravity = createSensorObservable("gravity");
const magless = createSensorObservable("magless");

export default {
  gyroscope,
  accelerometer,
  magnetometer,
  barometer,
  orientation,
  gravity,
  magless,
};
