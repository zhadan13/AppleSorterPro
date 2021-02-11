package project;

import arduino.Arduino;

public final class ArduinoClass {

    private final Arduino arduino = new Arduino("COM3", 9600);

    public ArduinoClass() {

    }

    protected void arduinoMessage(final char message) {
        arduino.serialWrite(message);
    }

    protected void arduinoOpenConnection() {
        arduino.openConnection();
    }

    protected void arduinoCloseConnection() {
        arduino.closeConnection();
    }

}
