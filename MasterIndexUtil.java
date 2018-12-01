public class MasterIndexUtil {
    public static String masterIndexPath= "MasterIndex/";
    public static String[] filelist = {masterIndexPath+"ab.txt",masterIndexPath+"cdefg.txt",masterIndexPath+"hijkl.txt",masterIndexPath+"mnopq.txt",masterIndexPath+"t.txt",masterIndexPath+"rsuvwxyz.txt"};
    public static int[] mapNumber = {2,5,5,5,1,8};
    public static int fileChunkSize = 1024 * 1024; // file chunk size in byte
    public static int maxRowOfChunk = 20000;   // file chunk size in row
}
