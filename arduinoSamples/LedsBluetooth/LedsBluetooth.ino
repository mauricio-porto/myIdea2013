// TX Bt <-> Rx Arduino
// Rx Bt <-> Tx Arduino
//


void setup() {
  pinMode(11, OUTPUT);
  pinMode(12, OUTPUT);
  pinMode(13, OUTPUT);

  Serial.begin(9600);
}

void loop() {
  char c = Serial.read();

  if (c == 'R') digitalWrite(11, HIGH);
  if (c == 'G') digitalWrite(12, HIGH);
  if (c == 'B') digitalWrite(13, HIGH);

  if (c == 'r') digitalWrite(11, LOW);
  if (c == 'g') digitalWrite(12, LOW);
  if (c == 'b') digitalWrite(13, LOW);

  delay(1000); 
}
