props = ["url" : "http://www.rambler.ru/"]  //default url
port = 8080		//port for translation
putUrl = "/put"

/////////////////////////////////////

loadProps()

def processPut(req){
	if(req.params.containsKey("url")){
		saveUrl(req.params["url"])
		req.response.end("ok!");
	} else {
		req.response.end("ok! " + props["lastdate"] + " : "+ props["url"]);
	}
}

def saveUrl(url){
	props = ["url" : url, "lastdate" : new Date()]
	new File("props.ser").withObjectOutputStream { out -> out << props }
}

def loadProps(){
	try {
		new File("props.ser").withObjectInputStream { instream ->
			instream.eachObject { props = it } }
	} catch(Exception e){ /* using default url */ }
	return props
}

def sendData(def req, def url){
	try {
		URLConnection conn = new URL(url).openConnection()
		conn.setConnectTimeout(10000)
		conn.setReadTimeout(20000)
	
		def bytes = new ByteArrayOutputStream()
		bytes << conn.getInputStream() 
		bytes.close()
		req.response.putHeader("Content-Type", conn.getContentType())
		req.response.putHeader("Content-Length", bytes.size())
		req.response.end new org.vertx.groovy.core.buffer.Buffer(bytes.toByteArray())
		
	} catch ( IOException e ) { 
		container.logger.error("ololo", e)
		req.response.end() }
}

vertx.createHttpServer().requestHandler{ req ->
	if(req.path.equals(putUrl)){
		processPut(req)
		return
	}
	
	String url = props["url"]
	if(!req.path.equals("/") && url.endsWith("/") ){
		url = url.substring(0, url.length()-1) + req.path
	}
	
	sendData(req, url)
}.listen(port)