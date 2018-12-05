public class MasterIndexUtil {
    public static String masterIndexPath= "MasterIndex/";
    public static String[] filelist = {
            masterIndexPath+"ab.txt", masterIndexPath+"cdefg.txt", masterIndexPath+"hijkl.txt",
            masterIndexPath+"mnopq.txt", masterIndexPath+"t.txt", masterIndexPath+"rsuvwxyz.txt"};
    public static int[] mapNumber = {2,5,5,5,1,8};
    public static String findMasterIndexFileName(char c) {
        if (c == 'a' || c == 'b') {
            return filelist[0];
        }
        else if ('c' <= c && c <= 'g') {
            return filelist[1];
        }
        else if ('h' <= c && c <= 'l') {
            return filelist[2];
        }
        else if ('m' <= c && c <= 'q') {
            return filelist[3];
        }
        else if (c == 't') {
            return filelist[4];
        }
        return filelist[5];
    }

    public static int fileChunkSize = 1024 * 1024; // file chunk size in byte
    public static int maxRowOfChunk = 20000;   // file chunk size in row
    public static int defaultMaxNumOfMappers = 3;
    public static int defaultMaxNumOfQueryThreads = 3;

    public static enum OrderType {
        INDEX, QUERY
    }

    public static String helperInfoFileName = "server_list.txt";
    public static String reducerInfoFileName = "reducer_list.txt";
}
