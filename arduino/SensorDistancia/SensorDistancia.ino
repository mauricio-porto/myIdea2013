// ---------------------------------------------------------------------------
// Example NewPing library sketch that does a ping about 20 times per second.
// ---------------------------------------------------------------------------

#include <NewPing.h>

#define TRIGGER_PIN  12  // Arduino pin tied to trigger pin on the ultrasonic sensor.
#define ECHO_PIN     11  // Arduino pin tied to echo pin on the ultrasonic sensor.
#define MAX_DISTANCE 300 // Maximum distance we want to ping for (in centimeters). Maximum sensor distance is rated at 400-500cm.
#define MIN_DISTANCE 50
#define DEFAULT_DISTANCE 150
#define STEP_RANGE 10
#define DELAY 100

int range = DEFAULT_DISTANCE;
boolean running = false;
boolean paused = false;

NewPing sonar(TRIGGER_PIN, ECHO_PIN, MAX_DISTANCE); // NewPing setup of pins and maximum distance.

void setup() {
  Serial.begin(9600); // Open serial monitor at 9600 baud to send info via Bluetooth.
}

void loop() {
  unsigned int uS = sonar.ping(); // Send ping, get ping time in microseconds (uS).
  unsigned int distCm = uS / US_ROUNDTRIP_CM;

  if (Serial.available() > 0) {
    char c = Serial.read();
    if (c == '!') {
      running = !running;
      goto bailout;
    }
    if (c == '+' && range < MAX_DISTANCE) {
      range += STEP_RANGE;
      goto bailout;
    }
    if (c == '-' && range > MIN_DISTANCE) {
      range -= STEP_RANGE;
      goto bailout;
    }
    if (c == '?') {
      Serial.print(" ?");
      Serial.print(distCm);
      Serial.print("#");
      Serial.print(range);
      if (running) {
        Serial.print("!");
      }
      goto bailout;
    }
  }

  if (running) {
    if (distCm > 0 && distCm <= range) {
      if (distCm <= 10) {
        paused = !paused;
        delay(2000);  // Lets give some time to the hand goes away...
      }
      if (!paused) {
        Serial.print("  "); // Lets give something to "wake-up" the receiving buffer
        Serial.println(distCm, DEC);
      }
    }
  }
bailout:
  delay(DELAY);
}

