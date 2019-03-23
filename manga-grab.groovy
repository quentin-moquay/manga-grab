@Grab("commons-io:commons-io:2.6")
@Grab("commons-cli:commons-cli:1.4")
import javax.net.ssl.*
import org.apache.commons.io.FilenameUtils
import groovy.transform.SourceURI
import java.nio.file.Path
import java.nio.file.Paths

// Consts
int ARG_URL = 0
int ARG_CHAPTER_PADDING = 1
int ARG_PAGE_PADDING = 2
int ARG_CHAPTER_START = 3
int ARG_CHAPTER_END = 4
int ARGS_BEFORE_REQUEST_PROPERTY = 5

String usage = "manga-grab.groovy ([options]) [url] [chapter_padding] [page_padding] [starting_chapter] [ending_chapter] [request_properties]\n"
usage += "\tURL with :chapter: for chapter index and :page: for page index. Do Not provide HTTPS links. Only HTTP.\n"
usage += "\tChapter padding : Digit padding (1, 01, 001, etc.) 1 for 1, 2 for 01 and so on\n"
usage += "\tPage padding : same logic as chapter padding\n"
usage += "\tStarting chapter : Which chapter number to start\n"
usage += "\tEnding chapter : Which chapter number to end\n\n"
usage += "\tRequest Properties : Each Headers you want to send to the downloading HTTP Request\n\n"
usage += "Options :\n\n"
usage += "\t-h or --help : show usages\n\n"
usage += "\t-n or --no-download : Only convert, no download\n\n"
usage += "\t-r {x} or --repack {x} : Repack each chapter to {x} pages (only with convert)\n\n"
usage += "\t-c {x} or --convert {x} : Convert to cbz/pdf \n\n"
usage += "Examples :\n\n"
usage += "\tmanga-grab.groovy 'http://www.funmanga.com/uploads/chapters/15549/:chapter:/p_:page:.jpg' 1 5 0 0\n"
usage += "\tmanga-grab.groovy 'http://images.mangafreak.net/mangas/prison_school/prison_school_:tome:/prison_school_:tome:_:page:.jpg' 1 1 1 1 'Cookie:__cfduid=d0a467a3dbccecddbb1dd9a95d24332191549719778;cf_clearance=af4a3c4d175e3ed83973a1923bfc70f96b44ca1e-1549808750-3600-150'\n"

// Usages
def cli = new CliBuilder(usage: "")
// Create the list of options.
cli.with {
    h longOpt: 'help', 'Show usage information'
    n longOpt: 'no-download', 'no download'
    c(longOpt: 'convert', args: 1, argName: 'x', 'Convert to x format')
    r(longOpt: 'repack', args: 1, argName: 'x', 'Repack each chapter to x pages')
}
def options
try {
    options = cli.parse(args)
} catch (java.lang.IllegalArgumentException e) {

}

// Exclude HTTPS Problems
def nullTrustManager = [
        checkClientTrusted: { chain, authType -> },
        checkServerTrusted: { chain, authType -> },
        getAcceptedIssuers: { null }
]

def nullHostnameVerifier = [
        verify: { hostname, session -> true }
]

SSLContext sc = SSLContext.getInstance("SSL")
sc.init(null, [nullTrustManager as X509TrustManager] as TrustManager[], null)
HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory())
HttpsURLConnection.setDefaultHostnameVerifier(nullHostnameVerifier as HostnameVerifier)

/**
 * Help Working with URL with padding like chapter_001/page_0004.jpg
 **/
class PadNumber {
    long pad
    long value

    PadNumber next() {
        value++
        this
    }

    String getSkuNumber() {
        String.format("%0${pad}d", value)
    }

    String toString() {
        this.getSkuNumber()
    }
}

/**
 * Closure for downloading pictures
 **/
urlToFile = { u, filename ->
    final URL url = new URL(u)
    final URLConnection hc = url.openConnection()
    hc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:65.0) Gecko/20100101 Firefox/65.0")
    if (args.size() > ARGS_BEFORE_REQUEST_PROPERTY) {
        for (int i = ARGS_BEFORE_REQUEST_PROPERTY; i < args.size(); i++) {
            def param = (args[i] as String).split(':', 2)
            println "add $param to request"
            hc.addRequestProperty(param[0], param[1])
        }
    }
    def file = new File(filename).newOutputStream()
    hc.connect()
    if (hc.responseCode != 200) throw new FileNotFoundException("${hc.responseCode}")
    file << hc.inputStream
    file.close()
}

// MAIN
if (!options) {
    println(usage)
    return
}

args = options.arguments()
println args

if (!options.n) {
    if (args.size() < 5 || options.h) {
        println(usage)
        return
    }
    def url = args[ARG_URL]
    def chapter = new PadNumber(pad: args[ARG_CHAPTER_PADDING] as int, value: args[ARG_CHAPTER_START] as int)

    for (int i = (args[ARG_CHAPTER_START] as int); i <= (args[ARG_CHAPTER_END] as int); i++) {
        final File folder = new File("$chapter")
        folder.mkdir()
        def page = new PadNumber(pad: args[ARG_PAGE_PADDING] as int, value: 1)
        boolean firstTime = true
        while (true) {
            try {
                def dl = url.replaceAll(":chapter:", "$chapter").replaceAll(":page:", "$page")
                println dl
                urlToFile(dl, "$chapter/${page}.jpg")
                firstTime = true
            } catch (FileNotFoundException e) {
                if (firstTime) {
                    // sometimes, on some websites, there is ad instead of page
                    firstTime = false
                } else {
                    println e
                    // twice nothing, there is nothing more on this chapter
                    break
                }
            }
            page++
        }
        chapter++
    }
}

if (options.c) {
    @SourceURI
    URI sourceUri
    Path scriptLocation = Paths.get(sourceUri)
    def converter = evaluate(new File("${scriptLocation.parent}${File.separator}${options.c}-convert.groovy"))
    if (options.r) {
        println options.r
        int chapter = 0
        int current = 0
        int limit = options.r as int
        def parentDir = new File(".")
        converter.createFile(chapter)
        def dirs = []
        parentDir.eachFileRecurse(groovy.io.FileType.DIRECTORIES) { dirs << it }
        dirs = dirs.sort { a, b -> a.name as int <=> b.name as int }
        dirs.each { dir ->
            println("Dir : " + dir.name)
            def pages = []
            dir.eachFileRecurse(groovy.io.FileType.FILES) { pages << it }
            pages = pages.sort { a, b -> FilenameUtils.getBaseName(a.name) as int <=> FilenameUtils.getBaseName(b.name) as int }
            pages.each { file ->
                println(file.name)
                if (file.size() == 0) {
                    file.delete()
                } else {
                    String newFileName = "p_${current}.${FilenameUtils.getExtension(file.name)}"
                    println(newFileName)
                    converter.addFile(newFileName, file)
                    current++
                    if (current > limit) {
                        chapter++
                        current = 0
                        converter.close()
                        converter.createFile(chapter)
                    }
                }
            }
        }
    } else {
        new File(".").eachFileRecurse(groovy.io.FileType.DIRECTORIES) { File dir ->
            if (!new File("${dir.name}.${options.c}").exists()) {
                converter.createFile(dir.name)
                def pages = []
                dir.eachFileRecurse(groovy.io.FileType.FILES) { pages << it }
                pages = pages.sort { a, b -> FilenameUtils.getBaseName(a.name) as int <=> FilenameUtils.getBaseName(b.name) as int }
                pages.each { file ->
                    if (file.size() == 0) {
                        file.delete()
                    } else {
                        converter.addFile(file)
                    }
                }
                converter.close()
            }
        }
    }

}
