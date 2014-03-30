package org.jsoftware.logback;

import javax.mail.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

/**
 * AbstractEmailSessionAppender that obtains javax.mail.Session from JNDI (default: java:comp/env/mail/Session)
 * @author szalik
 */
public class JndiEmailSessionAppender<E> extends AbstractEmailSessionAppender<E> {
    public static final String JNDI_JAVA_COMP_ENV = "java:comp/env";
    private String jndi = "mail/Session";

    /**
     * @param jndi set JNDI resource name without java:comp/env
     */
    public void setJndi(String jndi) {
        this.jndi = jndi.startsWith("/") ? jndi.substring(1) : jndi;
    }

    @Override
    protected Session getSession() throws Exception {
        addInfo(formatLogMessage("Obtaining mail session form JNDI '" + JNDI_JAVA_COMP_ENV + jndi + "'."));
        Context initCtx = new InitialContext();
        Context envCtx = (Context) initCtx.lookup(JNDI_JAVA_COMP_ENV);
        return (Session) envCtx.lookup(jndi);
    }
}
