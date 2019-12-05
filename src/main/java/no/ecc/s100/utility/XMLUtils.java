package no.ecc.s100.utility;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class XMLUtils {
    
    public static String readCharacters(XMLStreamReader reader) throws XMLStreamException {
        StringBuilder result = new StringBuilder();
        while (reader.hasNext()) {
            int eventType = reader.next();
            switch (eventType) {
                case XMLStreamReader.CHARACTERS:
                case XMLStreamReader.CDATA:
                    result.append(reader.getText());
                    break;
                case XMLStreamReader.END_ELEMENT:
                    return result.toString();
            }
        }
        throw new XMLStreamException("Premature end of file");
    }

}
