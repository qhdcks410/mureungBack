package com.start;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@Controller
public class changeController {

	@RequestMapping("/")
	public String welcome() {
		
		return "index";
	}
	
	@PostMapping("/api/getNameYn")
	@ResponseBody
	public HashMap<String,Object> convertPOJOandJSON(@RequestBody HashMap<String,Object> pram){
		
		BufferedReader in = null;
		
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder;
        Document doc = null;
        
		try {
			
			String url = "https://unipass.customs.go.kr:38010/ext/rest/persEcmQry/retrievePersEcm?crkyCn=s280d221y008x001m040w060b0&persEcm=" + pram.get("pname")  +"&pltxNm=" + URLEncoder.encode((String)pram.get("name"), "UTF-8");			
			URL obj = new URL(url);
			// 호출할 url
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			// 타입 설정
			con.setRequestProperty("CONTENT-TYPE","text/xml"); 
			in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
			String line;
			String reslut ="";
			while ((line = in.readLine()) != null || line != null) {
				// response를 차례대로 출력 System.out.println(line);
				reslut += line.trim();
			}
			
            // xml 파싱하기
            InputSource is = new InputSource(new StringReader(reslut));
            builder = factory.newDocumentBuilder();
            doc = builder.parse(is);
            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();
            // XPathExpression expr = xpath.compile("/response/body/items/item");
            XPathExpression expr = xpath.compile("/persEcmQryRtnVo");
            NodeList nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                NodeList child = nodeList.item(i).getChildNodes();
                for (int j = 0; j < child.getLength(); j++) {
                    Node node = child.item(j);
                    System.out.println("현재 노드 이름 : " + node.getNodeName());
                    System.out.println("현재 노드 타입 : " + node.getNodeType());
                    System.out.println("현재 노드 값 : " + node.getTextContent());
                    
                    if( node.getNodeName().equals("tCnt")) {
                    	if(node.getTextContent().equals("1")) {
                        	pram.put("resultYn", "Y");
                        	pram.put("resultMsg", node.getTextContent());                    		                   		
                    	}else{
                        	pram.put("resultYn", "N");
                        	pram.put("resultMsg", node.getTextContent());                   		
                    	}
                    }
                }
            }

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
		}

		
	    return pram;
	}	
}
