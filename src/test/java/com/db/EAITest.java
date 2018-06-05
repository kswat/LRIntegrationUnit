package com.db;

import static org.junit.Assert.*;

import java.util.Random;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnection;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.jms.connection.CachingConnectionFactory;

public class EAITest {

	public static final ConnectionFactory amqFactory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
	public static final ConnectionFactory amqFactory2 = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
	
    public static final CachingConnectionFactory inconnectionFactory = new CachingConnectionFactory( amqFactory);
    public static final CachingConnectionFactory outconnectionFactory = new CachingConnectionFactory( amqFactory2);
    
	private final static String[] configFilesGatewayDemo = {"bvr-config.xml"};
    
    @BeforeClass
    public static void startUp() throws Exception {
    	inconnectionFactory.setCacheConsumers(false);
    	inconnectionFactory.createConnection().close();
    	
    	outconnectionFactory.setCacheConsumers(false);
    	outconnectionFactory.createConnection().close();
    }

    @AfterClass
    public static void shutDown() {
    	inconnectionFactory.resetConnection();
    	outconnectionFactory.resetConnection();
    }

	@Test
	public void test() throws JMSException {
		
		final GenericXmlApplicationContext context = new GenericXmlApplicationContext(configFilesGatewayDemo);
		Queue inQueue = (Queue) context.getBean("inQ");
		Queue outQueue = (Queue) context.getBean("outQ");
		
		QueueConnection inconnection = inconnectionFactory.createQueueConnection();
		inconnection.start();
		
		QueueConnection outconnection = outconnectionFactory.createQueueConnection();
		outconnection.start();
		
		QueueSession session = inconnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		
		QueueBrowser qb = session.createBrowser(inQueue);
		   
		//Send a JMS Queue Message
		QueueSender queueSender = session.createSender(inQueue);
		   	   
		TextMessage outMessage = session.createTextMessage("a message");
		
		String msgGUID = createRandomString();		   
		outMessage.setJMSType(msgGUID);		   

		queueSender.send(outMessage);
		String resSelectorBSN = "JMSType='"+ msgGUID+"'";
		   
		QueueReceiver queueReceiver = session.createReceiver(outQueue, resSelectorBSN);
		javax.jms.Message inMessage = queueReceiver.receive(3000); //timeout
		   
		//Print out received message
		if (inMessage instanceof TextMessage) {
			 String replyString = ((TextMessage) inMessage).getText();
			 System.out.println(replyString);
		}else{
			System.out.println("inMessage not an instance of Text Message :(");
			System.out.println(inMessage.getClass().getName());			   	
		}		
	}

    private String createRandomString() {
        Random random = new Random(System.currentTimeMillis());
        long randomLong = random.nextLong();
        return Long.toHexString(randomLong);
    }	
}