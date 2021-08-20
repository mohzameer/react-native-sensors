import sensors from "./src/sensors";
export {
  setUpdateInterval as setUpdateIntervalForType,
  setLogLevelForType,
  isSensorAvailable as IsSensorTypeAvailable,
} from "./src/rnsensors";

export const SensorTypes = {
  accelerometer: "accelerometer",
  gyroscope: "gyroscope",
  magnetometer: "magnetometer",
  barometer: "barometer",
  orientation: "orientation",
  gravity: "gravity",
  magless: "magless",
};

export const { accelerometer, gyroscope, magnetometer, barometer, orientation, gravity, magless } = sensors;
export default sensors;
