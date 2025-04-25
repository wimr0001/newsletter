package wr.newsletter;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.IOException;
import java.util.Properties;


public class MailSender {

	private final String username;
	private final String password;
	private final String host;
	private final int port;
	private final int timeout;
	private boolean sslEnable;
	private boolean startTlsEnable;

	public MailSender(String username, String thePassword, String host, int port, int timeout, boolean ssl, boolean startTls) {
		this.username = username;
		this.password = thePassword;
		this.host = host;
		this.port = port;
		this.timeout = timeout;
		this.sslEnable = ssl;
		this.startTlsEnable = startTls;
	}

	public Session getSession() {
		final Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.ssl.enable", sslEnable);
		props.put("mail.smtp.host", this.host);
		props.put("mail.smtp.port", this.port);
		props.put("mail.smtp.timeout", this.timeout);
		props.put("mail.smtp.starttls.enable", startTlsEnable);
		javax.mail.Authenticator auth = new javax.mail.Authenticator() {
			protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
				return new javax.mail.PasswordAuthentication(username, password.toString());
			}
		};
		Session session = Session.getInstance(props, auth);
		return session;
	}

	public void sendMail(Session session, SendToAddress sendToAddress, String sendCC, String subject, String bodyHtml, String bodyText,
			String attachment, String nameFrom) throws MessagingException, IOException {
		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(username, nameFrom));
		InternetAddress[] toAddresses = {new InternetAddress(sendToAddress.getMailAddress(), sendToAddress.getPersonalName())};
		message.setRecipients(Message.RecipientType.TO, toAddresses);
		if (sendCC != null && !sendCC.equals("")) {
			message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(sendCC));
		}
		message.setSubject(subject);
		// create the mail root multipart
		MimeMultipart mpRoot = new MimeMultipart("mixed");
		
		// Create the content multipart (for text and HTML)
		Multipart mpContent = new MimeMultipart("alternative");
		
		// Create a body part to house the multipart/alternative Part
		MimeBodyPart contentPartRoot = new MimeBodyPart();
		contentPartRoot.setContent(mpContent);
		
		// Add the root body part to the root multipart
		mpRoot.addBodyPart(contentPartRoot);
		
		BodyPart messageBodyPart = new MimeBodyPart();
		messageBodyPart.setContent(bodyText, "text/plain; charset=UTF-8");
		messageBodyPart.setHeader("Content-Transfer-Encoding", "quoted-printable");
		mpContent.addBodyPart(messageBodyPart);
		messageBodyPart = new MimeBodyPart();
		messageBodyPart.setContent(bodyHtml, "text/html");
		mpContent.addBodyPart(messageBodyPart);
		if (attachment != null) {
			MimeBodyPart attachmentPart = new MimeBodyPart();
			attachmentPart.attachFile(new File(attachment), "application/pdf", null);
			mpRoot.addBodyPart(attachmentPart);
		}
		message.setContent(mpRoot);
		Transport.send(message);
	}
	
	

//	private File getFile(String filename) {
//		String fn = "file:///" + filename;
//		URI uri = URI.create(fn);
//		return new File(uri);
//	}
}