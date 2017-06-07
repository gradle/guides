//
// ============================================================================
// (C) Copyright Schalk W. Cronje 2017
//
// This software is licensed under the Apache License 2.0
// See http://www.apache.org/licenses/LICENSE-2.0 for license details
//
// Unless required by applicable law or agreed to in writing, software distributed under the License is
// distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and limitations under the License.
//
// ============================================================================
//

package org.gradle.guides

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Unroll


class TestJvmCodePluginSpec extends Specification {
    Project project = ProjectBuilder.builder().build()

    def 'Applying plugin should not throw exception'() {
        when:
        project.apply plugin : 'org.gradle.guides.test-jvm-code'

        then:
        noExceptionThrown()
    }

    @Unroll
    def 'Applying plugin should not throw exception after a #pluginName plugin has been applied'() {
        when:
        project.apply plugin : "org.gradle.guides.${pluginName}"
        project.apply plugin : 'org.gradle.guides.test-jvm-code'

        then:
        noExceptionThrown()

        where:
        pluginName << [ 'getting-started', 'topical', 'tutorial']
    }

}