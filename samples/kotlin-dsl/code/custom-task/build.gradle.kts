import com.company.gradle.binaryrepo.LatestArtifactVersion

tasks.create<LatestArtifactVersion>("latestVersionMavenCentral") {
    coordinates = "commons-lang:commons-lang:1.5"
    serverUrl = "http://repo1.maven.org/maven2/"
}

tasks.create<LatestArtifactVersion>("latestVersionInhouseRepo") {
    coordinates = "commons-lang:commons-lang:2.6"
    serverUrl = "http://my.company.com/maven2"
}
