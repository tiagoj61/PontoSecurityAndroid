# Mobile Module - Work Clock System

This document outlines the functionality of the **Mobile** module in the work clock system integrated with Bluetooth, Arduino, and a monolithic backend.

## Module Overview
The Mobile module is designed to:
1. Connect to a BLE (Bluetooth Low Energy) device.
2. Capture the MAC address of the user’s mobile device.
3. Send access credentials (PIN code) along with the MAC address to the server.
4. Receive access validation from the server and notify the Arduino of the connection status.

## Technologies Used
- **Language**: Java
- **Frameworks**: Android SDK
- **Protocols**: HTTP/HTTPS for server communication and Bluetooth for BLE integration

## Features

1. **Connection with Arduino via BLE**
   - The application uses the BLE device as a connection point to start the authentication process.
   - The `ComunicateToArduino.connect` function establishes the initial communication.

2. **MAC Address Capture**
   - The app lists all paired devices at runtime.
   - The MAC address is extracted and sent to the server as the user’s identifier.

3. **Sending Credentials to the Server**
   - The user-entered PIN code is combined with the MAC address and sent to the backend.
   - Endpoint used: `/PontoSecurity/rest/funcionario/login`.

4. **Access Validation**
   - The backend responds with the authentication status.
   - On success, the Arduino is notified to grant access.

5. **User Feedback**
   - The app displays success or failure messages to the user using the `Toast` component.

## Code Structure

### Main Class: `PassActivity`

- **`onCreate` Function:**
  Sets up the Bluetooth environment and initializes the PIN code entry interface.

- **`send` Function:**
  Captures paired devices, assembles the payload for the backend, and notifies the Arduino of the operation’s status.

### Helper Class: `ComunicateToServer`

- Responsible for sending information to the backend.
- Implements callbacks to handle success or error scenarios in communication.

### Helper Class: `ComunicateToArduino`

- Establishes a connection with the BLE device.
- Sends commands to the Arduino indicating success or failure status.

## Example Payload Sent to Server
```json
{
  "codigo": "12345",
  "enderecoMacDevices": [
    "00:11:22:33:44:55",
    "66:77:88:99:AA:BB"
  ]
}
```

## Operational Workflow
1. User opens the application.
2. The application connects to the BLE device.
3. User enters the PIN code.
4. The application sends the PIN and MAC address to the backend.
5. Backend validates the data and returns the connection status.
6. Arduino is notified to grant or deny access.

## Dependencies
- Permissions in `AndroidManifest.xml`:
  ```xml
  <uses-permission android:name="android.permission.BLUETOOTH"/>
  <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
  <uses-permission android:name="android.permission.INTERNET"/>
  ```

## Notes
- Ensure the BLE device is correctly configured before launching the application.
- Use HTTPS connections to ensure data security.
