
package Utilities;

import ContainerQs.QueueWriter;
import static ProcessingClasses.PostToEConnectESB.JMS_FACTORY;
import static ProcessingClasses.PostToEConnectESB.LOG_QUEUE;
import static ProcessingClasses.PostToEConnectESB.MAIN_ESBRequest_Queue;
import static ProcessingClasses.PostToEConnectESB.PROVIDER_URL;
import static ProcessingClasses.PostToEConnectESB.properties;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;


public class CustomLogger {
    public final String configFile = System.getenv("JBOSS_HOME") + "/external_configs/configs.properties";
    public static final Properties properties = new Properties();
    public static String LOG_QUEUE = "";
    public static String PROVIDER_URL = "";
    String moduleName = "ESBHTTPServer";
    HashMap<String, String> logFields = new HashMap();

    private static final Logger LOGGER = Logger.getLogger("");
    static private FileHandler fhandler;
    static private SimpleDateFormat today;
    static private Date date;
    private static final String logFile = "/home/esb/ESBLogs";
    //private static final String logFile="D:/home/esblogs/Everest/";

    public CustomLogger() {
        logFields.put("module", moduleName);
        
        try {
            properties.load(new FileInputStream(configFile));
            PROVIDER_URL = properties.getProperty("PROVIDER_URL");
            LOG_QUEUE = properties.getProperty("ESBLog_Queue_DS");
        } catch (IOException ex) {
            Logger.getLogger(CustomLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

//    public void log(String txt, String lFile) {
////        String directory = "ESBWSConnector";
////        SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.SSS");
////        String timestamp = format.format(new Date());
////        String finalmessage = "###" + directory + "$$$" + lFile + "###" + timestamp + "::" + txt;
////        QueueWriter WLI = new QueueWriter("jms/ESBLog_Queue_DS", Config.PROVIDER_URL);
////        if (!WLI.send(finalmessage, "")) {
////            //Write yourself directly
////            logVer2(txt, lFile);
////
////        }
//    }

//    public void logVer2(String txt, String lFile) {
//
//        try {
//            // 
////            fhandler = new FileHandler(today.format(date) + ".txt");
//            date = new Date();
//            today = new SimpleDateFormat("yyyy-MM-dd");
//            String fileName = makeDirectory(logFile, lFile, today.format(date));
//            fhandler = new FileHandler(fileName + lFile + today.format(date), 0, 1, true);
//            //String fileName = logFile;
//            fhandler = new FileHandler(fileName + today.format(date) + "-" + lFile + "_" + "%g.log", 26000000, 20, true);
//            LOGGER.setLevel(Level.ALL);
//            fhandler.setFormatter(new EsbFormatter());
//
//            LOGGER.addHandler(fhandler);
//            LOGGER.info(txt);
//            //fhandler.flush();
//            fhandler.close();
////            return true;
//        } catch (Exception ex) {
//            LOGGER.log(Level.INFO, "Error in log {0}", ex.getMessage());
////            return false;
//        }
//
//    }
        public Context getInitialContext() throws NamingException {
        Properties p = new Properties();
        p.put(Context.PROVIDER_URL, PROVIDER_URL);
        //p.put(Context.SECURITY_PRINCIPAL, SECURITY_PRINCIPAL);
        //p.put(Context.SECURITY_CREDENTIALS, SECURITY_CREDENTIALS);
        Context cont = new InitialContext(p);
        return cont;
        }
    
    public void logVer3(String fileName, String message){
//           logFields.put("logName", fileName);
//        logFields.put("logMessage", message);
//        logFields.put("module", moduleName);
//        Context cont;
//        try {
//            cont = getInitialContext();
//            QueueWriter WLI = new QueueWriter("java:jboss/exported/jms/queue/ESBLog_Queue_DS", logFields, JMS_FACTORY);
//           WLI.sendObject(cont);
//        } catch (NamingException | JMSException ex) {
//            Logger.getLogger(CustomLogger.class.getName()).log(Level.SEVERE, null, ex);
//        }
           
    }


    public String StackTraceWriter(Exception exception) {
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        exception.printStackTrace(printWriter);
        String s = writer.toString();
        return s;
    }

    private static String makeDirectory(String dir, String lFile, String today) {
        String flname = dir + today;
        boolean success = (new File(flname)).mkdir();
        if (!success) {
            // Directory creation failed
            return dir;
        } else {
            return flname + "/";
        }
    }
    

}
