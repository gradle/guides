package org.gradle.plugins.site.utils

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

class FileUtilsTest extends Specification {

    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    @Unroll
    def "can create directory '#subdir'"() {
        given:
        def dir = new File(temporaryFolder.root, subdir)

        expect:
        !dir.isDirectory()

        when:
        FileUtils.createDirectory(dir)

        then:
        dir.isDirectory()

        where:
        subdir << ['subdir', 'nested/subdir']
    }

    def "can copy file"() {
        given:
        def sourceFile = temporaryFolder.newFile('a.txt')
        def targetFile = new File(temporaryFolder.root, 'b.txt')

        expect:
        sourceFile.isFile()
        !targetFile.isFile()

        when:
        FileUtils.copyFile(sourceFile, targetFile)

        then:
        sourceFile.isFile()
        targetFile.isFile()
    }
}
