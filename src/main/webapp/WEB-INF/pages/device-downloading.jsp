<jsp:root
  xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:s="/struts-tags"
  xmlns:c="http://java.sun.com/jsp/jstl/core"
  version="2.0">

  <jsp:output doctype-root-element="html" omit-xml-declaration="true"
    doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN"
    doctype-system="http://www.w3c.org/TR/xhtml1/DTD/xhtml1-strict.dtd" />

	<jsp:directive.page contentType="text/html; charset=ISO-8859-15"/>
  <html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=iso-8859-15" />
    <title>Writing Progress</title>
	</head>
  <body>
  	<c:set var="link">
  		<s:url namespace="/ajax" action="devicedownload" includeParams="all"/>
  	</c:set>
  	<div dojoType="struts:BindDiv" updateFreq="2000" autoStart="true" stopTimerListenTopics="/finished"
  			 href="${link}" executeScripts="true" showError="true" showLoading="false">
			<p>Please wait...</p>
		</div>
  </body>
  </html>
</jsp:root>