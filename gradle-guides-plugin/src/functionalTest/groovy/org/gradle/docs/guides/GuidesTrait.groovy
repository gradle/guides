package org.gradle.docs.guides

import org.gradle.docs.TestFile

trait GuidesTrait {
    static String createGuide(String name) {
        return """
            documentation.guides.publishedGuides.create('${name}')
        """
    }

    static String guideDsl(String name) {
        return "documentation.guides.publishedGuides.${name}"
    }

    static void writeContentTo(TestFile directory) {
        directory.file('contents/index.adoc') << """
            |= Demo
            |
            |Some guide
            |""".stripMargin()
    }
}