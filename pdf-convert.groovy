@Grab(group='com.itextpdf', module='itextpdf', version='5.5.13')
import com.itextpdf.text.*
import com.itextpdf.text.pdf.*

class PdfConvert {

    String title
    Document document

    void createFile(String title) {
        this.title = title
        this.document = new Document()
        PdfWriter.getInstance(document, new FileOutputStream("${title}.pdf"))
        document.open()
    }

    void addFile(File file) {
        Image img = Image.getInstance(file.toURL())
        img.scaleToFit(PageSize.A4.width, PageSize.A4.height)
        document.add(img)
    }

    void addFile(String newFileName, File file) {
        this.addFile(file)
    }

    void close() {
        document.close()
        println("Wrote ${title}.pdf")
    }

}

return new PdfConvert()