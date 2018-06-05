package com.db;

import java.util.Random;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LWBIntegrationController {
	
	@Autowired
	ApplicationContext context;

	QueueConnectionFactory jmsIConnectionFactory;
	QueueConnectionFactory jmsOConnectionFactory;
	
	QueueSession inQueueSession;

	Destination inDestination;

	QueueConnection requestQueueConnection;

	QueueSession outQueueSession;

	Destination outDestination;

	QueueConnection responseQueueConnection;
			
	@RequestMapping(value="/msg/{empid}", method=RequestMethod.POST)
	public String tbmVrijeDagenCorrectie(@PathVariable("empid") String empid){
		System.out.println("Integration...... ");
		
		return manageMessage(empid);
	}
	
	private String manageMessage(String bsn){
		System.out.println("MANAGE MESSAGE ....... ............................................... ");
		
		QueueConnection inconnection = null;
		QueueConnection outconnection = null;
		Queue inQueue = null;
		Queue outQueue = null;
		QueueSession session = null;
		boolean transacted = false;

		try {
				
           //Get the connection factory
			QueueConnectionFactory inputqueueConnectionFactory = (QueueConnectionFactory) context.getBean("inconnectionFactory");
			QueueConnectionFactory outputqueueConnectionFactory = (QueueConnectionFactory) context.getBean("outconnectionFactory");
			
			inQueue = (Queue) context.getBean("inQ");
			outQueue = (Queue) context.getBean("outQ");
		   //Create and start a Connection
		   //Start the Connection
		   inconnection = inputqueueConnectionFactory.createQueueConnection();
		   inconnection.start();
 
		   outconnection = outputqueueConnectionFactory.createQueueConnection();
		   outconnection.start();
		   
		   //Create a transacted session
		   session = inconnection.createQueueSession(transacted,Session.AUTO_ACKNOWLEDGE);
		                    
		   //Create a queue browser
		   QueueBrowser qb = session.createBrowser(inQueue);
		   
		   //Send a JMS Queue Message
		   QueueSender queueSender = session.createSender(inQueue);
		   	   
		   TextMessage outMessage = session.createTextMessage(bsn);
		   
		   String msgGUID = createRandomString();
		   
		   outMessage.setJMSType(msgGUID);
		   
		   queueSender.send(outMessage);
		   System.out.println(outMessage.getText());
		   
		   String resSelectorBSN = "JMSType='"+ msgGUID+"'"; 
		   
		   QueueReceiver queueReceiver = session.createReceiver(outQueue, resSelectorBSN);
		   javax.jms.Message inMessage = queueReceiver.receive(3000); // timeout
		  
		   System.out.println("RESPONSE MESSAGE ::::::::::::::: "+inMessage);
		   //Print out received message
		   if (inMessage instanceof TextMessage) {
			    String replyString = ((TextMessage) inMessage).getText();
			    return replyString;
		   }else{
			   System.out.println("inMessage not an instance of Text Message :(");
			   System.out.println(inMessage.getClass().getName());
		   }

		} catch (JMSException e) {
			System.out.println(e.getMessage());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return "Request Failed";
	}	
	
    private String createRandomString() {
        Random random = new Random(System.currentTimeMillis());
        long randomLong = random.nextLong();
        return Long.toHexString(randomLong);
    }	
        
}
