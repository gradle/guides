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

package org.gradle.samples

import org.gradle.testkit.runner.BuildResult

import static org.gradle.testkit.runner.TaskOutcome.FROM_CACHE
import static org.gradle.testkit.runner.TaskOutcome.NO_SOURCE
import static org.gradle.testkit.runner.TaskOutcome.SKIPPED
import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE

class IncrementalSamplesFunctionalTest extends AbstractSampleFunctionalTest {
    private static final SKIPPED_TASK_OUTCOMES = [FROM_CACHE, UP_TO_DATE, SKIPPED, NO_SOURCE]

    def "skips sample tasks when no source"() {
        makeSingleProject()

        when:
        def result = build('assemble')

        then:
        assertSampleTasksSkipped(result)
        result.task(':assemble').outcome in SKIPPED_TASK_OUTCOMES
        result.task(':generateSampleIndex').outcome in SKIPPED_TASK_OUTCOMES
        result.task(':asciidocSampleIndex').outcome in SKIPPED_TASK_OUTCOMES
        !groovyDslZipFile.exists()
        !kotlinDslZipFile.exists()
        !new File(projectDir, 'build').exists()
    }

    // TODO: No asciidoc if no source
    // TODO: No groovy zip if no groovy source
    // TODO: No kotlin zip if no kotlin source
    // TODO: Not in index if no source
    // TODO: No change, all up-to-date
    // TODO: Change sample README content, asciidoctor execute and zips execute
    // TODO: Change gradle version, asciidoctor and zips execute
    // TODO: Change groovy script, only groovy script executes
    // TODO: Change kotlin script, only kotlin script executes
    // TODO: Change project version, asciidoctor and zips execute
    // TODO: Add/remove sample, index asciidoctor execute

    private static void assertSampleTasksSkipped(BuildResult result) {
        result.task(":generateWrapperForDemoSample").outcome in SKIPPED_TASK_OUTCOMES
        result.task(":syncDemoGroovyDslSample").outcome in SKIPPED_TASK_OUTCOMES
        result.task(":syncDemoKotlinDslSample").outcome in SKIPPED_TASK_OUTCOMES
        result.task(":compressDemoGroovyDslSample").outcome in SKIPPED_TASK_OUTCOMES
        result.task(":compressDemoKotlinDslSample").outcome in SKIPPED_TASK_OUTCOMES
        result.task(":assembleDemoSample").outcome in SKIPPED_TASK_OUTCOMES
    }
}
