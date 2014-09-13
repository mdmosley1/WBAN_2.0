WBAN Application Notes Overview: The application handles communication between the sensor device and the phone. It manages connections between all Bluetooth low energy (BLE) devices in the area and plots accelerometer data from one device at a time. It then forwards that data on to the database.

Class Descriptions

BleDeviceInfo – Stores and holds all information (device address, RSSI, etc.) for a given BLE device.

BluetoothLeService – Background service that handles communication between the sensor device and the phone. It sends updates the DeviceActivity when some kind of communication occurs between the device and phone. These updates consist of:
- Connection changes (disconnect or connection)
- Reads (if the gatt server on the device writes to the phone)
- Writes (If the phone wants to right to the device. This can be to change the transmission data rate or other key features on the device)
- Notifications (the device sends a notification to tell the phone that it has sensor data available. The phone can use this notification to extract the data and manipulate it)

GattInfo – Handles the UUID information for the Gatt server hosted on the device. The UUID is a string that identifies the device by its address, name and description. It also contains information on the sensors on the board and how to change them.

AboutDialog – Handles the ‘about’ option accessible from the main activity. Doesn’t do much other than bring up a dialog box displaying information.

DeviceActivity – The activity that handles the communication between the device and phone. This activity gets updates from the BluetoothLeSevice regarding device communication. Using these updates the DeviceActivity can receive sensor data. Using this data, the DeviceActivity plots the data and writes it to the web server. If there is no network connection, the activity saves the data to internal phone memory. When network connection is restored, it flushes all saved data to the database. The DeviceActivity also has options to change what accelerometer axes are being plotted and can call the HistoryPlot activity.

DeviceView – This is a class that was used by the original TI application to display raw data for each sensor. It is not used by the WBAN app, but remains in the code currently.

HistoryPlot – An activity that allows the user to enter starting and ending dates and times and retrieve data from the database corresponding to that time interval. It then creates a static plot of that time period. This activity is only partially complete, and doesn’t not function currently.

IOUtil – A class that is used to convert a saved file into a pure byte array. It is used by the DeviceActivity to retrieve saved acceleration data when the application is flushing old data to the database.

MainActivity – The main activity of the application. It handles device discovery and initial connection, as well as setting up the display framework for the application. It initiates the BluetoothLeService. This is based heavily on TI code and will not need to be changed.

NetworkUpdateReceiver – Listens for changes in the network connection. It is not used currently as network listening is taken care of completely in the DeviceActivity. It is old code that has not been removed.

PreferencesActivity – Old TI code that allows the user to change which sensor data is displayed. As we are only interested in plotting accelerometer data, this activity is unnecessary and is not used.

PreferencesFragment – Manages the display for the PreferencesActivity. Like the activity, it is old TI code that is not used anymore.

PreferencesListener – Listens for changes in the preferences and adjusts the display accordingly. Like the activity and fragment, it is old TI code that is not used anymore.

ScanView – Handles the display and logging for the device scanning process. It is implemented in the main activity. It is based heavily on TI code and should not be changed.

Sensor – Enum that contains data regarding each different sensor on the TI device. It includes different UUID information for each sensor. It is based heavily on TI code and should not be changed.

SensorTag – Contains a list of all the UUIDs for each sensor and sensor attribute. This class does not need to be changed, as the UUIDs are set for the TI device.

SimpleKeysStatus – Enum that defines the states of the two buttons for the TI device. It is not useful for our application, as we do not use the buttons on the device. It is TI code, and is not removed to avoid throwing errors in the other activities.

ViewPagerActivity – TI code that adjusts the display properties for some of the activities. Does not need to be changed.

Conversion – Contains utility functions for data conversion, etc.

CustomTimer – Timer that determines how quickly device discovery times out.

CustomTimerCallback – Abstract class that has functions which are called on timeout and on tick. It is used by the CustomTimer class.

CustomToast – A modified toast class. It has slightly different properties than a default toast call.

Point3D – Contains three variables that each correspond to a different axis. Used for storing acceleration axes data. NOTES: Classes highlighted in red are not used. They are either old TI code or were used at one point but are now deprecated.