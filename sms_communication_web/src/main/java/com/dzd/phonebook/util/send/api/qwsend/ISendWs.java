package com.dzd.phonebook.util.send.api.qwsend;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

/**
 * This class was generated by the JAX-WS RI. JAX-WS RI 2.1.3-hudson-390-
 * Generated source version: 2.0
 * 
 */
@WebService(name = "ISendWs", targetNamespace = "http://impl.ws.zyz.com/")
public interface ISendWs {

	/**
	 * 
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 * @param arg5
	 * @return returns java.lang.String
	 */
	@WebMethod
	@WebResult(targetNamespace = "")
	@RequestWrapper(localName = "send", targetNamespace = "http://impl.ws.zyz.com/", className = "com.ca.ws.impl.Send")
	@ResponseWrapper(localName = "sendResponse", targetNamespace = "http://impl.ws.zyz.com/", className = "com.ca.ws.impl.SendResponse")
	public String send(
			@WebParam(name = "arg0", targetNamespace = "") String arg0,
			@WebParam(name = "arg1", targetNamespace = "") String arg1,
			@WebParam(name = "arg2", targetNamespace = "") String arg2,
			@WebParam(name = "arg3", targetNamespace = "") String arg3,
			@WebParam(name = "arg4", targetNamespace = "") String arg4,
			@WebParam(name = "arg5", targetNamespace = "") String arg5);

}
