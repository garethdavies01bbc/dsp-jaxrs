package bbc.forge.dsp.jaxrs;

import org.apache.cxf.jaxrs.ext.RequestHandler;
import org.apache.cxf.message.Message;

import bbc.forge.dsp.common.security.EnvironmentSpecificFileLoader;
import bbc.forge.dsp.common.security.SSLHeadersCertificateProcessor;
import bbc.forge.dsp.common.security.Whitelist;
import bbc.forge.dsp.common.security.WhitelistReader;

import javax.ws.rs.container.ContainerResponseFilter;

public abstract class AbstractHttpRequestCertificateAuth implements ContainerResponseFilter {

	private static final int DEFAULT_REFRESH_TIME_IN_MINUTES = 5;
	protected int refreshTimeInMinutes = DEFAULT_REFRESH_TIME_IN_MINUTES;

	protected SSLHeadersCertificateProcessor headersProcessor = new SSLHeadersCertificateProcessor();

	protected Whitelist whitelist;

	protected boolean active = true;
	private String whitelistFilename;

	public void init(){
		whitelist = new Whitelist(
				new EnvironmentSpecificFileLoader(),
				new WhitelistReader(),
				whitelistFilename,
				refreshTimeInMinutes);
	}

	protected String getRequestInfo(Message message) {
		return message.get(Message.HTTP_REQUEST_METHOD)
			+ "|" + message.get(Message.REQUEST_URL)
			+ "|" + message.get(Message.ACCEPT_CONTENT_TYPE);
	}

	public void setWhitelistFilename(String whitelistFilename){
		this.whitelistFilename=whitelistFilename;
	}

	public void setRefreshTimeInMinutes(int refreshTimeInMinutes){
		this.refreshTimeInMinutes=refreshTimeInMinutes;
	}

	public void setActive(boolean active){
		this.active=active;
	}
}
