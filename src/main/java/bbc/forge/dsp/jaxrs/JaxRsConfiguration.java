package bbc.forge.dsp.jaxrs;


public class JaxRsConfiguration {

	private static final int DEFAULT_200_MAX_AGE = 30;

	private static final int DEFAULT_503_MAX_AGE = 5;

	private static final int DEFAULT_404_MAX_AGE = 60;

	private static final int DEFAULT_500_MAX_AGE = 5;

	private int maxAge503;

	private int maxAge200;

	private int maxAge404;

	private int maxAge500;

	private String textHtmlOverrideMimeType;

	private boolean varyEnabled;

	public JaxRsConfiguration() {
		setMaxAge200(DEFAULT_200_MAX_AGE);
		setMaxAge503(DEFAULT_503_MAX_AGE);
		setMaxAge404(DEFAULT_404_MAX_AGE);
		setMaxAge500(DEFAULT_500_MAX_AGE);
		varyEnabled = true;
	}

	public void setMaxAge200(final int maxAge200) {
		this.maxAge200 = maxAge200;
	}

	public void setMaxAge503(final int maxAge503) {
		this.maxAge503 = maxAge503;
	}

	public void setMaxAge404(final int maxAge404) {
		this.maxAge404 = maxAge404;
	}

	public void setMaxAge500(int maxAge500) {
		this.maxAge500 = maxAge500;
	}

	public void setTextHtmlOverrideMimeType(String textHtmlOverrideMimeType) {
		this.textHtmlOverrideMimeType = textHtmlOverrideMimeType;
	}

	public void setVaryEnabled(boolean varyEnabled) {
		this.varyEnabled = varyEnabled;
	}

	public int getMaxAge503() {
		return maxAge503;
	}

	public int getMaxAge200() {
		return maxAge200;
	}

	public int getMaxAge404() {
		return maxAge404;
	}

	public String getTextHtmlOverrideMimeType() {
		return textHtmlOverrideMimeType;
	}

	public int getMaxAge500() {
		return maxAge500;
	}

	public boolean isVaryEnabled() {
		return varyEnabled;
	}

}