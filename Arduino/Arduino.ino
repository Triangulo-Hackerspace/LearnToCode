// Digital
const uint8_t ENA = 5; //Velocidade do motor A (0-255)
const uint8_t ENB = 6; //Velocidade do motor B (0-255)

const uint8_t IN1_MAF = 2; // MOTOR A FORWARD
const uint8_t IN2_MBB = 3; // MOTOR B BACKWARD
const uint8_t IN3_MAB = 4; // MOTOR A BACKWARD
const uint8_t IN4_MBF = 7; // MOTOR B FORWARD

// Variables
uint8_t defaultSpeed = 100;
int pause = 1000;
String messageRead = "";


void setup()
{
	// Setup motors.
	pinMode(ENA, OUTPUT);
	pinMode(ENB, OUTPUT);
	pinMode(IN1_MAF, OUTPUT);
	pinMode(IN2_MBB, OUTPUT);
	pinMode(IN3_MAB, OUTPUT);
	pinMode(IN4_MBF, OUTPUT);

	// Stop driving.
	digitalWrite(ENA, LOW);
	digitalWrite(ENB, LOW);

	// Set the direction of each motor.
	//setMotorDirection();

	// Configure the BT Serial1 communication.
	Serial.begin(9600);
}

void loop(){
	readInput();
}

void readInput() {
	char character;
	uint8_t count = 1;

	while (Serial.available() > 0) {
          character = Serial.read();
	  if (character == ':') {
		switch (messageRead[0]){
                  case 'f':
		  case 'F': // GO FORWARD
  		    emfrente();
                    break;
                  case 'l':
		  case 'L': //Turn Left
		    virarEsquerda();
		    break;
                  case 'r':
		  case 'R':
		    virarDireita();
		  break;
	        }
		messageRead = "";
	} else {
	  messageRead += character;
	}
      }
}


void emfrente(){
  mover(HIGH, LOW, HIGH, LOW);
  delay(pause);
  parar();
}

void virarEsquerda(){  
  mover(HIGH, LOW, LOW, HIGH);
  delay(pause);
  parar();
}

void virarDireita(){ 
  mover(LOW, HIGH, HIGH, LOW);
  delay(pause);
  parar();
}

void parar(){
  analogWrite(ENA, 0);
  analogWrite(ENB, 0);
}

void mover(int vIN1_MAF,int vIN3_MAB,int vN4_MBF,int vIN2_MBB){
  if(
    (vIN1_MAF == HIGH && vIN3_MAB == HIGH) || 
    (vN4_MBF == HIGH && vIN2_MBB == HIGH)
      ){
    Serial.println("ERRO! Motor em curto");
    Serial.print("IN1_MAF: ");
    Serial.print(vIN1_MAF);
    
    Serial.print("IN3_MAB: ");
    Serial.print(vIN3_MAB);
    
    Serial.print("IN4_MBF: ");
    Serial.print(vN4_MBF);
    
    Serial.print("IN2_MBB: ");
    Serial.print(vIN2_MBB);
    return;
  }
  digitalWrite(IN1_MAF, vIN1_MAF); 
  digitalWrite(IN3_MAB, vIN3_MAB);
  
  digitalWrite(IN4_MBF, vN4_MBF); 
  digitalWrite(IN2_MBB, vIN2_MBB);
  
  analogWrite(ENA, defaultSpeed);
  analogWrite(ENB, defaultSpeed);
}
