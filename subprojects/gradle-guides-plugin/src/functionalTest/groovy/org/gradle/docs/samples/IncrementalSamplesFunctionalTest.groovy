/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.docs.samples


import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static org.hamcrest.CoreMatchers.containsString

class IncrementalSamplesFunctionalTest extends AbstractSampleFunctionalSpec {
    def "skips all tasks when no changes"() {
        makeSingleProject()
        writeSampleUnderTest()

        when:
        build("assemble")

        then:
        result.task(':generateSampleIndex').outcome == SUCCESS
        result.task(":generateWrapperForSamples").outcome == SUCCESS
        assertBothDslSampleTasksExecutedAndNotSkipped(result)

        when:
        build("assemble")

        then:
        result.task(':generateSampleIndex').outcome in SKIPPED_TASK_OUTCOMES
        result.task(":generateWrapperForSamples").outcome in SKIPPED_TASK_OUTCOMES
        assertBothDslSampleTasksSkipped(result)
    }

    def "executes generate, install and Zip tasks when README content changes"() {
        makeSingleProject()
        writeSampleUnderTest()

        when:
        build("assemble")

        then:
        assertBothDslSampleTasksExecutedAndNotSkipped(result)

        when:
        build("assemble")

        then:
        assertBothDslSampleTasksSkipped(result)

        when:
        sampleDirectoryUnderTest.file('README.adoc') << "More content\n"
        and:
        build("assemble")

        then:
        result.task(':generateWrapperForSamples').outcome in SKIPPED_TASK_OUTCOMES
        result.task(':generateSampleIndex').outcome in SKIPPED_TASK_OUTCOMES
        assertBothDslSampleTasksExecutedAndNotSkipped(result)
        and:
        def samplePage = file("build/working/samples/docs/sample_demo.adoc")
        samplePage.text.contains("More content")

        when:
        build("assemble")
        then:
        assertBothDslSampleTasksSkipped(result)
    }

    def "change to Groovy content causes only Groovy to be out-of-date"() {
        makeSingleProject()
        writeSampleUnderTest()
        build("assemble")

        when:
        build("assemble")

        then:
        result.task(':generateSampleIndex').outcome in SKIPPED_TASK_OUTCOMES
        result.task(":generateWrapperForSamples").outcome in SKIPPED_TASK_OUTCOMES
        assertBothDslSampleTasksSkipped(result)

        when:
        sampleDirectoryUnderTest.file('groovy/build.gradle') << "// This is a change"
        and:
        build("assemble")
        then:
        result.task(':generateSampleIndex').outcome in SKIPPED_TASK_OUTCOMES
        result.task(":generateWrapperForSamples").outcome in SKIPPED_TASK_OUTCOMES
        assertDslSampleTasksExecutedAndNotSkipped(result, "Groovy")
        assertDslSampleTasksSkipped(result, "Kotlin")
        file("build/sample-zips/sample_demo-groovy-dsl.zip").asZip().assertDescendantHasContent("build.gradle", containsString("// This is a change"))
    }

    def "change to Kotlin content causes only Kotlin to be out-of-date"() {
        makeSingleProject()
        writeSampleUnderTest()
        build("assemble")

        when:
        build("assemble")

        then:
        result.task(':generateSampleIndex').outcome in SKIPPED_TASK_OUTCOMES
        result.task(":generateWrapperForSamples").outcome in SKIPPED_TASK_OUTCOMES
        assertBothDslSampleTasksSkipped(result)

        when:
        sampleDirectoryUnderTest.file('kotlin/build.gradle.kts') << "// This is a change"
        and:
        build("assemble")
        then:
        result.task(':generateSampleIndex').outcome in SKIPPED_TASK_OUTCOMES
        result.task(":generateWrapperForSamples").outcome in SKIPPED_TASK_OUTCOMES
        assertDslSampleTasksExecutedAndNotSkipped(result, "Kotlin")
        assertDslSampleTasksSkipped(result, "Groovy")
        file("build/sample-zips/sample_demo-kotlin-dsl.zip").asZip().assertDescendantHasContent("build.gradle.kts", containsString("// This is a change"))
    }

    def "index is regenerated when sample is added or removed"() {
        makeSingleProject()
        writeSampleUnderTest()
        build("generateSampleIndex")

        when:
        build("generateSampleIndex")

        then:
        result.task(':generateSampleIndex').outcome in SKIPPED_TASK_OUTCOMES

        when:
        writeSampleUnderTest(file('src/docs/samples/new-sample'))
        buildFile << createSampleWithBothDsl('newSample')
        and:
        build("generateSampleIndex")
        then:
        result.task(':generateSampleIndex').outcome == SUCCESS
        def indexFile = file("build/tmp/generateSampleIndex/index.adoc")
        indexFile.text.contains('- <<sample_demo#,Demo>>')
        indexFile.text.contains('- <<sample_new_sample#,New Sample>>')
    }
    // TODO: Output are cached
}
