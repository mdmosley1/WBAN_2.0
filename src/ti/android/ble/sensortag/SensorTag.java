// This class contains all of the UUID strings that correspond to different sensors and notifications.

package ti.android.ble.sensortag;

import static java.util.UUID.fromString;

import java.util.UUID;

public class SensorTag {

  public final static UUID 
      
      UUID_ACC_SERV = fromString("f000aa10-0451-4000-b000-000000000000"),
      UUID_ACC_DATA = fromString("f000aa11-0451-4000-b000-000000000000"),
      UUID_ACC_CONF = fromString("f000aa12-0451-4000-b000-000000000000"), // 0: disable, 1: enable
      UUID_ACC_PERI = fromString("f000aa13-0451-4000-b000-000000000000"), // Period in tens of milliseconds

      UUID_GYR_SERV = fromString("f000aa50-0451-4000-b000-000000000000"), 
      UUID_GYR_DATA = fromString("f000aa51-0451-4000-b000-000000000000"),
      UUID_GYR_CONF = fromString("f000aa52-0451-4000-b000-000000000000"), // 0: disable, bit 0: enable x, bit 1: enable y, bit 2: enable z
      UUID_GYR_PERI = fromString("f000aa53-0451-4000-b000-000000000000"), // Period in tens of milliseconds

      // NOTE: The _PERI UUIDs can be used to change the transmission frequency of the sensors.
      
      UUID_KEY_SERV = fromString("0000ffe0-0000-1000-8000-00805f9b34fb"), 
      UUID_KEY_DATA = fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
}
