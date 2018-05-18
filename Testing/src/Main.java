public class Main {

    public static void main(String[] args) {
        System.out.println();
        System.out.println("------Information Provider-------");
        System.out.println("-source\t\t\tInput file to be processed and filepath(\"/usr/Documents/\", \"C:\\Folder\\filename.ext\")\n");
        System.out.println("-mongo\t\t\tThe ip and port adress of the mongoDB server\n\t\t\t\tlocalhost, 192.168.100.1:99882 ...\n");
        System.out.println("-log\t\t\tThe output method that the program will have\n\t\t\t\tconsole/file/all/none (For file a file will be created with the name /logs/TheFloow.yyyyMMdd.HH.mm.log)\n");
        System.out.println("-chunksize\t\tNumber of bytes of a chunk in mongodb for the subfiles file ex: 371234\n\t\t\t\tDefault:358400\n");
        System.out.println("-filezie\t\tNumber of bytes of the subfiles created from the sourcefile in mongodb for the source file ex: 371234\n\t\t\t\tDefault:10870912\n");
        System.out.println("-result\t\t\tThe display method of the rezults\n\t\t\t\tconsole/file/all/none (For file a file will be created with the name /res/TheFloow.yyyyMMdd.HH.mm.rez)\n");
        System.out.println("-resultSize\t\tThe number of MAX and MIN count entry of the results\n");
        System.out.println("------Example of run-------");
        System.out.println("java –Xmx8192m -jar PlatformJob.jar –source dump.xml –mongo localhost -log file -chunksize 358400 -filesize 10870912 -result file -resultSize 25");
    }
}
