#include "greeter.h"

#include <iostream>

void Greeter::Greet() {
  std::cout << "Hello, " << name_ << ", your name has " << GetNameLength() << " chars." << std::endl;
}

int Greeter::GetNameLength() {
  return name_.length();
}
