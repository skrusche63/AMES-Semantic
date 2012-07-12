package de.kp.ames.semantic.xml;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xpath.XPathAPI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class WNJsonConverter {

	public static String straightConversionXmlToJson(String xml) throws Exception {
		JSONObject jsonObject = new JSONObject();
		jsonObject = XML.toJSONObject(xml);

		return jsonObject.toString(4);
	}

	public static String conversionXmlToJson(InputStream is) throws Exception {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//		factory.setNamespaceAware(true);


		Document xmlDoc = factory.newDocumentBuilder().parse(is);
		Element rootElement = xmlDoc.getDocumentElement();
		
		System.out.println("rootElement: " + rootElement.getTagName() + " child Count: " + rootElement.getChildNodes().getLength());
		
		NodeList nodeList = XPathAPI.selectNodeList(rootElement, ".//Item");
		JSONArray jsonArray = new JSONArray();
		for (int i=0; i < nodeList.getLength(); i++) {
			Element e = (Element) nodeList.item(i);
			
			jsonArray.put(convertElementToJson(e));
			
		}
		
		return jsonArray.toString(4);

	}
	
	private static JSONObject convertElementToJson(Element e) {
		JSONObject jsonObject = new JSONObject();

		
		NodeList children = e.getChildNodes();
		
		for (int j=0; j < children.getLength(); j++) {
			
			Element child = (Element) children.item(j);
			String key = child.getTagName();
			String value;
			if (key.equals("Image")) {
				value = child.getAttribute("source");
			} else {
				value = child.getTextContent().trim();
			}
			try {
				jsonObject.put(key, value);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		return jsonObject;
	}
}
