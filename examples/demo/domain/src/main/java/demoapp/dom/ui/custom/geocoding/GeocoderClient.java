package demoapp.dom.ui.custom.geocoding;

import java.net.URL;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import lombok.SneakyThrows;
import lombok.val;

import demoapp.dom.ui.custom.latlng.LatLng;

@Service
public class GeocoderClient {

    private final static String KEY = "xlc4LAkkA0BFboXOzzrp4M5F5y83VCE4";

    @SneakyThrows
    public LatLng geocode(final String address) {

        val url = new URL(String.format("http://open.mapquestapi.com/geocoding/v1/address?key=%s&location=%s&outFormat=XML", KEY, address));

        val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        val xmlDoc = builder.parse(url.toString());

        return new LatLng(
                extract(xmlDoc, "//latLng/lat/text()"),
                extract(xmlDoc, "//latLng/lng/text()")
        );
    }

    private static String extract(Document xmlDoc, String expression) throws XPathExpressionException {
        return extract(xmlDoc, expression, 0);
    }

    private static String extract(Document xmlDoc, String expression, int index) throws XPathExpressionException {
        val xPathExpression = XPathFactory.newInstance().newXPath().compile(expression);
        val nodeList = (NodeList) xPathExpression.evaluate(xmlDoc, XPathConstants.NODESET);
        val node = nodeList.item(index);
        return node.getNodeValue();
    }

}
