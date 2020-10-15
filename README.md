# Gradle Guides 

## Introduction

This repository contains the source for the Gradle Guides homepage at https://guides.gradle.org.

Each individual Gradle guide can be found in the `subprojects` directory. 

This repository also contains some Gradle plugins for writing and testing guides and sample builds. These can be found in
the `subprojects/gradle-guides-plugin` directory.

### Are you interested in contributing a guide?

We want Gradle Guides to be a community project, so we encourage anyone that is interested in writing a guide to submit a proposal as an [issue](https://github.com/gradle/guides/issues) in this repository. Doing so triggers a discussion and review process with the Gradle Guides team that is designed to help you produce a high-quality guide. You can learn more about that process in the following sections.

## Authoring a guide

Each guide uses the plugins from the `subprojects/gradle-guides-plugin` project. These plugins add tasks to
build and test the guide and any associated samples.
See the [README](subprojects/gradle-guides-plugin/README.adoc) for details.

### How do I test a change to a guide?

Please run `./gradlew -p subprojects/<guide> build` to test the guide before submitting PR.

The guides are also tested on [TeamCity](https://builds.gradle.org/project/DocumentationPortal_Guides?branch=&mode=builds#all-projects).

### Adding multi-language samples and text

Gradle Guides have the ability to display different content based on the user's preferred Gradle DSL syntax: Groovy (default) or Kotlin.

If you are authoring a guide, you can take advantage of this by adding:

```asciidoc
[.multi-language-text.lang-groovy]
```

where "groovy" or "kotlin" are the acceptable languages. Each guide will use JavaScript to show/hide the appropriate content based on DSL selection.

Code examples should be handled like this:

```asciidoc
[source.multi-language-sample,groovy]
.build.gradle
----
include::{samplescodedir}/foo/build.gradle
----
[source.multi-language-sample,kotlin]
.build.gradle.kts
----
include::{samplescodedir}/foo/build.gradle.kts
----
```

This will combine the Groovy and Kotlin samples into 1 block and provide tabs so a user can select their preferred DSL. **Note:** when a language is selected, it applies changes to all parts of guides and saves the preference for later.

#### Multi-file and multi-language samples

The following shows how to use asciidoc nested blocks to allow multi-file samples. This nesting ensures that the files are grouped properly and shown together when they're displayed.

```adoc
=== Example: Configuring arbitrary objects using a script

[.multi-language-sample,groovy]
====
.build.gradle
[source,groovy]
----
include::{samplesPath}/configureObjectUsingScript/build.gradle[]
----

.other.gradle
[source,groovy]
----
include::{samplesPath}/configureObjectUsingScript/other.gradle[]
----
====

.Output of `gradle -q configure`
----
> gradle -q configure
include::{samplesPath}/configureObjectUsingScript/configureObjectUsingScript.out[]
----
```

## Handling proposals for new guides

The rest of this README is written primarily for the project team, but you're welcome to read on if you want to learn what happens behind the scenes.

As mentioned in the introduction, we would like Gradle users to contribute their own guides to the Gradle Guides site. To make the process as simple as possible, we set up the infrastructure needed by each guide and aid in the development of the guide's content. What follows is the process the Gradle Guides team should follow when a user submits a proposal.

### Agree on a topic and scope for the guide

The first step in processing a new guide proposal is to work with the submitter to determine whether the proposed guide is appropriate and to decide on the exact topic, what form the guide should take, and how much it should cover. You should initiate and continue this discussion as much as possible on the proposal's GitHub issue. This ensures that the discussion is public.

In the (hopefully rare) case of rejecting a guide proposal, provide your reasoning in an issue comment—keeping it as clear and neutral as possible—and close the issue.

Otherwise, once you have agreed on the title and other details above, move on to the next step of creating a GitHub repository for the new guide.

### Create a new project for the guide

Each guide gets its project under the `subprojects` directory, so you will have to create one for each accepted proposal.
Once this has been done, _paste the link for this new repository into the guide's GitHub issue_ and let the author know that they have everything they need to begin writing.

TODO - add detail here

The author will then write and test the guide content, and will ultimately submit a pull request against this repository. When this happens, provide your feedback in the form of a [GitHub Review](https://help.github.com/articles/reviewing-changes-in-pull-requests/) and/or [additional commits on the author's pull request branch](https://help.github.com/articles/committing-changes-to-a-pull-request-branch-created-from-a-fork/)—whichever you deem to be the most efficient use of both your time.
