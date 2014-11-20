package ti.android.ble.sensortag;

//import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT8;
import ti.android.util.Point3D;
import android.bluetooth.BluetoothGattCharacteristic;

public enum Sensor {

	ACCELEROMETER() {
		@Override
		public Point3D convert(final byte[] value) {

			Integer x = (int) value[0];
			Integer y = (int) value[1];
			Integer z = (int) value[2] * -1;

			return new Point3D(x / 64.0, y / 64.0, z / 64.0);
		}
	},

	GYROSCOPE() {
		@Override
		public Point3D convert(final byte [] value) {

			float y = shortSignedAtOffset(value, 0) * (500f / 65536f) * -1;
			float x = shortSignedAtOffset(value, 2) * (500f / 65536f);
			float z = shortSignedAtOffset(value, 4) * (500f / 65536f);

			return new Point3D(x,y,z);      
		}
	};

	private static Integer shortSignedAtOffset(byte[] c, int offset) {
		Integer lowerByte = (int) c[offset] & 0xFF; 
		Integer upperByte = (int) c[offset+1]; // // Interpret MSB as signed
		return (upperByte << 8) + lowerByte;
	}

	public void onCharacteristicChanged(BluetoothGattCharacteristic c) {
		throw new UnsupportedOperationException("Programmer error, the individual enum classes are supposed to override this method.");
	}

	public Point3D convert(byte[] value) {
		throw new UnsupportedOperationException("Programmer error, the individual enum classes are supposed to override this method.");
	}

	private String asensor;
	private String theSensor;

	private Sensor() {

	}
	private Sensor(String asensor) {
		this.theSensor = asensor;
	}

	@Override
	public String toString() {
		return theSensor;
	}

	public static final Sensor[] SENSOR_LIST = {ACCELEROMETER, GYROSCOPE};
}