package no.ecc.s100.security;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import no.ecc.s100.S100ProductSpecification;
import no.ecc.s100.utility.FileUtils;
import no.ecc.s100.utility.Hex;
import no.ecc.s100.utility.XMLUtils;

public class S100DataPermit implements Comparable<S100DataPermit> {

    private final String fileName;
    private final int edtn;
    private final Date permitEndDate;
    private final String encryptedDataKey;
    private final S100ProductSpecification productSpecification;

    static final String PERMIT_ELEMENT = "permit";
    private static final String FILENAME_ELEMENT = "filename";
    private static final String EDITION_NUMBER_ELEMENT = "editionNumber";
    private static final String EXPIRY_ELEMENT = "expiry";
    private static final String ENCRYPTED_KEY_ELEMENT = "encryptedKey";

    static final String EXPIRY_DATE_FORMAT = "yyyyMMdd";

    private S100DataPermit(String fileName, int edtn, Date permitEndDate, String encryptedDataKey,
            S100ProductSpecification productSpecification) {
        this.fileName = fileName;
        this.edtn = edtn;
        this.permitEndDate = permitEndDate;
        this.encryptedDataKey = encryptedDataKey;
        this.productSpecification = productSpecification;
    }

    S100DataPermit(S100ProductSpecification productSpecification, DateFormat expiryDateFormat,
            XMLStreamReader streamReader) throws IOException, XMLStreamException {
        this.productSpecification = productSpecification;

        String fileName = null;
        Integer edtn = null;
        Date permitEndDate = null;
        String encryptedDataKey = null;

        assert streamReader.getEventType() == XMLStreamConstants.START_ELEMENT;
        String rootElementName = streamReader.getLocalName();
        while (streamReader.hasNext()) {
            int e = streamReader.next();
            if (e == XMLStreamConstants.START_ELEMENT) {
                String localName = streamReader.getLocalName();
                if (FILENAME_ELEMENT.equals(localName)) {
                    fileName = XMLUtils.readCharacters(streamReader);
                } else if (EDITION_NUMBER_ELEMENT.equals(localName)) {
                    edtn = Integer.valueOf(XMLUtils.readCharacters(streamReader));
                } else if (EXPIRY_ELEMENT.equals(localName)) {
                    try {
                        permitEndDate = expiryDateFormat.parse(XMLUtils.readCharacters(streamReader));
                    } catch (ParseException et) {
                        throw new IOException(et);
                    }
                } else if (ENCRYPTED_KEY_ELEMENT.equals(localName)) {
                    encryptedDataKey = XMLUtils.readCharacters(streamReader);
                }
            }
            if (e == XMLStreamConstants.END_ELEMENT && rootElementName.equals(streamReader.getLocalName())) {
                break;
            }
        }

        this.fileName = fileName;
        this.edtn = edtn;
        this.permitEndDate = permitEndDate;
        this.encryptedDataKey = encryptedDataKey;
    }

    public static S100DataPermit create(String fileName, int edtn, Date permitEndDate, String dataKey, String hwId,
            S100ProductSpecification productSpecification) throws GeneralSecurityException {
        S100Crypt crypt = new S100Crypt.EmptyIVNoPadding(hwId);
        String encryptedDataKey = Hex.toString(crypt.encrypt(Hex.fromString(dataKey)));
        return new S100DataPermit(fileName, edtn, permitEndDate, encryptedDataKey, productSpecification);
    }

    public String getFileName() {
        return fileName;
    }

    public String getDataSetId() {
        return FileUtils.getFileNameWithoutSuffix(fileName);
    }

    public int getEdtn() {
        return edtn;
    }

    public Date getPermitEndDate() {
        return permitEndDate;
    }

    public String getEncryptedDataKey() {
        return encryptedDataKey;
    }

    public S100ProductSpecification getProductSpecification() {
        return productSpecification;
    }

    void appendTo(XMLStreamWriter writer, DateFormat permitDateFormat) throws XMLStreamException {
        writer.writeStartElement(PERMIT_ELEMENT);

        writer.writeStartElement(FILENAME_ELEMENT);
        writer.writeCharacters(getFileName());
        writer.writeEndElement();

        writer.writeStartElement(EDITION_NUMBER_ELEMENT);
        writer.writeCharacters(Integer.toString(getEdtn()));
        writer.writeEndElement();

        writer.writeStartElement(EXPIRY_ELEMENT);
        writer.writeCharacters(permitDateFormat.format(getPermitEndDate()));
        writer.writeEndElement();

        writer.writeStartElement(ENCRYPTED_KEY_ELEMENT);
        writer.writeCharacters(getEncryptedDataKey());
        writer.writeEndElement();

        writer.writeEndElement();
    }

    @Override
    public int compareTo(S100DataPermit o) {
        return fileName.compareTo(o.fileName);
    }

    @Override
    public int hashCode() {
        return fileName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof S100DataPermit)) {
            return false;
        }
        S100DataPermit o = (S100DataPermit) obj;
        return fileName.equals(o.getFileName());
    }

}
