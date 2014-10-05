package ti.android.ble.sensortag;

//import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT8;
import static ti.android.ble.sensortag.SensorTag.*;

import java.util.List;
import java.util.UUID;

import ti.android.util.Point3D;

import android.bluetooth.BluetoothGattCharacteristic;


/**
 * This enum encapsulates the differences amongst the sensors. The differences include UUID values and how to interpret the
 * characteristic-containing-measurement.
 */
public enum Sensor {

  ACCELEROMETER(UUID_ACC_SERV, UUID_ACC_DATA, UUID_ACC_CONF) {
  	@Override
  	public Point3D convert(final byte[] value) {
  		/*
  		 * The accelerometer has the range [-2g, 2g] with unit (1/64)g.
  		 * 
  		 * To convert from unit (1/64)g to unit g we divide by 64.
  		 * 
  		 * (g = 9.81 m/s^2)
  		 * 
  		 * The z value is multiplied with -1 to coincide with how we have arbitrarily defined the positive y direction. (illustrated by the apps accelerometer
  		 * image)
  		 */
  		Integer x = (int) value[0];
  		Integer y = (int) value[1];
  		Integer z = (int) value[2] * -1;

  		return new Point3D(x / 64.0, y / 64.0, z / 64.0);
  	}
  },

  GYROSCOPE(UUID_GYR_SERV, UUID_GYR_DATA, UUID_GYR_CONF, (byte)7) {
    @Override
    public Point3D convert(final byte [] value) {

      float y = shortSignedAtOffset(value, 0) * (500f / 65536f) * -1;
      float x = shortSignedAtOffset(value, 2) * (500f / 65536f);
      float z = shortSignedAtOffset(value, 4) * (500f / 65536f);
      
      return new Point3D(x,y,z);      
    }
  };


  /**
   * Gyroscope, Magnetometer, Barometer, IR temperature all store 16 bit two's complement values in the awkward format LSB MSB, which cannot be directly parsed
   * as getIntValue(FORMAT_SINT16, offset) because the bytes are stored in the "wrong" direction.
   * 
   * This function extracts these 16 bit two's complement values.
   * */
  private static Integer shortSignedAtOffset(byte[] c, int offset) {
    Integer lowerByte = (int) c[offset] & 0xFF; 
    Integer upperByte = (int) c[offset+1]; // // Interpret MSB as signed
    return (upperByte << 8) + lowerByte;
  }

  private static Integer shortUnsignedAtOffset(byte[] c, int offset) {
    Integer lowerByte = (int) c[offset] & 0xFF; 
    Integer upperByte = (int) c[offset+1] & 0xFF; // // Interpret MSB as signed
    return (upperByte << 8) + lowerByte;
  }

  public void onCharacteristicChanged(BluetoothGattCharacteristic c) {
    throw new UnsupportedOperationException("Programmer error, the individual enum classes are supposed to override this method.");
  }

  public Point3D convert(byte[] value) {
    throw new UnsupportedOperationException("Programmer error, the individual enum classes are supposed to override this method.");
  }

	private final UUID service, data, config;
	private byte enableCode; // See getEnableSensorCode for explanation.
	public static final byte DISABLE_SENSOR_CODE = 0;
	public static final byte ENABLE_SENSOR_CODE = 1;
	public static final byte CALIBRATE_SENSOR_CODE = 2;

	/**
	 * Constructor called by the Gyroscope because he needs a different enable
	 * code.
	 */
  private Sensor(UUID service, UUID data, UUID config, byte enableCode) {
    this.service = service;
    this.data = data;
    this.config = config;
    this.enableCode = enableCode;
  }

  /**
   * Constructor called by all the sensors except Gyroscope
   * */
  private Sensor(UUID service, UUID data, UUID config) {
    this.service = service;
    this.data = data;
    this.config = config;
    this.enableCode = ENABLE_SENSOR_CODE; // This is the sensor enable code for all sensors except the gyroscope
  }

  /**
   * @return the code which, when written to the configuration characteristic, turns on the sensor.
   * */
  public byte getEnableSensorCode() {
    return enableCode;
  }

  public UUID getService() {
    return service;
  }

  public UUID getData() {
    return data;
  }

  public UUID getConfig() {
    return config;
  }

  public static Sensor getFromDataUuid(UUID uuid) {
    for (Sensor s : Sensor.values()) {
      if (s.getData().equals(uuid)) {
        return s;
      }
    }
    throw new RuntimeException("Programmer error, unable to find uuid.");
  }
  
  public static final Sensor[] SENSOR_LIST = {ACCELEROMETER, GYROSCOPE};
}
