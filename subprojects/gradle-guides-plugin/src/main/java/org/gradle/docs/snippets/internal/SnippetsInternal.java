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

package org.gradle.docs.snippets.internal;

import org.gradle.api.DomainObjectSet;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.docs.snippets.Snippets;

import javax.inject.Inject;

public abstract class SnippetsInternal implements Snippets {
    private final NamedDomainObjectContainer<SnippetInternal> publishedSnippets;
    private final DomainObjectSet<SnippetBinary> binaries;

    @Inject
    public SnippetsInternal(ObjectFactory objectFactory) {
        this.publishedSnippets = objectFactory.domainObjectContainer(SnippetInternal.class, name -> objectFactory.newInstance(SnippetInternal.class, name));
        this.binaries = objectFactory.domainObjectSet(SnippetBinary.class);
    }

    @Override
    public NamedDomainObjectContainer<? extends SnippetInternal> getPublishedSnippets() {
        return publishedSnippets;
    }

    public DomainObjectSet<? super SnippetBinary> getBinaries() {
        return binaries;
    }
}
