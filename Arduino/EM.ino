#include <LiquidCrystal.h>
#include <Keypad.h>

int loopDelay = 10;
int rotorPositions = 26;
int revPos = 5;

// LCD
byte lcdRs = 7;
byte lcdEn = 8;
byte lcdD4 = 9;
byte lcdD5 = 10;
byte lcdD6 = 11;
byte lcdD7 = 12;
byte lcdContrast = A5;
LiquidCrystal lcd(lcdRs, lcdEn, lcdD4, lcdD5, lcdD6, lcdD7);

int rot1 = 0;
int rot2 = 0;
int rot3 = 0;
int conf1 = 0;
int conf2 = 0;
int conf3 = 0;
byte incRot1 = A0;
byte incRot2 = A1;
byte incRot3 = A2;
byte rotMode = A4;
bool pressedInc1 = false;
bool pressedInc2 = false;
bool pressedInc3 = false;
bool pressedRotMode = false;

char rotorToChar(int r) {
  switch(r) {
    case 0: return 'A';
    case 1: return 'B';
    case 2: return 'C';
    case 3: return 'D';
    case 4: return 'E';
    case 5: return 'F';
    case 6: return 'G';
    case 7: return 'H';
    case 8: return 'I';
    case 9: return 'J';
    case 10: return 'K';
    case 11: return 'L';
    case 12: return 'M';
    case 13: return 'N';
    case 14: return 'O';
    case 15: return 'P';
    case 16: return 'Q';
    case 17: return 'R';
    case 18: return 'S';
    case 19: return 'T';
    case 20: return 'U';
    case 21: return 'V';
    case 22: return 'X';
    case 23: return 'Y';
    case 24: return 'W';
    case 25: return 'Z';
  }
  return '0';
}

bool rotorControlModeIncrement = true;
bool revToggle = true;
void printRotors() {
    lcd.setCursor(0, 0);
    if (revToggle) {
      lcd.print(rotorToChar(rot1));
      lcd.print(rotorToChar(rot2));
      lcd.print(rotorToChar(rot3));
    } else {
      lcd.print(conf1 + 1);
      lcd.print(conf2 + 1);
      lcd.print(conf3 + 1); 
    }
    if (rotorControlModeIncrement) { 
      lcd.print('+');
    } else {
      lcd.print('-');
    }
}

// Keypad
byte kbRow2 = 2;
byte kbRow3 = 3;
byte kbCol3 = 4;
byte kbRow4 = 5;
byte kbCol1 = 6;
byte kbRow1 = 13;
byte kbCol2 = A3;
const byte rows = 4; //four rows
const byte cols = 3; //three columns
char keys[rows][cols] = {
  {'1','2','3'},
  {'4','5','6'},
  {'7','8','9'},
  {'*','0','#'}
};
byte rowPins[rows] = {kbRow1, kbRow2, kbRow3, kbRow4}; //connect to the row pinouts of the keypad
byte colPins[cols] = {kbCol1, kbCol2, kbCol3}; //connect to the column pinouts of the keypad
Keypad keypad = Keypad(makeKeymap(keys), rowPins, colPins, rows, cols);

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  pinMode(incRot1, INPUT_PULLUP);
  pinMode(incRot2, INPUT_PULLUP);
  pinMode(incRot3, INPUT_PULLUP);
  pinMode(rotMode, INPUT_PULLUP);
  analogWrite(lcdContrast, 0);
  lcd.begin(16, 2);
  printRotors();
}

struct Character {
  int i = 0;
  int size;
  float delta = 0.0f;
  char* list;

  Character(char* l, int s) {
    this->list = l;
    this->size = s;
  }

  char next() {
    char res = list[i++];
    
    if (i >= this->size) {
      i = 0;
    }
    
    return res;
  }

  void reset() {
    i = 0;
  }
};

void advanceRotors() {
  rot3 = (rot3 + 1) % rotorPositions;
  if (rot3 != 0) {
    printRotors();
    return;
  }
  rot2 = (rot2 + 1) % rotorPositions;
  if (rot2 != 0) {
    printRotors();
    return;
  }
  rot1 = (rot1 + 1) % rotorPositions;
  printRotors();
}

void retRotors() {
  rot3 = (rot3 - 1) % rotorPositions;
  if (rot3 < 0) {
    rot3 = rotorPositions - 1;
  }
  if (rot3 != rotorPositions - 1) {
    printRotors();
    return;
  }
  rot2 = (rot2 - 1) % rotorPositions;
  if (rot2 < 0) {
    rot2 = rotorPositions - 1;
  }
  if (rot2 != rotorPositions - 1) {
    printRotors();
    return;
  }
  rot1 = (rot1 - 1) % rotorPositions;
  if (rot1 < 0) {
    rot1 = rotorPositions - 1;
  }
  printRotors();
}

struct Message {
  String contents;
  int cursor = 0;
  float delay = 300.0f;
  float deltaTime = 0.0f;
  float blinkDeltaTime = 0.0f;
  float blinkDelay = 800.0f;
  bool blink = false;
  bool flushed = false;

  int r1, r2, r3;
  
  Message(String m) {
    this->contents = m;
  }

  void redraw() {
    this->flushed = false;
  }

  void clear() {
    this->contents = "";
    this->cursor = 0;
    lcd.setCursor(0, 1);
    lcd.print((char)255);
    lcd.print("               ");
  }

  void clip() {
    if (this->contents.length() <= 0) {
      return;
    }
    
    this->contents.remove(this->contents.length() - 1);
    this->redraw();
  }

  void append(String s) {
    if (isEmpty()) {
      r1 = rot1;
      r2 = rot2;
      r3 = rot3;
    }
    this->contents += s;
    this->redraw();
  }
  
  void edit(char c) {
    this->contents.remove(this->contents.length() - 1);
    this->contents += String(c);
    this->redraw();
  }

  void drawLast() {
    if (flushed) {
      return;
    }
    flushed = true;

    lcd.setCursor(0, 1);
    int start = this->contents.length() - 15;
    if (this->contents.length() < 15) {
      start = 0;
    }

    int i;
    for (i = start; i < this->contents.length(); i++) {
      lcd.print(this->contents.charAt(i));
    }
    lcd.print((char)255);
    i++;

    for (; i < 16; i++) {      
      lcd.print(' ');
    }
  }

  bool isEmpty() {
    return this->contents.length() <= 0;
  }
};


Message m = Message("Hello world testing lcd...");
char c1l[1] = {'1'};
char c2l[7] = {'a', 'b', 'c', 'A', 'B', 'C', '2'};
char c3l[7] = {'d', 'e', 'f', 'D', 'E', 'F', '3'};
char c4l[7] = {'g', 'h', 'i', 'G', 'H', 'I', '4'};
char c5l[7] = {'j', 'k', 'l', 'J', 'K', 'L', '5'};
char c6l[7] = {'m', 'n', 'o', 'M', 'N', 'O', '6'};
char c7l[9] = {'p', 'q', 'r', 's', 'P', 'Q', 'R', 'S', '7'};
char c8l[7] = {'t', 'u', 'v', 'T', 'U', 'V', '8'};
char c9l[9] = {'w', 'x', 'y', 'z', 'W', 'X', 'Y', 'Z', '9'};
char c0l[2] = {' ', '0'};
Character c1 = Character(c1l, 1);
Character c2 = Character(c2l, 7);
Character c3 = Character(c3l, 7);
Character c4 = Character(c4l, 7);
Character c5 = Character(c5l, 7);
Character c6 = Character(c6l, 7);
Character c7 = Character(c7l, 7);
Character c8 = Character(c8l, 7);
Character c9 = Character(c9l, 7);
Character c0 = Character(c0l, 2);
Character* last = NULL;
float deltaTimeCharater = 0.0f;
float deltaTimeRev = 0.0f;
float characterChangeDelay = 500.0f;

void waitKey() {
  while (Serial.available() == 0) {
      delay(100);
  }
}

void loop() {
  if (m.isEmpty()) {
    if (Serial.available()) {
      while (true) {
        if (!Serial.available()) {
          break;
        }
        char r = (char)Serial.read();
        //if (r == '\n') {
        m.append(String(r));
      }
      printRotors();
      return;
    }
  }

  char key = keypad.getKey();

  deltaTimeCharater += loopDelay;
  if (key != NO_KEY) {
    deltaTimeCharater = 0;
  }
  
  if (deltaTimeCharater >= characterChangeDelay) {
    last->reset();
    deltaTimeCharater = 0;
    last = NULL;
  }

  switch (key) {
  case '1':
    if (last != &c1) {
      last->reset();
      last = &c1;
    }
    
    advanceRotors();
    m.append(String(key));
    break;
  case '2':
    if (last == NULL) {
    advanceRotors();
      last = &c2;
      m.append(String(c2.next()));
      break;
    }

    if (last != &c2) {
    advanceRotors();
      last->reset();
      last = &c2;
      m.append(String(c2.next()));
      break;
    }

    m.edit(c2.next());
    
    break;
  case '3':
    if (last == NULL) {
    advanceRotors();
      last = &c3;
      m.append(String(c3.next()));
      break;
    }

    if (last != &c3) {
    advanceRotors();
      last->reset();
      last = &c3;
      m.append(String(c3.next()));
      break;
    }

    m.edit(c3.next());
    
    break;
  case '4':
    if (last == NULL) {
    advanceRotors();
      last = &c4;
      m.append(String(c4.next()));
      break;
    }

    if (last != &c4) {
    advanceRotors();
      last->reset();
      last = &c4;
      m.append(String(c4.next()));
      break;
    }

    m.edit(c4.next());
    
    break;
  case '5':
    if (last == NULL) {
    advanceRotors();
      last = &c5;
      m.append(String(c5.next()));
      break;
    }

    if (last != &c5) {
    advanceRotors();
      last->reset();
      last = &c5;
      m.append(String(c5.next()));
      break;
    }

    m.edit(c5.next());
    
    break;
  case '6':
    if (last == NULL) {
    advanceRotors();
      last = &c6;
      m.append(String(c6.next()));
      break;
    }

    if (last != &c6) {
    advanceRotors();
      last->reset();
      last = &c6;
      m.append(String(c6.next()));
      break;
    }

    m.edit(c6.next());
    
    break;
  case '7':
    if (last == NULL) {
    advanceRotors();
      last = &c7;
      m.append(String(c7.next()));
      break;
    }

    if (last != &c7) {
    advanceRotors();
      last->reset();
      last = &c7;
      m.append(String(c7.next()));
      break;
    }

    m.edit(c7.next());
    
    break;
  case '8':
    if (last == NULL) {
    advanceRotors();
      last = &c8;
      m.append(String(c8.next()));
      break;
    }

    if (last != &c8) {
    advanceRotors();
      last->reset();
      last = &c8;
      m.append(String(c8.next()));
      break;
    }

    m.edit(c8.next());
    
    break;
  case '9':
    if (last == NULL) {
    advanceRotors();
      last = &c9;
      m.append(String(c9.next()));
      break;
    }

    if (last != &c9) {
    advanceRotors();
      last->reset();
      last = &c9;
      m.append(String(c9.next()));
      break;
    }

    m.edit(c9.next());
    
    break;
    
  case '0':
    if (last == NULL) {
    advanceRotors();
      last = &c0;
      m.append(String(c0.next()));
      break;
    }

    if (last != &c0) {
    advanceRotors();
      last->reset();
      last = &c0;
      m.append(String(c0.next()));
      break;
    }

    m.edit(c0.next());
    
    break;
  case '*':
    if (last != NULL) {
      last->reset();
      last = NULL;
    }

    if (!m.isEmpty()) {
      retRotors();
    }
    
    m.clip();
    break;
  case '#':
    if (last != NULL) {
      last->reset();
    }
    last = NULL;

    if (m.isEmpty()) {
      break;
    }
    
    Serial.print("0,");
    Serial.print(conf1);
    Serial.print(conf2);
    Serial.print(conf3);
    Serial.print("-");
    Serial.print(rotorToChar(m.r1));
    Serial.print(rotorToChar(m.r2));
    Serial.println(rotorToChar(m.r3));
    
    Serial.print("1,"); 
    Serial.println(m.contents);
    m.clear();
    break;
  default:
    m.append(String(key));
    break;
  }
  
  m.drawLast();
  
  if (pressedRotMode && digitalRead(rotMode) == 1) {
      pressedRotMode = false;
      
      if (deltaTimeRev > 200) {
        revToggle = !revToggle;
      } else {
        rotorControlModeIncrement = !rotorControlModeIncrement;  
      }
      deltaTimeRev = 0;
      printRotors();
      
  } else if (digitalRead(rotMode) == 0) {
      pressedRotMode = true;
      deltaTimeRev += loopDelay;
  }
  
  // increment rotor 1
  if (pressedInc1 && digitalRead(incRot1) == 1) {
      pressedInc1 = false;
      if (revToggle) {
        if (rotorControlModeIncrement) {
          rot1 = (rot1 + 1) % rotorPositions;
        } else {
          rot1 = (rot1 - 1) % rotorPositions;
          while (rot1 < 0) {
            rot1 += rotorPositions;
          }
        }
      } else {
        if (rotorControlModeIncrement) {
          conf1 = (conf1 + 1) % revPos;
        } else {
          conf1 = (conf1 - 1) % revPos;
          while (conf1 < 0) {
            conf1 += revPos;
          }
        }
      }
      printRotors();
  } else if (digitalRead(incRot1) == 0) {
      pressedInc1 = true;
  }

  // increment rotor 2
  if (pressedInc2 && digitalRead(incRot2) == 1) {
      pressedInc2 = false;

      if (revToggle) {
        if (rotorControlModeIncrement) {
          rot2 = (rot2 + 1) % rotorPositions;
        } else {
          rot2 = (rot2 - 1) % rotorPositions;
          while (rot2 < 0) {
            rot2 += rotorPositions;
          }
        }
      } else {
        if (rotorControlModeIncrement) {
          conf2 = (conf2 + 1) % revPos;
        } else {
          conf2 = (conf2 - 1) % revPos;
          while (conf2 < 0) {
            conf2 += revPos;
          }
        }
      }
      printRotors();
  } else if (digitalRead(incRot2) == 0) {
      pressedInc2 = true;
  }
  
  // increment rotor 3
  if (pressedInc3 && digitalRead(incRot3) == 1) {
      pressedInc3 = false;
      if (revToggle) {
        if (rotorControlModeIncrement) {
          rot3 = (rot3 + 1) % rotorPositions;
        } else {
          rot3 = (rot3 - 1) % rotorPositions;
          while (rot3 < 0) {
            rot3 += rotorPositions;
          }
        }
      } else {
        if (rotorControlModeIncrement) {
          conf3 = (conf3 + 1) % revPos;
        } else {
          conf3 = (conf3 - 1) % revPos;
          while (conf3 < 0) {
            conf3 += revPos;
          }
        }
      }
      printRotors();
  } else if (digitalRead(incRot3) == 0) {
      pressedInc3 = true;
  }

  delay(loopDelay);
}
