package guide

import org.gradle.guides.test.fixtures.AbstractSamplesFunctionalTest
import spock.lang.Unroll


class WorkerAPISamplesFunctionalTest extends AbstractSamplesFunctionalTest {

    @Unroll
    def "#sampleProject project creates MD5 hashes"() {
        given:
        copySampleCode(sampleProject)

        when:
        def result = succeeds("md5", "--stacktrace")

        then:
        println result.output
        file("build/md5/einstein.txt.md5").text == "811c80b182fc25f3a492bd0717b5743f"
        file("build/md5/feynman.txt.md5").text == "b90d2a906b4927daba5ba35141b27b9f"
        file("build/md5/oppenheimer.txt.md5").text == "4a0e4517830b2e196df12f89ff28595d"

        where:
        sampleProject << sampleProjects()
    }

    private static List<String> sampleProjects() {
        samplesCodeDir.listFiles().findAll { it.isDirectory() }.collect { it.name }
    }
}
