@Grab("net.lingala.zip4j:zip4j:1.3.2")
import net.lingala.zip4j.core.*
import net.lingala.zip4j.model.*
import net.lingala.zip4j.util.*

class CbzConvert {

    String title
    ZipFile zipFile
    ZipParameters parameters

    void createFile(String title) {
        this.title = title
        this.zipFile = new ZipFile("${title}.cbz")
        this.parameters = new ZipParameters()
        this.parameters.compressionMethod = Zip4jConstants.COMP_DEFLATE
        this.parameters.compressionLevel = Zip4jConstants.DEFLATE_LEVEL_NORMAL
    }

    void addFile(File file) {
        this.zipFile.addFile(file, this.parameters)
    }

    void addFile(String newFileName, File file) {
        this.parameters.setFileNameInZip(newFileName)
        this.parameters.setSourceExternalStream(true)
        this.addFile(file)
    }

    void close() {
        println("Wrote ${title}.cbz")
    }

}

return new CbzConvert()