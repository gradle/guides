/*
 * Copyright 2020 the original author or authors.
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

package org.gradle.docs.internal.components;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.docs.guides.internal.GuideContentBinary;
import org.gradle.docs.samples.internal.SampleContentBinary;

import javax.inject.Inject;

public abstract class TestableAsciidoctorContentComponent extends NamedDocumentationComponent {
    @Inject
    public TestableAsciidoctorContentComponent(String name) {
        super(name);
    }

    /**
     * Linked to {@link SampleContentBinary#getInstalledIndexPageFile()} or {@link GuideContentBinary#getInstalledIndexPageFile()}
     */
    public abstract RegularFileProperty getContentFile();

    /**
     * Linked to {@link DocumentationArchiveComponent#getInstallDirectory()} or null if no starting sample should be used
     */
    public abstract DirectoryProperty getStartingSampleDirectory();
}
