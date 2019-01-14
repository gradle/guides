#include <cassert>
#include <iostream>

#include "greeter.hpp"

void testNameLength() {
  Greeter g("GradleUser");
  std::cout << "[test] returns the correct name length..." << std::flush;
  assert(g.getNameLength() == 10);
  std::cout << " pass" << std::endl;
}

int main(int argc, char **argv) {
  testNameLength();
  return 0;
}
