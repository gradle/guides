#ifndef _GREETER_H_
#define _GREETER_H_

#include <iostream>
#include <string>

class Greeter {
public:
    Greeter(std::string name_) : name(name_) {};
    Greeter() : name("World") {};
    void greet();
private:
    std::string name;
};

#endif
