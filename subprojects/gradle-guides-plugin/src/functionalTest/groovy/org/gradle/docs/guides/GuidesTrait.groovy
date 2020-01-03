package org.gradle.docs.guides

trait GuidesTrait {
    static String createGuide(String name) {
        return """
            documentation.guides.publishedGuides.create('${name}')
        """
    }

    static String guideDsl(String name) {
        return "documentation.guides.publishedGuides.${name}"
    }
}