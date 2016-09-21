# Gradle Guides [![Build Status](https://travis-ci.org/gradle/guides.svg?branch=master)](https://travis-ci.org/gradle/guides)

## Introduction

This repository contains the source for the Gradle Guides homepage at https://guides.gradle.org.

Each individual Gradle Guide is backed by its own repository under the [gradle-guides](https://github.com/gradle-guides) GitHub organization. For example, the _Building JVM Libraries_ guide hosted at https://guides.gradle.org/gs-building-jvm-libraries is backed by the repository at https://github.com/gradle-guides/gs-building-jvm-libraries.

Proposals for new guides are managed here, in the [issues](https://github.com/gradle/guides/issues) associated with this repository. The process for submitting these proposals is documented in [_Getting Started Writing Getting Started Guides_](https://guides.gradle.org/gs-writing-gs-guides).

This README is written primarily for the [Gradle Guides Team](https://github.com/orgs/gradle-guides/people), and the sections that follow explain:

 - [How to build and serve all guides locally](#how-to-build-and-serve-all-guides-locally)
 - [How to handle proposals for new guides](#how-to-handle-proposals-for-new-guides)


### A technical note on publication to GitHub Pages

The Gradle Guides homepage at guides.gradle.org is hosted on [GitHub Pages](https://pages.github.com/) via what's known as an [Organization Page](https://help.github.com/articles/user-organization-and-project-pages/). The GitHub repositories where Organization Pages live have naming constraints, and in our case the repository where the guides.gradle.org Organization Page lives is [gradle-guides/gradle-guides.github.io](https://github.com/gradle-guides/gradle-guides.github.io). That repository is a _mirror_ of this repository, meaning that every time a commit is pushed to this repository (gradle/guides), that same commit is also pushed to gradle-guides/gradle-guides.github.io, which in turn triggers publication to GitHub Pages. The mirroring process is managed as a simple Travis CI build; see [.travis.yml](.travis.yml) for details. We set up this mirrored arrangement because "gradle-guides/gradle-guides.github.io" is cumbersome to type and talk about, while "gradle/guides" is a friendlier and more intention-revealing name. The mirroring process should be transparent and hands-free for maintainers. We mention it here for reference and clarity.


## How to build and serve all guides locally

The following commands will check out this repository and all individual guide repositories into a single directory hierarchy. It will then generate HTML for each of the individual guides and start an nginx process to serve them all. This approach closely mirrors how GitHub Pages serves content.

> NOTE: _You will need to install the `mr` and `nginx` utilities for the following commands to work. Your favorite package manager should have both._

    git clone https://github.com/gradle/guides.git
    cd guides
    mr checkout
    mr run ./gradlew build
    ./nginx.sh
    open http://localhost:8080

When complete, http://localhost:8080 should mirror what's available at https://guides.gradle.org.


## How to handle proposals for new guides

We encourage Gradle users to write their own Getting Started Guides and to submit them for inclusion on the Gradle Guides site. To make the process as simple as possible, we set up the guide repository infrastructure for them. What follows is the process to follow when a user submits a proposal per the instructions in [_Writing Getting Started Guides_](https://guides.gradle.org/gs-writing-gs-guides/).


### Agree on a topic and scope for the guide

New proposals are submitted as GitHub issues here in the [gradle/guides](https://github.com/gradle/guides/issues) repository. Follow through on each proposal by discussing whether the guide is appropriate, what its title should be, and the extent of its scope. Prefer keeping as much of the discussion as possible in the GitHub issue to keep the process open.

In the (hopefully rare) case of rejecting a guide proposal, provide your reasoning in a comment and close the issue.

Otherwise, once you have agreed on the title and other details above, you can move on to the next step.


### Create a new repository for the guide

Once agreement has been reached, it's time to create a repository for the guide. Follow these steps on your local machine, replacing `<new-guide>` with a name that matches the agreed guide title:

    git clone https://github.com/gradle-guides/gs-writing-gs-guides.git gs-new-guide
    cd gs-new-guide
    rm -rf .git
    git init
    git add .
    git commit -m "Initialize repository from template"

When naming the repository, ensure that the `gs-` prefix remains. For example, `gs-writing-plugins` would be a suitable repository name for a guide titled _Getting Started Writing Gradle Plugins_, and `gs-fat-jars` would work for a guide titled _Getting Started Working with Fat JARs_.


### Update the skeleton project

You need to make various changes to some of the files in the project so that it's ready for consumption by the guide author:

 - Update the `repoPath` variable in `build.gradle` so that it reflects the name of the new repository
 - Replace the contents of `README.adoc` with the agreed title of the guide plus the standard section headings
   - What you'll build
   - What you'll need
   - Summary
   - Next steps

Commit these changes once you're happy with them.


### Host the guide repository on GitHub

Create a new repository under the _gradle-guides_ organisation, taking the following into account:

 - Give the repository the same name as you used in the first steps above, e.g. `gs-writing-plugins`
 - When prompted, do not add a README or any other files to the repository

When finished, you should have a new, empty repository at `https://github.com/gradle-guides/gs-new-guide`, where `<gs-new-guide>` matches the name of the local repo you created.

Now push the contents of your local Git repository to the new remote repository with these commands, again replacing `<new-guide>` as appropriate:

    git remote add origin https://github.com/gradle-guides/gs-new-guide
    git push --set-upstream origin master

Once this has been done, _paste the link for this new repository on the guide's GitHub issue_ and let the author know he has everything he needs to begin writing.

The author will then following the remaining steps laid out in _Writing Getting Started Guides_, and will ultimately submit a Pull Request against the repository. When this happens, provide your feedback in the form of a [GitHub Review](https://help.github.com/articles/reviewing-changes-in-pull-requests/) and/or [additional commits on the author's Pull Request branch](https://help.github.com/articles/committing-changes-to-a-pull-request-branch-created-from-a-fork/), whichever you deem to be a most efficient use of both your time.
