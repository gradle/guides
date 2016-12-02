# Gradle Guides [![Build Status](https://travis-ci.org/gradle/guides.svg?branch=master)](https://travis-ci.org/gradle/guides)

## Introduction

This repository contains the source for the Gradle Guides homepage at https://guides.gradle.org.

Each individual Gradle Guide is backed by its own repository under the [gradle-guides](https://github.com/gradle-guides) GitHub organization. For example, you can find the source of the _Building JVM Libraries_ guide—hosted at https://guides.gradle.org/gs-building-jvm-libraries—in the [gradle-guides/gs-building-jvm-libraries](https://github.com/gradle-guides/gs-building-jvm-libraries) repository.

### Are you interested in contributing a guide?

We want Gradle Guides to be a community project, so we encourage anyone that is interested in writing a guide to submit a proposal as an [issue](https://github.com/gradle/guides/issues) in this repository. This triggers a discussion and review process with the Gradle Guides Team that is designed to help you produce a high-quality guide. You can learn more about that process in [_How to Write Gradle Guides_](https://guides.gradle.org/gs-writing-gs-guides).

The rest of the README is written primarily for the [Gradle Guides Team](https://github.com/orgs/gradle-guides/people), but you're welcome to read on if you want to learn what happens behind the scenes.

## Handling proposals for new guides

As mentioned in the previous section, we would like Gradle users to contribute their own guides to the Gradle Guides site. To make the process as simple as possible, we set up the infrastructure needed by each guide and aid in the development of the guide's content. What follows is the process the Gradle Guides Team should follow when a user submits a proposal, per the instructions in [_How to Write Gradle Guides_](https://guides.gradle.org/gs-writing-gs-guides/).


### Agree on a topic and scope for the guide

The first step in processing a new guide proposal is to work with the submitter to determine whether the proposed guide is appropriate and decide on the exact topic, what form the guide should take, and how much it should cover. You should initiate and continue this discussion on the GitHub issue representing the proposal as much as possible. This ensures that the discussion is public.

In the (hopefully rare) case of rejecting a guide proposal, provide your reasoning in an issue comment—keeping it as clear and neutral as possible—and close the issue.

Otherwise, once you have agreed on the title and other details above, move on to the next step of creating a GitHub repository for the new guide.


### Create a new repository for the guide

Each guide gets its own GitHub repository under the gradle-guides organization, so you will have to create one for each accepted proposal. You should start by cloning an existing repository that has the required structure.

Follow these steps on your local machine, replacing `<new-guide>` with a name that matches the agreed guide title:

    git clone https://github.com/gradle-guides/gs-writing-gs-guides.git new-guide
    cd new-guide
    rm -rf .git
    git init
    git add .
    git commit -m "Initialize repository from template"

When naming the repository, ensure that the `gs-` prefix remains for Getting Started Guides. Other types of guide require no specific prefix. For example:

 - `gs-writing-plugins` for a guide titled _Getting Started Writing Gradle Plugins_
 - `gs-fat-jars` for a guide titled _Getting Started Working with Fat JARs_
 - `maven-migration` or `migrating-from-maven` for a guide titled _Migrating to Gradle from Maven_


### Update the skeleton project

You need to make various changes to some of the files in the project so that it's ready for consumption by the guide author:

 - Update the `repoPath` variable in `build.gradle` so that it reflects the name of the new repository
 - Replace the contents of `README.adoc` with the agreed title of the guide plus any standard section headings applicable to the particular type of guide

Commit these changes once you're happy with them.


### Host the guide repository on GitHub

Create a new repository under the _gradle-guides_ organisation, taking the following into account:

 - Give the repository the same name as you used in the first steps above, e.g. `gs-writing-plugins`
 - When prompted, do not add a README or any other files to the repository

When finished, you should have a new, empty repository at `https://github.com/gradle-guides/gs-new-guide`, where `<gs-new-guide>` matches the name of the local repo you created.

Now push the contents of your local Git repository to the new remote repository with these commands, again replacing `<new-guide>` as appropriate:

    git remote add origin https://github.com/gradle-guides/new-guide
    git push --set-upstream origin master

Once this has been done, _paste the link for this new repository on the guide's GitHub issue_ and let the author know they have everything they need to begin writing.

The author will then following the remaining steps laid out in _How to Write Gradle Guides_, and will ultimately submit a pull request against the repository. When this happens, provide your feedback in the form of a [GitHub Review](https://help.github.com/articles/reviewing-changes-in-pull-requests/) and/or [additional commits on the author's pull request branch](https://help.github.com/articles/committing-changes-to-a-pull-request-branch-created-from-a-fork/)—whichever you deem to be the most efficient use of both your time.


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


