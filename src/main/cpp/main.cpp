#include <iostream>  // <1>
#include "greeting.hpp" // <2>

int main(int argc, char** argv, char** env) {
    std::cout << greeting << std::endl;
    return 0;
}
