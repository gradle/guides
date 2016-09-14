# guides.gradle.org [![Build Status](https://travis-ci.org/gradle/guides.svg?branch=master)](https://travis-ci.org/gradle/guides)

To build and serve all guides locally, run the following:

    git clone https://github.com/gradle-guides/gradle-guides.github.io
    cd gradle-guides.github.io
    mr checkout
    mr run ./gradlew build
    ./nginx.sh
    open http://localhost:8080

When complete, http://localhost:8080 should mirror what's available at http://guides.gradle.org.
