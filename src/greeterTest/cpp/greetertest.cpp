#include <gtest/gtest.h>
#include "greeter.hpp"

TEST(GreeterTest, CheckCapitalisation) {
    Greeter g("GradleUser");
    EXPECT_EQ(g.getNameLength(), 10);
}

int main(int argc, char **argv) {
  ::testing::InitGoogleTest(&argc, argv);
  return RUN_ALL_TESTS();
}
