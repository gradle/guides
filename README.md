# guides.gradle.org [![Build Status](https://travis-ci.org/gradle/guides.svg?branch=master)](https://travis-ci.org/gradle/guides)

To build and serve all guides locally, run the following:

    git clone https://github.com/gradle/guides.git
    cd guides
    mr checkout
    mr run ./gradlew build
    ./nginx.sh
    open http://localhost:8080

When complete, http://localhost:8080 should mirror what's available at http://guides.gradle.org.

## Accepting new guides

We hope to encourage Gradle users to write Getting Started Guides and submit them for inclusion on the Gradle Guides site. To enable that, we will do some of the initial setup for them. What follows is the process once someone has submitted a proposal for a new guide according to the [Getting Started Writing GS Guides guide](http://guides.gradle.org/gs-writing-gs-guides/).

### Agree a topic and scope for the guide

When someone submits a new proposal, they raise an issue in this repository - [gradle/guides](https://github.com/gradle/guides/issues). We need to follow through on that issue and discuss whether the guide is appropriate, what its title should be, and the extent of its scope. As much of the discussion should take place in the GitHub issue as possible to keep the process open.

If the guide is rejected, we should provide our reasoning in the issue as well and then close it. This will hopefully be a rare case and in most circumstances we will have agreed a topic title, in which case one of use can move on to the next step.

### Create a new repository for the guide

Once agreement has been reached, it's time to create a repository for the guide. Follow these steps on your local machine, replacing `<new-guide>` with a name that matches the agreed guide title:

```
git clone https://github.com/gradle-guides/gs-writing-gs-guides.git gs-new-guide
cd gs-your-guide
rm -rf .git
git init
git add .
git commit -m "Initialize repository from template"
```

When naming the repository, ensure that the `gs-` prefix remains. For example, `gs-writing-plugins` would be a suitable repository name for a guide titled "Getting Started Writing Gradle Plugins", and `gs-fat-jars` would work for a guide titled "Getting Started Working with Fat JARs".

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
 - Make sure the repository is _public_, not _private_
 - When prompted, do not add a README or any other files to the repository

When complete, you should have a new, empty repository at https://github.com/gradle-guides/gs-new-guide - with `<gs-new-guide>` matching the name of the local repo you created.

Now push the contents of your local Git repository to the new remote repository with these commands, again replacing `<new-guide>`:


    git remote add origin https://github.com/gradle-guides/gs-new-guide
    git push --set-upstream origin master:master

Once this has been done, _paste the link for this new repository on the guide's GitHub issue_.
