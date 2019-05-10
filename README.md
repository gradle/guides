# Gradle Guides [![Build Status](https://travis-ci.org/gradle/guides.svg?branch=master)](https://travis-ci.org/gradle/guides)

## Introduction

This repository contains the source for the Gradle Guides homepage at https://guides.gradle.org.

Each individual Gradle guide is backed by its own repository under the [gradle-guides](https://github.com/gradle-guides) GitHub organization. For example, you can find the source of the _Building Java Libraries_ guide—hosted at [https://guides.gradle.org/building-java-libraries/](https://guides.gradle.org/building-java-libraries/)—in the [gradle-guides/building-java-libraries](https://github.com/gradle-guides/building-java-libraries) repository.

### Are you interested in contributing a guide?

We want Gradle Guides to be a community project, so we encourage anyone that is interested in writing a guide to submit a proposal as an [issue](https://github.com/gradle/guides/issues) in this repository. Doing so triggers a discussion and review process with the Gradle Guides team that is designed to help you produce a high-quality guide. You can learn more about that process in [_Getting Started Writing Gradle Guides_](https://guides.gradle.org/writing-getting-started-guides/) and about the style we use in our [_Style Guide_](https://guides.gradle.org/style-guide/).

The rest of the README is written primarily for the [project team](https://github.com/orgs/gradle-guides/people), but you're welcome to read on if you want to learn what happens behind the scenes.

## Handling proposals for new guides

As mentioned in the previous section, we would like Gradle users to contribute their own guides to the Gradle Guides site. To make the process as simple as possible, we set up the infrastructure needed by each guide and aid in the development of the guide's content. What follows is the process the Gradle Guides team should follow when a user submits a proposal, per the instructions in [_Getting Started Writing Gradle Guides_](https://guides.gradle.org/writing-getting-started-guides/).


### Agree on a topic and scope for the guide

The first step in processing a new guide proposal is to work with the submitter to determine whether the proposed guide is appropriate and to decide on the exact topic, what form the guide should take, and how much it should cover. You should initiate and continue this discussion as much as possible on the proposal's GitHub issue. This ensures that the discussion is public.

In the (hopefully rare) case of rejecting a guide proposal, provide your reasoning in an issue comment—keeping it as clear and neutral as possible—and close the issue.

Otherwise, once you have agreed on the title and other details above, move on to the next step of creating a GitHub repository for the new guide.

### Create a new repository for the guide

Each guide gets its own GitHub repository under the gradle-guides organization, so you will have to create one for each accepted proposal. A helper Gradle script, exists in the `create-guides` folder of the `gradle/guides` repository to aid in this process.

**NOTE:** In order for this to work a GitHub personal authentication token must be supplied. The token must have `repo` scope. It is passed as a project property called `gradle.guides.authToken`. This can be setup in the `gradle.properties` file or supplied via the command line using `-Pgradle.guides.authToken=<GITHUB-TOKEN>`.

You might want to consider adding two more (optional) properties:

- `gradle.guides.user.email` - Set this to your email address at `@gradle.com`.
- `gradle.guides.user.signingkey` - Set this to your GPG key for signing commits.

Once again these can be set in `gradle.properties` or supplied on the command-line.

Follow these steps on your local machine, replacing `Creating Java Projects` with the agreed-upon title for the guide, and replacing `49` with the number of the issue created in the step above:

    git clone https://github.com/gradle/guides.git
    cd guides/create-guides
    # Use getting-started OR topical as the guide type
    # The issue must be in the GitHub markup format organization/repository#number
    ./gradlew create --guide-type getting-started --guide-name 'Creating Java Projects' --guide-issue 'gradle/guides#49' --guide-repo-name 'creating-java-projects'

    # The command above will create a directory named `build/new-guide-repositories/creating-java-projects`
    cd build/new-guide-repositories/creating-java-projects

In its current format the Gradle `create` task will take care of creating the repository on GitHub, cloning the appropriate template for the guide type, substituting appropriate values and finally pushing the content upstream. It will also take care of enabling the repository to be built by Travis.

Once this has been done, _paste the link for this new repository into the guide's GitHub issue_ and let the author know that they have everything they need to begin writing.

The author will then follow the remaining steps laid out in _Getting Started Writing Gradle Guides_, and will ultimately submit a pull request against the repository. When this happens, provide your feedback in the form of a [GitHub Review](https://help.github.com/articles/reviewing-changes-in-pull-requests/) and/or [additional commits on the author's pull request branch](https://help.github.com/articles/committing-changes-to-a-pull-request-branch-created-from-a-fork/)—whichever you deem to be the most efficient use of both your time.

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

## FAQ

### How do I build and serve all guides locally?

The following commands will check out this repository and all individual guide repositories into a single directory hierarchy. It will then generate HTML for each of the individual guides and start an nginx process to serve them all. This approach closely mirrors how GitHub Pages serves content.

> NOTE: _You will need to install the [`mr`](https://myrepos.branchable.com/) and [`nginx`](https://nginx.org/en/) utilities for the following commands to work. Your favorite package manager should have both._

    git clone https://github.com/gradle/guides.git
    cd guides
    mr checkout
    mr run ./gradlew build
    ./nginx.sh
    open http://localhost:8080

When complete, http://localhost:8080 should list directories that you can click to serve guides locally. For example, click on the `building-android-apps` directory to serve the _Building Android Apps_ guide.

### How is the Gradle Guides site published?

The Gradle Guides homepage at guides.gradle.org is hosted on [GitHub Pages](https://pages.github.com/) via what's known as an [Organization Page](https://help.github.com/articles/user-organization-and-project-pages/).

The GitHub repositories where Organization Pages live have naming constraints, and in our case the repository where the guides.gradle.org Organization Page lives is [gradle-guides/gradle-guides.github.io](https://github.com/gradle-guides/gradle-guides.github.io). That repository is a _mirror_ of this one—gradle/guides—meaning that every commit here is also pushed to gradle-guides/gradle-guides.github.io, which in turn triggers publication to GitHub Pages.

The mirroring process is managed as a simple Travis CI build; see [.travis.yml](.travis.yml) for details. We set up this mirrored arrangement because "gradle-guides/gradle-guides.github.io" is cumbersome to type and talk about, while "gradle/guides" is a friendlier and more intention-revealing name. The mirroring process should be transparent and hands-free for maintainers. We mention it here for reference and clarity.

### How do I test a change to a guide?

Each guide is tested on Travis. This is triggered when you create a PR for the guide repo.

## Other resources

- Getting start guides are created using this template repo: https://github.com/gradle-guides/gs-template
- Topical guides are created using this template repo: https://github.com/gradle-guides/topical-template
- Tutorials are created using this template repo: https://github.com/gradle-guides/tutorial-template
- Each guide uses the plugins from https://github.com/gradle-guides/gradle-guides-plugin
