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

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project

/** PLugin that is used to test JVM code snippets.
 *
 * @since 0.1
 */
@CompileStatic
class TestJvmCodePlugin implements Plugin<Project> {
    void apply(Project project) {
        project.apply plugin : 'groovy'
        project.apply plugin : BasePlugin

        setJDKVersion(project)
        addDependencies(project)
    }

    @CompileDynamic
    void setJDKVersion(Project project) {
        project.sourceCompatibility = 1.7
        project.targetCompatibility = 1.7
    }

    @CompileDynamic
    void addDependencies(Project project) {
        def spockGroovyVer = GroovySystem.version.replaceAll(/\.\d+$/,'')

        project.dependencies {
            testCompile localGroovy()
            testCompile ("org.spockframework:spock-core:1.0-groovy-${spockGroovyVer}") {
                exclude module : 'groovy-all'
            }
            testCompile 'commons-io:commons-io:2.5'
            testCompile gradleTestKit()
        }
    }
}
