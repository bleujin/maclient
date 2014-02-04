package net.ion.bleujin;

import net.ion.framework.util.Debug;
import net.ion.radon.aclient.NewClient;
import net.ion.radon.aclient.Response;
import junit.framework.TestCase;

public class TestFirst extends TestCase {

	
	public void testConnect() throws Exception {
		NewClient nc = NewClient.create() ;
		Response response = nc.prepareGet("http://www.daum.net").execute().get() ;
		
		Debug.line(response.getTextBody()) ;
		nc.close(); 
		
	}
	
}
