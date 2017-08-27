package guide

import org.gradle.guides.test.fixtures.AbstractSamplesFunctionalTest
import spock.lang.Unroll


class WorkerAPISamplesFunctionalTest extends AbstractSamplesFunctionalTest {

    @Unroll
    def "#sampleProject project creates MD5 hashes"() {
        given:
        copySampleCode(sampleProject)

        when:
        def result = succeeds("md5")

        then:
        println result.output
        file("build/md5/einstein.txt.md5").exists()
        file("build/md5/feynman.txt.md5").exists()
        file("build/md5/oppenheimer.txt.md5").exists()

        where:
        sampleProject << sampleProjects()
    }

    private static List<String> sampleProjects() {
        samplesCodeDir.listFiles().findAll { it.isDirectory() }.collect { it.name }
    }
}
