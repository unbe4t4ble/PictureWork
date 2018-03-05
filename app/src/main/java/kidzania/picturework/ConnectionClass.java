package kidzania.picturework;

import android.annotation.SuppressLint;
import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by H-PC on 16-Oct-15.
 */
public class ConnectionClass {

    public static String IPDATABASE;
    public static String IPUPLOAD;
    public static String NAMADB;
    public static String USER;
    public static String PASSWORD;
    public static String PATH;

    @SuppressLint("NewApi")
    Connection CONN() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Connection conn = null;
        try {
            String classs = "net.sourceforge.jtds.jdbc.Driver";
            Class.forName(classs);
            //Provider=SQLOLEDB.1;Password=accounting;Persist Security Info=True;User ID=accounting;Initial Catalog=PICTUREWORK;Data Source=192.168.1.8\finance
            String connString = "jdbc:jtds:sqlserver://"+IPDATABASE+"/"+NAMADB+";encrypt=false;instance=FINANCE";
            String username = USER;
            String password = PASSWORD;
            //conn = DriverManager.getConnection(ConnURL);
            conn = DriverManager.getConnection(connString,username,password);
        } catch (SQLException se) {
            Log.e("ERRO", se.getMessage());
        } catch (ClassNotFoundException e) {
            Log.e("ERRO", e.getMessage());
        } catch (Exception e) {
            Log.e("ERRO", e.getMessage());
        }
        return conn;
    }
}
