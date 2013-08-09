/**
 *
 */

package com.hp.myidea.guidedroid.base;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import android.content.Context;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 *
 */
public class DOMUtil {

    private static final String TAG = DOMUtil.class.getSimpleName();

    public static void test(Context context) {
        if (context == null) {
            return;
        }
        try {
            InputStream is = context.getAssets().open("building91A.xml");
            Document xmlDoc = DOMUtil.buildDocument(is);
            is.close();
            Element topElement = xmlDoc.getDocumentElement();
            List<Element> pointList = DOMUtil.findAllElementsByTagName(topElement, "geopoint");
            for (Element element : pointList) {
                Log.d(TAG, "\tGeopoint: " + element.getAttribute("guidedroid:hashId"));
            }
        } catch (IOException e) {
            Log.e(TAG, "\n\n\n\t\tError: ", e);
        }
    }

    /**
     * We need to build the XML document here, in a centralized way,
     * to deal correctly with namespace.
     * <br/>
     * If we do not set the DocumentBuilderFactory namespace aware,
     * calls to getLocalName and getNamespaceURI will return null.
     * <br/>
     * Worst, in order to search for a tag name we would need to pass in
     * the complete name, including the prefix (i.e., "ns1:someName").
     * <br/>
     * To do this, we would need to know previously the namespace prefix. Bad idea!
     *
     * @param xmlSrc
     * @return
     */
    public static Document buildDocument(InputStream inputStream) {
        Document doc = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
            doc = db.parse(inputStream);
        } catch (ParserConfigurationException e) {
            Log.e(TAG, "Bad news: ", e);
        } catch (SAXException e) {
            Log.e(TAG, "Bad news: ", e);
        } catch (IOException e) {
            Log.e(TAG, "Bad news: ", e);
        }
        return doc;
    }

    /**
     * @see #buildDocument(InputStream)
     *
     * @param xmlSrc
     * @return
     */
    public static Document buildDocument(String xmlSrc) {
        return buildDocument(new ByteArrayInputStream(xmlSrc.getBytes()));
    }

    /**
     * Searches for an element that has the given tag name.
     *
     * @param node The node to search for the tag name in.
     * @param localName The local name (w/o namespace prefix) to search for.
     * @return The first element found with the given tag name.
     * If no element is found, null is returned.
     */
    public static Element findFirstElementByTagName(Node node, String localName) {
        if (node == null || localName == null) {
            return null;
        }

        if ((node instanceof Element)) {
            String tagName = ((Element)node).getLocalName();
            if (tagName == null) {
                tagName = ((Element)node).getTagName();
            }

            //Log.d(TAG, "\tFound tag: " + tagName);

            if (tagName.equalsIgnoreCase(localName)) {
                return (Element)node;
            }
        }
        // WORKAROUND !!!
        // The getNextSibling implementation has a bug on the Android 2.1-update1 version
        // see http://code.google.com/p/android/issues/detail?id=779
        // We need to surround it with a try-catch block
        try {
            for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
                Element retElem = findFirstElementByTagName(child,localName);
                if (retElem != null) {
                    return retElem;
                }
            }
        } catch (IndexOutOfBoundsException e) {
            // Do nothing, same as "return null;"
        }
        return null;
    }

    public static List<Element> findAllElementsByTagName(Element elem, String localName) {
        if (elem == null || localName == null) {
            return null;
        }

        List<Element> ret = new ArrayList<Element>();
        DOMUtil.findAllElementsByTagName(elem, localName, ret);
        return ret;
    }

    public static Element getFirstElement(Node parent) {
        Node n = parent.getFirstChild();
        while (n != null && Node.ELEMENT_NODE != n.getNodeType()) {
            try {
                n = n.getNextSibling();
            } catch (IndexOutOfBoundsException e) {
                // WORKAROUND - see above
                n = null;
            }
        }
        if (n == null) {
            return null;
        }
        return (Element)n;
    }

    public static Element getNextElement(Element el) {
        Node nd = null;
        try {
            nd = el.getNextSibling();
        } catch (IndexOutOfBoundsException e) {
            // WORKAROUND - see above
        }
        while (nd != null) {
            if (nd.getNodeType() == Node.ELEMENT_NODE) {
                return (Element)nd;
            }
            try {
                nd = nd.getNextSibling();
            } catch (IndexOutOfBoundsException e) {
                // WORKAROUND - see above
            }
        }
        return null;
    }

    private static void findAllElementsByTagName(Element el, String localName, List<Element> elementList) {

        if (el == null || localName == null) {
            return;
        }

        String tagName = el.getLocalName();
        if (tagName == null) {
            tagName = el.getTagName();
        }

        if (localName.equals(tagName)) {
            elementList.add(el);
        }

        Element elem = getFirstElement(el);
        while (elem != null) {
            DOMUtil.findAllElementsByTagName(elem, localName, elementList);
            elem = getNextElement(elem);
        }
    }


}
