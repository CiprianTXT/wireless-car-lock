#include <Arduino.h>
#include <SoftwareSerial.h>
#include <LCD_I2C.h>

// Defining pins
#define BUZZER_PIN      3
#define LOCK_LED_PIN    4
#define BT_AT_PIN       5
#define BT_POWER_PIN    6
#define RX_PIN          7
#define TX_PIN          8

// Defining AT commands
#define AT_NAME         "AT+NAME"
#define AT_PSWD         "AT+PSWD"

// Global variables
bool locked = false;
unsigned long prevTimeLockLED = 0;
bool hideContact = false;
bool updateContact = false;
String contactName;
String contactNumber;

SoftwareSerial BTSerial(RX_PIN, TX_PIN);
LCD_I2C Display(0x27, 16, 2);

void commandParser();
void configureBluetooth();
void serialFlush();

void setup() {
    // Starting display
    Display.begin();
    Display.backlight();

    // Starting lock LED and buzzer
    pinMode(BUZZER_PIN, OUTPUT);
    pinMode(LOCK_LED_PIN, OUTPUT);
    digitalWrite(LOCK_LED_PIN, LOW);

    // Starting bluetooth module
    pinMode(BT_POWER_PIN, OUTPUT);
    pinMode(BT_AT_PIN, OUTPUT);
    digitalWrite(BT_POWER_PIN, HIGH);
    digitalWrite(BT_AT_PIN, LOW);

    // Starting serials
    Serial.begin(38400);
    BTSerial.begin(38400);
}

void loop() {
    // Parsing incoming commands
    if (BTSerial.available()) {
        commandParser();
    }

    // Blink LED if door is locked
    unsigned long lockLEDBlinkInterval = millis() - prevTimeLockLED;
    if (locked) {
        if (lockLEDBlinkInterval >= 1000) {
            digitalWrite(LOCK_LED_PIN, HIGH);
            if (lockLEDBlinkInterval > 1100) {
                prevTimeLockLED = millis();
                digitalWrite(LOCK_LED_PIN, LOW);
            }
        }
    } else {
        digitalWrite(LOCK_LED_PIN, LOW);
    }

    // Display contact info
    if (updateContact) {
        Display.clear();
        Display.setCursor(0, 0);
        Display.print(contactName);
        Display.setCursor(0, 1);
        Display.print(contactNumber);
        updateContact = false;
    }

    // BTSerial.println(Serial.readString());
    // Serial.println(locked ? "Locked" : "Unlocked");
    // configureBluetooth();
}

void commandParser() {
    bool commandSuccessful = false;
    String command = BTSerial.readString();
    command.trim();

    // Sync lock state with phone app
    if (command.equals("sync")) {
        BTSerial.println(locked ? "Locked" : "Unlocked");
        commandSuccessful = true;
    }

    // Lock/Unlock car doors
    if (command.equals("lock")) {
        locked = !locked;
        tone(BUZZER_PIN, 500, 500);
        commandSuccessful = true;
    }

    // Hide/Show contact info
    if (command.startsWith("hideInfo")) {
        short int colonPos = command.indexOf(':');
        hideContact = command.substring(colonPos + 1).equals("0") ? false : true;
        if (hideContact) {
            Display.noBacklight();
            Display.clear();
        } else {
            Display.backlight();
            updateContact = true;
        }
        commandSuccessful = true;
    }

    // Set contact info
    if (command.startsWith("setInfo")) {
        short int colonPos = command.indexOf(':');
        short int commaPos = command.indexOf(',');
        contactName = command.substring(colonPos + 1, commaPos);
        contactNumber = command.substring(commaPos + 1);
        updateContact = true;
        commandSuccessful = true;
    }

    // Set name for bluetooth module
    if (command.startsWith("setBtName")) {
        short int colonPos = command.indexOf(':');
        String moduleName = command.substring(colonPos + 1);
        String atCommand = AT_NAME;
        String atParams = "=";
        atParams.concat(moduleName);
        atCommand.concat(atParams);
        digitalWrite(BT_POWER_PIN, LOW);
        digitalWrite(BT_AT_PIN, HIGH);
        digitalWrite(BT_POWER_PIN, HIGH);
        delay(1);
        BTSerial.println(atCommand);
        digitalWrite(BT_POWER_PIN, LOW);
        digitalWrite(BT_AT_PIN, LOW);
        digitalWrite(BT_POWER_PIN, HIGH);
        serialFlush();
        commandSuccessful = true;
    }

    // Set passkey for bluetooth module
    if (command.startsWith("setBtPswd")) {
        short int colonPos = command.indexOf(':');
        String modulePswd = command.substring(colonPos + 1);
        String atCommand = AT_PSWD;
        String atParams = "=";
        atParams.concat(modulePswd);
        atCommand.concat(atParams);
        digitalWrite(BT_POWER_PIN, LOW);
        digitalWrite(BT_AT_PIN, HIGH);
        digitalWrite(BT_POWER_PIN, HIGH);
        delay(1);
        BTSerial.println(atCommand);
        digitalWrite(BT_POWER_PIN, LOW);
        digitalWrite(BT_AT_PIN, LOW);
        digitalWrite(BT_POWER_PIN, HIGH);
        serialFlush();
        commandSuccessful = true;
    }

    BTSerial.println(commandSuccessful ? "success" : "fail");
}

void configureBluetooth() {
    digitalWrite(BT_POWER_PIN, LOW);
    digitalWrite(BT_AT_PIN, HIGH);
    digitalWrite(BT_POWER_PIN, HIGH);
    while (1) {
        if (Serial.available()) {
            BTSerial.write(Serial.read());
        }

        if (BTSerial.available()) {
            Serial.write(BTSerial.read());
        }
    }
}

void serialFlush() {
    while (Serial.available()) {
        Serial.read();
    }
    while (BTSerial.available()) {
        BTSerial.read();
    }
}
