import java.net.URL;
import java.net.URLConnection;

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
	props["url"] = url
	props["lastdate"] = new Date()
	new File("props.ser").withObjectOutputStream { out -> out << props }
}

def loadProps(){
	try {
		new File("props.ser").withObjectInputStream { instream ->
			instream.eachObject { props = it }
		}
	} catch(Exception e){ /* using default url */ }
	return props
}

def sendData(def req, def url){
	URLConnection conn = new URL(url).openConnection()
	conn.setConnectTimeout(10000)
	conn.setReadTimeout(20000)
	
	BufferedInputStream inn = new BufferedInputStream(conn.getInputStream())
	def data = new byte[1024]
	int count
	def buff = new org.vertx.groovy.core.buffer.Buffer()
	while ((count = inn.read(data, 0, 1024)) != -1) {
		def bytes = new byte[count]
		System.arraycopy(data, 0, bytes, 0, count)	//refactor it then
		buff.appendBytes((byte[]) bytes)
	}
	
	req.response.putHeader("Content-Type", conn.getContentType())
	req.response.putHeader("Content-Length", buff.getLength())
	req.response.end(buff)
}

vertx.createHttpServer().requestHandler{ req ->
	if(req.path.equals(putUrl)){
		processPut(req)
		return
	}
	
	String url = props["url"]
	if(!req.path.equals("/") && url.endsWith("/") ){
		url = url.substring(0, url.length()-1)
		url = url + req.path
	}
	
	sendData(req, url)
}.listen(port)