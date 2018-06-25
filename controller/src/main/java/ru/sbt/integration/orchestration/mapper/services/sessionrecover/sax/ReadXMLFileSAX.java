package ru.sbt.integration.orchestration.mapper.services.sessionrecover.sax;

import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;
import ru.sbt.integration.orchestration.mapper.services.sessionrecover.exceptions.SessionRecoverException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.StringReader;

public class ReadXMLFileSAX {
    public static void parseXML(DefaultHandler handler, String xml) {
        if (xml == null || xml.equals("")) {
            throw new SessionRecoverException("xml is null");
        }

        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            InputSource inputSource = new InputSource(new StringReader(xml));
            saxParser.parse(inputSource, handler);

        } catch (Exception e) {
            throw new SessionRecoverException(e);
        }
    }
}
