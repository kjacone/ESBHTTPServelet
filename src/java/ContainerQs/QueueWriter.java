package ContainerQs;

import java.text.SimpleDateFormat;
import java.util.*;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public final class QueueWriter {

    private QueueConnectionFactory qconFactory;
    private QueueConnection qcon;
    private QueueSession qsession;
    private QueueSender qsender;
    private String queue;
    private TextMessage msg;
    private String JMS_FACTORY;
    private HashMap objmsg = new HashMap();

    public QueueWriter(String queue, HashMap objmsg, String JMS_FACTORY) {
        this.JMS_FACTORY = JMS_FACTORY;
        this.queue = queue;
        this.objmsg = objmsg;
    }

    public boolean sendObject(Context cont) throws NamingException, JMSException {
        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;
        try {
            System.out.println("Get JNDI Context");

            System.out.println("Get connection facory");
            //Custom made ConnetionFactory
            ConnectionFactory conFact = (ConnectionFactory) cont.lookup("java:jboss/exported/jms/RemoteConnectionFactory");
            System.out.println("Found Connection Factory: " + conFact);

            System.out.println("Create connection");
            connection = conFact.createConnection();

            System.out.println("Create session");
            session = connection.createSession(false,
                    QueueSession.AUTO_ACKNOWLEDGE);
            Destination queue = (Queue) cont.lookup(this.queue);

            producer = session.createProducer(queue);
            ObjectMessage msg = session.createObjectMessage();
            msg.setObject(objmsg);
            String CorrID = objmsg.get("CorrelationID") != null && (!objmsg.get("CorrelationID").toString().equalsIgnoreCase("null") || objmsg.get("CorrelationID").toString().length() < 1) ? objmsg.get("CorrelationID").toString() : new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
            msg.setJMSCorrelationID(CorrID);
            producer.send(msg);
            System.out.println("Message Sent: " + msg.toString());
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        } finally {
            System.out.println("close all the connections");    
            connection.close();      
            session.close();
            producer.close();
            
        }
    }

}
