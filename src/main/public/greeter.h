#ifndef GREETER_H_
#define GREETER_H_

#include <string>
#include <algorithm>

#if defined(DLL_EXPORT)
#  define DECLSPEC __declspec(dllexport)
#else
#  define DECLSPEC
#endif  // defined(DLL_EXPORT)

class DECLSPEC Greeter {
  public:
    explicit Greeter(const std::string& name) : name_(name) {};
    Greeter() : name_("World") {};
    void Greet();
    int GetNameLength();
  private:
    std::string name_;
};

#endif  // GREETER_H_
