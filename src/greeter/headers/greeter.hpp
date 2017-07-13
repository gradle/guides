#ifndef _GREETER_H_
#define _GREETER_H_

#include <string>
#include <algorithm>

#if defined(DLL_EXPORT)
#define DECLSPEC __declspec(dllexport)
#else
#define DECLSPEC
#endif


class DECLSPEC Greeter {
public:
    Greeter(std::string name_) : name(name_) {};
    Greeter() : name("World") {};
    void greet();
    int getNameLength();
private:
    std::string name;
};

#endif
