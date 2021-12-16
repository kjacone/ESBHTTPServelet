package ProcessingClasses;

import ContainerQs.QueueBrowser;
import Database.Config;
import ContainerQs.QueueWriter;
import Database.Database;
import Utilities.CustomLogger;
import com.google.gson.Gson;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Properties;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class PostToEConnectESB {

    public final String configFile = System.getenv("JBOSS_HOME") + "/external_configs/configs.properties";
    public static final Properties properties = new Properties();
    public static final Gson gson = new Gson();
    CustomLogger ESBLogger = new CustomLogger();

    public static String JMS_FACTORY;
    public static String MAIN_ESBRequest_Queue;
    public static String MAIN_ESBResponse_Queue;
    public static String PROVIDER_URL;
    public static String SECURITY_PRINCIPAL;
    public static String SECURITY_CREDENTIALS;
    public static String LOG_QUEUE = "";

    Database database = new Database();

    public PostToEConnectESB() {
        try {
            properties.load(new FileInputStream(configFile));

            JMS_FACTORY = "java:jboss/exported/jms/RemoteConnectionFactory"; //properties.getProperty("JMS_FACTORY");
            MAIN_ESBRequest_Queue = properties.getProperty("MAIN_ESBRequest_Queue");
            MAIN_ESBResponse_Queue = properties.getProperty("MAIN_ESBResponse_Queue");
            PROVIDER_URL = properties.getProperty("PROVIDER_URL");
            SECURITY_PRINCIPAL = properties.getProperty("SECURITY_PRINCIPAL");
            SECURITY_CREDENTIALS = properties.getProperty("SECURITY_CREDENTIALS");
            LOG_QUEUE = properties.getProperty("ESBLog_Queue_DS");

        } catch (Exception ex) {

        }
    }

    public HashMap PostPaymentResult(HashMap fields) {
        try {
            String CorrelationID = "EC" + fields.get("32") + database.getCorrelationID();
            String narration = fields.get("68") != null ? fields.get("68").toString() : "" + ": XREF=" + CorrelationID;

            if (fields.containsKey("source")) {
                if (fields.get("source").toString().equalsIgnoreCase("")) {
                    fields.put("source", "everest");
                }
            } else {
                fields.put("source", "everest");
            }

            //global fields
            fields.put("88", narration);
            fields.put("online", "1");
            fields.put("MTI", (fields.get("0") == null) ? "0200" : fields.get("0"));
            fields.put("direction", "request");
            fields.put("destination", "xml");
            fields.put("CorrelationID", CorrelationID);
            fields.put("validation", "pass");

            boolean sentToQ = false;
            HashMap fromESB = new HashMap();

            QueueWriter WLI = new QueueWriter(MAIN_ESBRequest_Queue, fields, JMS_FACTORY);
            Context cont = getInitialContext();

            ////ESBLogger.logVer2("Log File To ESB " + CorrelationID + ":" + fields.toString(), "logFileToESB--");
//            System.out.print(fields);
            // Added this block to send to 
            int trials = 0;
            do {
                sentToQ = WLI.sendObject(cont);
                trials++;
            } while (sentToQ == false & trials < 3); //Try three time then give up and respond to Everest

            if (sentToQ) {
                ESBLogger.logVer3("ToESB", "Trials: " + trials + " : " + fields.toString());
                long Start = System.currentTimeMillis();
                long Stop = Start + (Config.flexTimeOut * 1000);

                do {
                    Thread.currentThread().sleep(150);
                    fromESB = getMessageFromUDQueue(CorrelationID, getInitialContextBrowser());
                } while (fromESB.isEmpty() && System.currentTimeMillis() < Stop);

                if (fromESB.isEmpty()) {
                    //Timeout
                    fields.put("39", "999");
                    fields.put("48", "No response from CBS");
                    ESBLogger.logVer3("NoResponseFromESB", fields.toString());
                } else {
                    //ESBLogger.logVer2("No Response " + CorrelationID + ":" + fields.toString(), "NoResponse--");
                    fields = fromESB;
                }
            } else {
                fields.put("39", "91");
                fields.put("48", "Host Not Available");
                ESBLogger.logVer3("FailedToESB", "Trials: " + trials + " : " + fields.toString());
            }

            ESBLogger.logVer3("FromESB", fields.toString());

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            ESBLogger.logVer3("exception", sw.toString());
        }
        return fields;
    }

    public InitialContext getInitialContextBrowser() throws NamingException {

        Properties p = new Properties();
        p.put(Context.SECURITY_PRINCIPAL, SECURITY_PRINCIPAL);
        p.put(Context.SECURITY_CREDENTIALS, SECURITY_CREDENTIALS);
        return new InitialContext(p);

    }

    public Context getInitialContext() throws NamingException {
        Properties p = new Properties();
        p.put(Context.PROVIDER_URL, PROVIDER_URL);
        //p.put(Context.SECURITY_PRINCIPAL, SECURITY_PRINCIPAL);
        //p.put(Context.SECURITY_CREDENTIALS, SECURITY_CREDENTIALS);
        Context cont = new InitialContext(p);
        return cont;

    }

    public HashMap getMessageFromUDQueue(String JMSCorrelationID, InitialContext cont) {
        Message msg = null;
        Object ResponseMessage = null;
        HashMap fields = new HashMap();
        int loops = 1;
        try {
            while (true) {
                if (loops > 5 || !fields.isEmpty()) {
                    break;
                }

                Message message = new QueueBrowser(cont).browseQueue(JMSCorrelationID, PROVIDER_URL, MAIN_ESBResponse_Queue);

                if (message instanceof ObjectMessage) {

                    System.out.println("Reading message");
                    ObjectMessage tm = (ObjectMessage) message;
                    //System.out.print();
                    //System.out.println("Reading message  1-----------------------");
                    fields = (HashMap) (((ObjectMessage) tm).getObject());
                    //System.out.println(tm.getObject());
                    //System.out.println("Reading message  2-----------------------");
                    System.out.println(loops);
                    //fields = tm.getObject();
                }

                loops++;
            }

        } catch (JMSException ex) {
            
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            ESBLogger.logVer3("exception", sw.toString());

        }
        return fields;
    }

    public String anyDate(String format) {
        try {
            if ("".equals(format)) {
                format = "yyyy-MM-dd HH:mm:ss"; // default
            }
            java.util.Date today;
            SimpleDateFormat formatter;

            formatter = new SimpleDateFormat(format);
            today = new java.util.Date();
            return (formatter.format(today));
        } catch (Exception ex) {
           StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            ESBLogger.logVer3("exception", sw.toString());  }
        return "";
    }
}
