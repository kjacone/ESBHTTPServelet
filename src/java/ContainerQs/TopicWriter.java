package ContainerQs;

import Database.Config;
import Utilities.CustomLogger;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Properties;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

public class TopicWriter {

    public final static String JMS_FACTORY = "jms/RemoteConnectionFactory";
    private String TOPIC;
    private TopicConnectionFactory tconFactory;
    private TopicConnection tcon;
    private TopicSession tsession;
    private TopicPublisher tpublisher;
    private Topic topic;
    private TextMessage msg;
    private ObjectMessage objmsg;

    public TopicWriter(String TOPIC) {
        this.TOPIC = TOPIC;
        InitialContext ic = getInitialContext();
        init(ic, TOPIC);
    }

    public InitialContext getInitialContext() {
        InitialContext cont = null;
        Properties p = new Properties();
        try {
            p.put(Context.INITIAL_CONTEXT_FACTORY,
                    "org.jboss.naming.remote.client.InitialContextFactory");
            p.put(Context.URL_PKG_PREFIXES,
                    " org.jboss.naming:org.jnp.interfaces");
            p.put(Context.PROVIDER_URL, Config.PROVIDER_URL);

            p.put(Context.SECURITY_PRINCIPAL, Config.SECURITY_PRINCIPAL);
            p.put(Context.SECURITY_CREDENTIALS, Config.SECURITY_CREDENTIALS);
            cont = new InitialContext(p);
        } catch (Exception ex) {
          
        }
        return cont;
    }

    public void init(Context ctx, String topicName) {
        try {
            tconFactory = (TopicConnectionFactory) PortableRemoteObject.narrow(ctx.lookup(JMS_FACTORY), TopicConnectionFactory.class);
            tcon = tconFactory.createTopicConnection();
            tsession = tcon.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            topic = (Topic) PortableRemoteObject.narrow(ctx.lookup(topicName), Topic.class);
            tpublisher = tsession.createPublisher(topic);
            msg = tsession.createTextMessage();
            objmsg = tsession.createObjectMessage();
            tcon.start();
        } catch (ClassCastException | JMSException | NamingException ex) {
           ex.printStackTrace();
        }
    }

    public boolean sendObject(HashMap message, String biller) {
        boolean sent = false;
        try {
            //objmsg.setJMSCorrelationID(message.get("CorrelationID").toString());
            objmsg.setObject(message);
            objmsg.setStringProperty("biller", ((biller != null) ? biller : ""));
            tpublisher.publish(objmsg, DeliveryMode.PERSISTENT, 1, 0);
            sent = true;
        } catch (JMSException ex) {
          ex.printStackTrace();   
        }
        close();
        return sent;
    }

    public void close() {
        try {
            if (tcon != null) {
                tcon.close();
            }
            if (tsession != null) {
                tsession.close();
            }
            if (tpublisher != null) {
                tpublisher.close();
            }
        } catch (JMSException ex) {
          ex.printStackTrace();   
        }
    }

//    public static void main(String ars [] ){
//        System.out.println("Here we are ... ");
//        HashMap hp = new HashMap();
//        hp.put("1", "One");
//        hp.put("2", "Two");
//        hp.put("3", "Three");
//        hp.put("4", "Four");
//        hp.put("5", "Five");
//        new TopicWriter("jms/topic/ESBAdaptor_Requests_Topic_DS").sendObject(hp, "wewewewewewewew");
//    }
}
