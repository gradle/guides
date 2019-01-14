#include "greeter.h"

#include <cassert>
#include <iostream>

void TestNameLength() {
  Greeter g("GradleUser");
  std::cout << "[test] returns the correct name length..." << std::flush;
  assert(g.GetNameLength() == 10);
  std::cout << " pass" << std::endl;
}

int main(int argc, char** argv) {
  TestNameLength();
  return 0;
}
