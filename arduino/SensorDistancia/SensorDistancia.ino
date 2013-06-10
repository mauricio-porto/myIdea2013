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
#define DELAY 1000

int range = DEFAULT_DISTANCE;

NewPing sonar(TRIGGER_PIN, ECHO_PIN, MAX_DISTANCE); // NewPing setup of pins and maximum distance.

void setup() {
  Serial.begin(9600); // Open serial monitor at 9600 baud to send info via Bluetooth.
}

void loop() {
  if (Serial.available() > 0) {
    char c = Serial.read();
    if (c == '+' && range < MAX_DISTANCE) {
      range += STEP_RANGE;
      Serial.print(" RANGE: ");
      Serial.println(range, DEC);
      delay(3000);
    }
    if (c == '-' && range > MIN_DISTANCE) {
      range -= STEP_RANGE;
      Serial.print(" RANGE: ");
      Serial.println(range, DEC);
      delay(3000);
    }
  }

  unsigned int uS = sonar.ping(); // Send ping, get ping time in microseconds (uS).
  unsigned int distCm = uS / US_ROUNDTRIP_CM;
  if (distCm > 0 && distCm <= range) {
    Serial.print("  "); // Lets give something to "wake-up" the receiving buffer
    Serial.print(distCm);
  }
  delay(DELAY);
}

