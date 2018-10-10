import Generate

tasks.create<Generate>("generate") {
    fileCount = 2
    content = "Hello World!"
    generatedFileDir = file("$buildDir/generated-output")
}
