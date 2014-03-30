package org.jsoftware.logback;

import ch.qos.logback.core.Layout;
import ch.qos.logback.core.layout.EchoLayout;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;

/**
 * Base email appender
 * <p>It uses javax.mail.Session to send an email.</p>
 * @author szalik
 */
abstract class AbstractEmailSessionAppender<E> extends AbstractQueueAppender<E> {
    private Session session;
    private Layout<E> bodyLayout = new EchoLayout<E>();
    private Layout<E> subjectLayout = new EchoLayout<E>();
    private String fromStr, toStr;
    private InternetAddress fromAddress;
    private InternetAddress[] toAddresses;

    protected abstract Session getSession() throws Exception;

    public void setSendTo(String sendTo) {
        this.toStr = sendTo;
	}

    public void setSendFrom(String fromStr) {
        this.fromStr = fromStr;
    }

	public void setLayout(Layout<E> layout) {
		this.bodyLayout = layout;
	}

    public void setBodyLayout(Layout<E> bodyLayout) {
        this.bodyLayout = bodyLayout;
    }

    public void setSubjectLayout(Layout<E> subjectLayout) {
        this.subjectLayout = subjectLayout;
    }


    private static InternetAddress[] parse(String str, String fieldName) throws AddressException {
        if (str == null) {
            throw new AddressException("Property '" + fieldName + "' not set!");
        }
        if (str.trim().length() == 0) {
            throw new AddressException("Property '" + fieldName + "' is blank!");
        }
        InternetAddress[] ret = InternetAddress.parse(str.trim());
        if (ret.length == 0) {
            throw new AddressException("Property '" + fieldName + "' has no email addresses ('" + str + "')!");
        }
        return ret;
    }


    @Override
    protected void init() throws Exception {
        super.init();
        session = getSession();
        if (fromStr == null) {
            fromStr = session.getProperty("mail.from");
        }
        fromAddress = parse(fromStr, "sendFrom")[0];
        toAddresses =  parse(toStr, "sendTo");
    }


    @Override
    protected void processEvent(E event) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(fromAddress);
            message.setRecipients(Message.RecipientType.TO, toAddresses);
            message.setSubject(subjectLayout.doLayout(event));
            message.setSentDate(new Date());
            message.setText(bodyLayout.doLayout(event));
            session.getTransport().sendMessage(message, toAddresses);
        } catch (MessagingException e) {
            addError(formatLogMessage("Unable to send email message"), e);
        }
    }

}
