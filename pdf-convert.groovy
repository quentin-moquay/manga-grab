@Grab(group = 'com.itextpdf', module = 'itextpdf', version = '5.5.13')
import com.itextpdf.text.*
import com.itextpdf.text.pdf.*

class PdfConvert {

    String title
    Document document

    void createFile(def title) {
        this.title = title
        this.document = new Document()
        PdfWriter.getInstance(document, new FileOutputStream("${title}.pdf"))
        document.open()
    }

    void addFile(File file) {
        println file
        Image img = Image.getInstance(file.toURL())

        float scaler = ((PageSize.A4.width - document.leftMargin()
                - document.rightMargin()) / img.getWidth()) * 100

        img.scalePercent(scaler)
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