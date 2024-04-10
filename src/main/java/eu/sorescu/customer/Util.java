package eu.sorescu.customer;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class Util {
    static String throwable2string(Throwable t){
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        PrintStream ps=new PrintStream(baos);
        t.printStackTrace(ps);
        ps.flush();
        // no need to flush baos, but it should be done (theoretically); the problem is that autoclosable is forcing to handle io exception
        return new String(baos.toString());
    }
}
