import groovy.transform.SourceURI

import java.nio.file.Path
import java.nio.file.Paths

/**
 * Work only with Calibre installed on your computer and ebook-convert.exe on path
 */
class MobiConvert {

    @SourceURI
    URI sourceUri
    Path scriptLocation = Paths.get(sourceUri)
    def wrap = groovy.util.Eval.me(new File("${scriptLocation.parent}${File.separator}cbz-convert.groovy").text)

    def title
    void createFile(def title) {
        this.title = title
        wrap.createFile("temp-"+title)
    }

    void addFile(File file) {
        wrap.addFile(file)
    }

    void addFile(String newFileName, File file) {
        wrap.addFile(newFileName, file)
    }

    void close() {
        wrap.close()

        def ex = (System.properties['os.name'].contains('Windows')) ? '.exe' : ''

        Process process = "ebook-convert${ex} temp-${title}.cbz ${title}.mobi --no-inline-toc --output-profile kindle_pw".execute()
        def out = new StringBuffer()
        def err = new StringBuffer()
        process.consumeProcessOutput( out, err )
        process.waitFor()
        if( out.size() > 0 ) println out
        if( err.size() > 0 ) println err

        new File("temp-${title}.cbz").delete()
        println("Wrote ${title}.mobi")
    }

}

return new MobiConvert()
