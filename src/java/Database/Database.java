package Database;

import Utilities.CustomLogger;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import oracle.jdbc.OracleTypes;
import Utilities.Props;


public class Database {
    Props props = new Props();
    CallableStatement callableStatement = null;
    Connection dbConnection = null;
    private Statement stmt = null;
    private ResultSet rs = null;

    public Connection GetConnection() {
        
        try {
            InitialContext context = new InitialContext();
//            DataSource dataSource = (DataSource) context.lookup("java:jboss/datasources/EBANKDS");
//            DataSource dataSource = (DataSource) context.lookup("java:jboss/datasources/DCB_EbankDS");
//              DataSource dataSource = (DataSource) context.lookup("java:jboss/datasources/PROD_EBANKDS");
              DataSource dataSource = (DataSource) context.lookup(props.getDatabaseContextURL());
            dbConnection = dataSource.getConnection();
        } catch (Exception ex) {
            CustomLogger ESBLogger = new CustomLogger();
//            ESBLogger.log("Error at GetConnection " + ex.getMessage() + "\n" + ESBLogger.StackTraceWriter(ex), "error--");
        }
        return dbConnection;
    }

    public String getCorrelationID() {
        dbConnection = GetConnection();
        String CorrelationID = null;
        try {
            callableStatement = dbConnection.prepareCall("{call SP_GET_ESB_SEQUENCE(?)}");
            callableStatement.registerOutParameter("cv_1", OracleTypes.CURSOR);
            callableStatement.execute();
//            CorrelationID = callableStatement.getString("cv_1");
            
            rs = (ResultSet) callableStatement.getObject("cv_1");
            while (rs.next()) {
                CorrelationID = rs.getString("NEXTVAL");
                System.out.println(CorrelationID);
            }
        } catch (Exception e) {
            System.out.println();
            CustomLogger ESBLogger = new CustomLogger();
//            ESBLogger.log(" Error at getCorrelationID " + e.getMessage() + "\n" + ESBLogger.StackTraceWriter(e), "error--");
        }
        HouseKeep();
        return CorrelationID;

    }

    public boolean AuthenticateClientV2(HashMap fields) {
        boolean auth = false;
        dbConnection = null;
        //CallableStatement callableStatement = null;
        try {
            if (fields.get("sourceid") != null && fields.get("password") != null) {
                String SourceId = fields.get("sourceid").toString();
                String Password = fields.get("password").toString();
                String IP = fields.get("IP").toString();
                String Port = fields.get("Port").toString();
                dbConnection = GetConnection();
                callableStatement = dbConnection.prepareCall("{call SP_AUTHENTICATE_CLIENT(?,?,?)}");
                callableStatement.setString("iv_SourceIP", IP);
                callableStatement.setString("iv_Port", Port);
                callableStatement.registerOutParameter("ov_authorized", java.sql.Types.VARCHAR);
                callableStatement.executeUpdate();
                String ov_authorized = callableStatement.getString("ov_authorized");
                if (!ov_authorized.equalsIgnoreCase("0")) {
                    auth = true;
                }
            }
        } catch (Exception ex) {
            CustomLogger ESBLogger = new CustomLogger();
//            ESBLogger.log("Error at GetConnection " + ex.getMessage() + "\n" + ESBLogger.StackTraceWriter(ex), "error--");
        }
        HouseKeep();
        return auth;
    }

    public boolean AuthenticateClient(HashMap AuthHeader) {
        String username = AuthHeader.get("sourceid") != null ? AuthHeader.get("sourceid").toString() : "";
        String password = AuthHeader.get("password") != null ? AuthHeader.get("password").toString() : "";
        String IP = AuthHeader.get("IP") != null ? AuthHeader.get("IP").toString() : "";
        boolean authorized = false;
        String query = "select AUTHORIZED from TBAPIUSERS"
                + " where SOURCE_IP = '" + IP + "' and username = '" + username + "' and password = '" + password + "'";
        try {
            dbConnection = GetConnection();
            stmt = dbConnection.createStatement();
            stmt.execute(query);
            rs = stmt.getResultSet();
            while (rs.next()) {
                authorized = "1".equals(rs.getString("AUTHORIZED").trim());
            }
        } catch (Exception e) {
            CustomLogger ESBLogger = new CustomLogger();
//            ESBLogger.log("Error at AuthenticateClient " + e.getMessage() + "\n" + ESBLogger.StackTraceWriter(e), "error--");
        }
        HouseKeep();
        return authorized;
    }
    public double GettransactionLimit(HashMap reqmessage) {
        String limitAmount = "";
        double limitAmt = 0;

        String query = "SELECT AMOUNT FROM TBLIMITS WHERE PROCODE ='" + reqmessage.get("3") + "' AND APPROVED =1";
        try {
            dbConnection = GetConnection();
            stmt = dbConnection.createStatement();
            stmt.execute(query);
            rs = stmt.getResultSet();
            while (rs.next()) {
                limitAmount = rs.getString("AMOUNT");
            }
            limitAmt = Double.valueOf(limitAmount);
        } catch (SQLException e) {
            CustomLogger ESBLogger = new CustomLogger();
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            ESBLogger.logVer3("Exception", sw.toString() + " | requestMap:" + query);

        } finally {
            HouseKeep();
        }
        return limitAmt;
    }
    public void HouseKeep() {
        try {
            //Close JDBC objects as soon as possible
            if (stmt != null) {
                stmt.close();
            }
            if(rs != null){
                rs.close();
            }
            if (callableStatement != null) {
                callableStatement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }
        } catch (Exception e) {
            CustomLogger ESBLogger = new CustomLogger();
//            ESBLogger.log("Error at AuthenticateClient " + e.getMessage() + "\n" + ESBLogger.StackTraceWriter(e), "error--");

        }
    }

}
