package project;

import arduino.Arduino;

public final class ArduinoClass {

    private final Arduino arduino;

    public ArduinoClass() {
        this.arduino = new Arduino("COM3", 9600);
    }

    public void arduinoMessage(final char message) {
        arduino.serialWrite(message);
    }

    public void arduinoOpenConnection() {
        arduino.openConnection();
    }

    public void arduinoCloseConnection() {
        arduino.closeConnection();
    }

}
