package com.gayu.Gayathri;

import java.io.InputStream;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.testng.annotations.Test;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;

public class PP7v15 {
	  @Test
	    public void testStorageDetails() {
	        // Set up SSH connection
	        JSch jsch = new JSch();
	        com.jcraft.jsch.Session session = null;

	        try {
	            // Replace these with your SSH server details
	            String user = "hbp";
	            String host = "pp7v15.humanbrain.in";
	            String password = "Health#123";
	            int port = 22;

	            // Establish SSH session
	            session = jsch.getSession(user, host, port);
	            session.setPassword(password);
	            session.setConfig("StrictHostKeyChecking", "no");
	            session.connect();

	            // Execute command on SSH server
	            Channel channel = session.openChannel("exec");
	            // Command to retrieve storage details
	            ((ChannelExec) channel).setCommand("df -h /store");
	            channel.setInputStream(null);
	            ((ChannelExec) channel).setErrStream(System.err);

	            // Get output
	            InputStream in = channel.getInputStream();
	            channel.connect();

	            byte[] tmp = new byte[1024];
	            StringBuilder output = new StringBuilder();

	            while (true) {
	                while (in.available() > 0) {
	                    int i = in.read(tmp, 0, 1024);
	                    if (i < 0) break;
	                    output.append(new String(tmp, 0, i));
	                }
	                if (channel.isClosed()) {
	                    if (in.available() > 0) continue;
	                    System.out.println("Exit status: " + channel.getExitStatus());
	                    break;
	                }
	                try {
	                    Thread.sleep(1000);
	                } catch (Exception ee) {
	                    ee.printStackTrace();
	                }
	            }

	            // Parse and format output as table
	            String[] lines = output.toString().split("\n");

	            System.out.println("+------------------------------------+------+-------+-------+--------+----------------------+");
	            System.out.println("|       Filesystem                   | Size | Used  | Avail |  Use%  | Mounted on           |");
	            System.out.println("+------------------------------------+------+-------+-------+--------+----------------------+");

	            StringBuilder emailContent = new StringBuilder();
	            boolean sendEmail = false;

	            for (int i = 1; i < lines.length; i++) {
	                String[] parts = lines[i].trim().split("\\s+");
	                System.out.printf("| %-34s | %4s | %5s | %5s | %6s | %-20s |\n", parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]);
	                System.out.println("+------------------------------------+------+-------+-------+--------+----------------------+");
	                
	                // Check if Use% is greater than 70%
	                int usePercent = Integer.parseInt(parts[4].replace("%", ""));
	                if (usePercent > 70) {
	                    sendEmail = true;
	                   // emailContent.append(String.format("Filesystem: %s, Use%%: %s\n", parts[0], parts[4]));
	                }
	            }

	            // Send email if necessary
	            if (sendEmail) {
	                sendEmailAlert(emailContent.toString());
	            }

	            channel.disconnect();
	            session.disconnect();

	        } catch (Exception e) {
	            e.printStackTrace();
	            System.out.println("Test encountered an exception: " + e.getMessage());
	        }
	    }

	    private void sendEmailAlert(String messageBody) {
	        // Recipient's email ID needs to be mentioned.
	        String[] to = {"karthik6595@gmail.com","annotation.divya@gmail.com","nathan.i@htic.iitm.ac.in","venip@htic.iitm.ac.in"}; 
	       // String[] to = {"annotation.divya@gmail.com"};
	        // Sender's email ID needs to be mentioned
	        String from = "gayathri@htic.iitm.ac.in";
	        // Assuming you are sending email through Gmail's SMTP
	        String host = "smtp.gmail.com";
	        // Get system properties
	        Properties properties = System.getProperties();
	        // Setup mail server
	        properties.put("mail.smtp.host", host);
	        properties.put("mail.smtp.port", "465");
	        properties.put("mail.smtp.ssl.enable", "true");
	        properties.put("mail.smtp.auth", "true");
	        // Get the Session object and pass username and password
	        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
	            protected PasswordAuthentication getPasswordAuthentication() {
	                return new PasswordAuthentication("gayathri@htic.iitm.ac.in", "Gayu@0918");
	            }
	        });
	        // Used to debug SMTP issues
	        session.setDebug(true);
	        try {
	            // Create a default MimeMessage object.
	            MimeMessage message = new MimeMessage(session);
	            // Set From: header field of the header.
	            message.setFrom(new InternetAddress(from));
	            // Set To: header field of the header.
	            for (String recipient : to) {
	                message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
	            }
	            // Set Subject: header field
	            message.setSubject("PP7V15.humanbrain.in - STORAGE ALERT ⚠️ ");
	            // Set the actual message
	            message.setText("This email has been automatically generated:\n" + messageBody + "Attention and Action Required 🚨\n" + messageBody
	                    + "\nPP7V15 **scanner_7.1_nvmeShare** storage utilization has crossed 70% 🚫:\n" + messageBody);
	            System.out.println("sending...");
	            // Send message
	            Transport.send(message);
	            System.out.println("Sent message successfully....");
	        } catch (MessagingException mex) {
	            mex.printStackTrace();
	        }
	    }
	}
