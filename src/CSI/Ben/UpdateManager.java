package CSI.Ben;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class UpdateManager {
	private static final Logger logger = LogManager.getLogger(UpdateManager.class);
	public static void main(String[] args){
		try {
			//取得list
			Map<String, String> updateMapList = readSetting();
			
			//執行
			execThem(updateMapList);
			
			//單純DEBUG
			logger.debug(CommonMethodForUpdate.JSONStringify(updateMapList));
			
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			e.printStackTrace();
			logger.debug(sw.toString());
		}
		
	}
	
	public static void execThem(Map<String, String> updateMapList) throws Exception{
		//弄好path
//		String osSystem = (System.getProperty("os.name").toLowerCase().indexOf("windows")>-1?"Windows":"Linux");
		String updateDir = CommonMethodForUpdate.readConfig(null, "updateDir");
		
		if(!(new File(updateDir).exists())){
			new File(updateDir).mkdirs();
		}
		
		logger.debug("=== WELCOME TO UPDATE MANAGER!!! ===");
		//遍歷map
		for (Entry<String, String> entry : updateMapList.entrySet()) {
		    String missionName = entry.getKey();
		    String missionFile = entry.getValue();
		    logger.debug("=== start mission '"+missionName+"' ===");
		    String file_path = updateDir +  missionFile.replaceAll("UpdateData/","");
		    if(!new File(file_path.split(" ")[0]).exists()){
		    	logger.debug("=== end mission for file '"+file_path+"' not found ===");
		    	continue;
		    }
		    
			String command = "java -jar " + file_path;
			Process process = Runtime.getRuntime().exec(command);
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			logger.debug("=== BELOWS ARE LOG FROM '"+missionFile+"' ===");
			while ((line = reader.readLine()) != null) {
				System.out.println("  "+line);
			}
			
			int returnValue = process.waitFor();
		    logger.debug("=== end mission '"+missionName+"' with '"+returnValue+"'===");
		}
	}
	
	public static Map<String,String> readSetting() throws Exception{
		Map<String,String> updateMapList = new LinkedHashMap<String,String>();
		//設定檔
		
		String updateListPath = CommonMethodForUpdate.readConfig(null, "updateListPath");
		
		if(!(new File(updateListPath).exists())){
			throw new Exception("設定檔: "+updateListPath+" 不存在");
		}
		
		//讀xml
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		builder = factory.newDocumentBuilder();
		InputStream inputStream = new FileInputStream(updateListPath);
		Document doc = builder.parse(new InputSource(new InputStreamReader(inputStream, "UTF-8")));
		NodeList nl = doc.getElementsByTagName("UpdateElement");
		
		for(int i=0;i < nl.getLength();i++){
			
//			logger.debug(getTagVal((Element)nl.item(i),"ElementName")+" : "+getTagVal((Element)nl.item(i),"ElementJarName"));
			updateMapList.put(
				getTagVal((Element)nl.item(i),"ElementName"),
				getTagVal((Element)nl.item(i),"ElementJarName")
			);
		}
		
		return updateMapList;
	}
	public static String getTagVal(Element ele,String tagName){
		if(ele==null){
			return "";
		}else{
			return ele.getElementsByTagName(tagName)
				.item(0)
				.getFirstChild()
				.getNodeValue()
				.trim();
		}
	}
}
