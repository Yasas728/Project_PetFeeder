int outputPin = 9;       // Change this to any digital pin
int frequency = 1000;    // Frequency in Hz (e.g., 1000 Hz = 1 kHz)

void setup() {
  tone(outputPin, frequency);  // Start square wave on pin 9
}

void loop() {
  // Nothing needed here
}
