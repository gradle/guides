import Generate

tasks.register<Generate>("generate") {
    fileCount = 2
    content = "Hello World!"
    generatedFileDir = file("$buildDir/generated-output")
}
