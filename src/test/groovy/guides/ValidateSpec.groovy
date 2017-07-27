package guides

import org.gradle.guides.test.fixtures.AbstractSamplesFunctionalTest
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder

class ValidateSpec extends AbstractSamplesFunctionalTest {

    static final File SRC_CODE_DIR   = new File( System.getProperty('samplesDir') ?: 'samples', 'code' )
    static final File SRC_OUTPUT_DIR = new File( System.getProperty('samplesDir') ?: 'samples', 'output' )

    @Rule TemporaryFolder temporaryFolder = new TemporaryFolder()
    File workingDir

    void setup() {
        initialize()
    }

    def 'Ensure generated content is correct'() {
        when:
        String output = runInit()
        List<String> gradleLines = filteredLines(testDirectory,'build.gradle')
        List<String> expectedGradleLines = filteredLines(SRC_CODE_DIR,'build.gradle')
        List<String> javaAppLines = filteredLines(testDirectory,'src/main/java/App.java')
        List<String> expectedJavaAppLines = filteredLines(SRC_CODE_DIR,'src/main/java/App.java')
        List<String> javaTestAppLines = filteredLines(testDirectory,'src/test/java/AppTest.java')
        List<String> expectedJavaTestAppLines = filteredLines(SRC_CODE_DIR,'src/test/java/AppTest.java')

        then:
        output.contains( expectedOutput('init.txt') )

        and:
        hasFile 'build.gradle'
        hasFile 'settings.gradle'
        hasFile 'gradle/wrapper/gradle-wrapper.jar'
        hasFile 'gradle/wrapper/gradle-wrapper.properties'
        hasFile 'gradlew'
        hasFile 'gradlew.bat'
        hasFile 'src/main/java/App.java'
        hasFile 'src/test/java/AppTest.java'

        and:
        !gradleLines.empty
        !expectedGradleLines.empty
        gradleLines.containsAll(expectedGradleLines)

        and:
        !javaAppLines.empty
        !expectedJavaAppLines.empty
        javaAppLines.containsAll(expectedJavaAppLines)

        and:
        !javaTestAppLines.empty
        !expectedJavaTestAppLines.empty
        javaTestAppLines.containsAll(expectedJavaTestAppLines)
    }

    def 'Run gradle with "build, run"'() {

        setup:
        runInit()

        when:
        String output = runGradle 'build'

        then:
        output.contains( new File(SRC_OUTPUT_DIR,'build.txt').text )

        when:
        output = runGradle 'run'

        then:
        output.contains( new File(SRC_OUTPUT_DIR,'run.txt').text )

    }

    private String expectedOutput(final String relativePath) {
        new File(SRC_OUTPUT_DIR,relativePath).text
    }

    private List<String> filteredLines(final File baseDir, final String relativePath) {
        List<String> generated = new File(testDirectory,relativePath).readLines().collect { String line ->
            line.replaceAll( ~/\s+\/\/\s+<\d>\s+/,'').
                replaceAll( ~/^\s+/,'' ).
                replaceAll( ~/^\/\/.+/,'' )
        }.findAll { String line ->
            !line.empty
        }
    }

    private String runGradle(String... args) {
        succeeds(args).output.replaceAll( ~/Download.+?\n/,'')
    }

    private String runInit() {
        runGradle 'init', '--type', 'java-application'
    }

    private boolean hasFile(final String relativePath) {
        new File(testDirectory,relativePath).exists()
    }
}
