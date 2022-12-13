package ProcessingClasses;

import Database.Database;
import Utilities.CustomLogger;
import Utilities.XmlParser;
import com.google.gson.Gson;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import org.json.JSONObject;

public class ProcessTransactions {

    public static final Gson gson = new Gson();
    CustomLogger ESBLogger = new CustomLogger();
    public String ProcessRequests(String xmlMessage) {
        Database db = new Database();
        try {
            HashMap<String, String> fields = new HashMap();
            HashMap<String, String> fld_response = new HashMap();
            HashMap<String, String> Error = new HashMap();
            XmlParser xparser = new XmlParser();
            PostToEConnectESB cbs = new PostToEConnectESB();
            double transLimit = 0;
            double transAmount = 0;
          

            fields = xparser.XmlBreaker(xmlMessage);
            if (!fields.isEmpty()) {

                if (fields.get("3") == null) {
                    Error.put("3", "Field 3 missing.");
                } else if (fields.get("100") == null) {
                    Error.put("100", "Field 100 missing.");
                } else if (fields.get("37") == null) {
                    Error.put("37", "Field 37 missing.");
                } else {
                    for (String wherekey : fields.keySet()) {

                        if (fields.get(wherekey).isEmpty()) {
                            fields.remove(wherekey);
                            if (!wherekey.equals("4")) {
                                fields.put(wherekey, "");
                            } else {
                                fields.put(wherekey, "0");
                            }
                        }
                    }
                }
                //@TODO:
                String fld3 = fields.get("3");
                if ("400000".equalsIgnoreCase(fld3) || "620000".equalsIgnoreCase(fld3) || "630000".equalsIgnoreCase(fld3) || "440000".equalsIgnoreCase(fld3) || "010000".equalsIgnoreCase(fld3)) {
                    transLimit = db.GettransactionLimit(fields);
                    transAmount = Double.valueOf(fields.get("4"));
                    if (transAmount > transLimit) {
                        //LIMIT_EXCEEDED = "610"
                        fields.put("39", "610");
                        fields.put("validation", "failed");
                        fields.put("48", "AMOUNT LIMIT EXCEEDED");
                        fields.put("validation", "failed");
//                        Error.put("54", "AMOUNT LIMIT EXCEEDED");
                    }
                }

                String XREF = fields.get("37");
                fields.put("37", XREF);

                if (Error.isEmpty()) {
                    fields.put("XREF", XREF);
                    fields.put("38", XREF);
                    fields.put("validation", "passed");
                    fields = cbs.PostPaymentResult(fields);

                } else if ("610".equalsIgnoreCase(fields.get("39"))) {
                    fields.put("39", "610");
                    fields.put("validation", "failed");

                } else {
                    fields.put("39", "57");
                    fields.put("validation", "failed");
                    fields.put("54", "AMOUNT LIMIT EXCEEDED");
                }

            } else {
                fields.put("39", "57");
                fields.put("48", "Invalid XML isomsg format");
            }
            //System.out.println(fields.get("39") + ":" + fields.get("48"));
            String respXmlMessage = null;
            //if (!fields.get("48").equals("No Response")) { //No Response from ESB

            if (fields.containsKey("39")) {
                respXmlMessage = xparser.WriteResponseXML(fields);
            } else {
                fields.put("39", "990");
                fields.put("48", "Transaction Not Processed");
                respXmlMessage = xparser.WriteResponseXML(fields);
            }

//            System.out.println("HashMap: " + fields.toString());
//            System.out.println("XML: " + respXmlMessage);
            return respXmlMessage;
        } catch (Exception ex) {
           StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            ESBLogger.logVer3("exception", sw.toString());
            return null;
        }
    }

}
