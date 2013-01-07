props = [:]
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
	props["url"] = url
	props["lastdate"] = new Date()
	new File("props.ser").withObjectOutputStream { out -> out << props }
}

def loadProps(){
	try {
		new File("props.ser").withObjectInputStream { instream ->
			instream.eachObject { props = it }
		}
	}catch(Exception e){
		props =  ["url" : "http://www.rambler.ru/"]
	}
	return props
}

def loadData(def url){
	def file = new FileOutputStream("temp")
	def out = new BufferedOutputStream(file)
	out << new URL(url).openStream()
	out.close()
	return file
}

def server = vertx.createHttpServer()
server.requestHandler{ req ->

	if(req.path.equals("/put")){
		processPut(req)
		return
	}

	String url = props["url"]
	if(!req.path.equals("/") && url.endsWith("/") ){
		url = url.substring(0, url.length()-1)
		url = url + req.path
	}

	loadData(url)

	req.response.sendFile "temp"
}.listen(8080, "localhost")