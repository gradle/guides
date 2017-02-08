# Gradle Guides [![Build Status](https://travis-ci.org/gradle/guides.svg?branch=master)](https://travis-ci.org/gradle/guides)

## Introduction

This repository contains the source for the Gradle Guides homepage at https://guides.gradle.org.

Each individual Gradle guide is backed by its own repository under the [gradle-guides](https://github.com/gradle-guides) GitHub organization. For example, you can find the source of the _Building JVM Libraries_ guide—hosted at [https://guides.gradle.org/gs-building-jvm-libraries](https://guides.gradle.org/gs-building-jvm-libraries)—in the [gradle-guides/gs-building-jvm-libraries](https://github.com/gradle-guides/gs-building-jvm-libraries) repository.

### Are you interested in contributing a guide?

We want Gradle Guides to be a community project, so we encourage anyone that is interested in writing a guide to submit a proposal as an [issue](https://github.com/gradle/guides/issues) in this repository. Doing so triggers a discussion and review process with the Gradle Guides team that is designed to help you produce a high-quality guide. You can learn more about that process in [_Getting Started Writing Gradle Guides_](https://guides.gradle.org/gs-writing-gs-guides).

The rest of the README is written primarily for the [project team](https://github.com/orgs/gradle-guides/people), but you're welcome to read on if you want to learn what happens behind the scenes.

## Handling proposals for new guides

As mentioned in the previous section, we would like Gradle users to contribute their own guides to the Gradle Guides site. To make the process as simple as possible, we set up the infrastructure needed by each guide and aid in the development of the guide's content. What follows is the process the Gradle Guides team should follow when a user submits a proposal, per the instructions in [_Getting Started Writing Gradle Guides_](https://guides.gradle.org/gs-writing-gs-guides/).


### Agree on a topic and scope for the guide

The first step in processing a new guide proposal is to work with the submitter to determine whether the proposed guide is appropriate and to decide on the exact topic, what form the guide should take, and how much it should cover. You should initiate and continue this discussion as much as possible on the proposal's GitHub issue. This ensures that the discussion is public.

In the (hopefully rare) case of rejecting a guide proposal, provide your reasoning in an issue comment—keeping it as clear and neutral as possible—and close the issue.

Otherwise, once you have agreed on the title and other details above, move on to the next step of creating a GitHub repository for the new guide.

### Create a new repository for the guide

Each guide gets its own GitHub repository under the gradle-guides organization, so you will have to create one for each accepted proposal. A helper script, `create-gs-guides` exists in the root of the gradle/guides repository to aid in this process.

Follow these steps on your local machine, replacing `Creating Java Projects` with the agreed-upon title for the guide, and replacing `49` with the number of the issue created in the step above:

    git clone https://github.com/gradle/guides.git
    cd guides
    ./create-gs-guide 'Creating Java Projects' 49

    # The command above will create a directory named gs-creating-java-projects
    cd gs-creating-java-projects

    # Review the newly-created guide, make sure everything looks correct

    # Create a GitHub repository for the guide (assumes `hub` is installed and aliased to `git`)
    git create gradle-guides/gs-creating-java-projects
    git push --set-upstream origin master


Once this has been done, _paste the link for this new repository into the guide's GitHub issue_ and let the author know that they have everything they need to begin writing.

The author will then following the remaining steps laid out in _Getting Started Writing Gradle Guides_, and will ultimately submit a pull request against the repository. When this happens, provide your feedback in the form of a [GitHub Review](https://help.github.com/articles/reviewing-changes-in-pull-requests/) and/or [additional commits on the author's pull request branch](https://help.github.com/articles/committing-changes-to-a-pull-request-branch-created-from-a-fork/)—whichever you deem to be the most efficient use of both your time.


## FAQ

### How do I build and serve all guides locally?

The following commands will check out this repository and all individual guide repositories into a single directory hierarchy. It will then generate HTML for each of the individual guides and start an nginx process to serve them all. This approach closely mirrors how GitHub Pages serves content.

> NOTE: _You will need to install the `mr` and `nginx` utilities for the following commands to work. Your favorite package manager should have both._

    git clone https://github.com/gradle/guides.git
    cd guides
    mr checkout
    mr run ./gradlew build
    ./nginx.sh
    open http://localhost:8080

When complete, http://localhost:8080 should mirror what's available at https://guides.gradle.org.

### How is the Gradle Guides site published?

The Gradle Guides homepage at guides.gradle.org is hosted on [GitHub Pages](https://pages.github.com/) via what's known as an [Organization Page](https://help.github.com/articles/user-organization-and-project-pages/).

The GitHub repositories where Organization Pages live have naming constraints, and in our case the repository where the guides.gradle.org Organization Page lives is [gradle-guides/gradle-guides.github.io](https://github.com/gradle-guides/gradle-guides.github.io). That repository is a _mirror_ of this one—gradle/guides—meaning that every commit here is also pushed to gradle-guides/gradle-guides.github.io, which in turn triggers publication to GitHub Pages.

The mirroring process is managed as a simple Travis CI build; see [.travis.yml](.travis.yml) for details. We set up this mirrored arrangement because "gradle-guides/gradle-guides.github.io" is cumbersome to type and talk about, while "gradle/guides" is a friendlier and more intention-revealing name. The mirroring process should be transparent and hands-free for maintainers. We mention it here for reference and clarity.
