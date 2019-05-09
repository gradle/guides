file(".mrconfig").readLines().filter { it.startsWith("[") && it.endsWith("]") }.map { it.substring(1, it.length-1) }.forEach {
    settings.includeBuild(it)
}