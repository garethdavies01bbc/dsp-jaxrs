package bbc.forge.dsp.jaxrs;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import bbc.forge.dsp.exceptions.ErrorCodeException;

public class ExceptionRenderer {

	public String render(Exception e) {		
		ByteArrayOutputStream boas = new ByteArrayOutputStream();
		PrintWriter printWriter = new PrintWriter(boas);
		if(e instanceof ErrorCodeException){
			ErrorCodeException ex = (ErrorCodeException) e;
			printWriter.println("Code: " + ex.getCode());
		}
		printWriter.println("Message: " + e.getMessage());		
		printWriter.println();
		printWriter.println();
		printWriter.println("StackTrace:");
		printWriter.println();
		e.printStackTrace(printWriter);

		printWriter.flush();
		try {
			return boas.toString("UTF-8");
		}
		catch (UnsupportedEncodingException u) {
			throw new RuntimeException("Unsupported encoding UTF-8");
		}
	}

}
