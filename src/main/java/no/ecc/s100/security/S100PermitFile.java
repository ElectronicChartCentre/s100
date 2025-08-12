package no.ecc.s100.security;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

import no.ecc.s100.S100ProductSpecification;
import no.ecc.s100.utility.XMLUtils;

public class S100PermitFile {

    private final Date date;
    private final String dataserver;
    private final String hwId;
    private final String userPermitString;
    private final Multimap<S100ProductSpecification, S100DataPermit> permitsByProductSpecification = TreeMultimap
            .create();

    public static final String PERMIT_DOT_XML = "PERMIT.XML";

    private static final String DATE_ELEMENT = "date";
    private static final String USERPERMIT_ELEMENT = "userpermit";
    private static final String DATASERVER_ELEMENT = "dataserver";
    private static final String PRODUCT_ELEMENT = "product";

    private static final String ID_ATTRIBUTE = "id";

    private static final String HEADER_DATE_FORMAT = "yyyyMMdd hh:mm:ss";

    public S100PermitFile(String dataserver, String hwId, String userPermitString) {
        this.dataserver = dataserver;
        this.hwId = hwId;
        this.userPermitString = userPermitString;
        this.date = new Date();
    }

    public S100PermitFile(S100ManufacturerLookup manufacturerLookup, InputStream in)
            throws IOException, XMLStreamException {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();

        Date date = null;
        S100UserPermit userPermit = null;
        String dataServer = null;
        S100ProductSpecification currentProductSpecification = null;
        DateFormat expiryDateFormat = new SimpleDateFormat(S100DataPermit.EXPIRY_DATE_FORMAT);

        XMLStreamReader streamReader = null;
        try {
            streamReader = inputFactory.createXMLStreamReader(in);

            while (streamReader.hasNext()) {
                int e = streamReader.next();
                if (e == XMLStreamConstants.START_ELEMENT) {
                    String localName = streamReader.getLocalName();
                    if (DATE_ELEMENT.equals(localName)) {
                        try {
                            date = new SimpleDateFormat(HEADER_DATE_FORMAT)
                                    .parse(XMLUtils.readCharacters(streamReader));
                        } catch (ParseException e1) {
                            throw new IOException(e1);
                        }
                    } else if (USERPERMIT_ELEMENT.equals(localName)) {
                        userPermit = new S100UserPermit(XMLUtils.readCharacters(streamReader));
                    } else if (DATASERVER_ELEMENT.equals(localName)) {
                        dataServer = XMLUtils.readCharacters(streamReader);
                    } else if (PRODUCT_ELEMENT.equals(localName)) {
                        currentProductSpecification = new S100ProductSpecification(
                                streamReader.getAttributeValue(null, ID_ATTRIBUTE));
                    } else if (currentProductSpecification != null && (S100DataPermit.PERMIT_ELEMENT.equals(localName)
                            || S100DataPermit.DATASET_PERMIT_ELEMENT.equals(localName))) {
                        permitsByProductSpecification.put(currentProductSpecification,
                                new S100DataPermit(currentProductSpecification, expiryDateFormat, streamReader));
                    }
                }
            }

        } finally {
            if (streamReader != null) {
                streamReader.close();
            }
        }

        S100Manufacturer m = manufacturerLookup.manufacturerForMId(userPermit.getMId());
        if (m == null) {
            throw new IllegalStateException("Unknown manufacturer. M_ID=" + userPermit.getMId());
        }

        this.date = date;
        this.dataserver = dataServer;
        this.userPermitString = userPermit.getUserPermitString();

        try {
            this.hwId = m.decrypt(userPermit.getHwIdEncrypted());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Could not decrypt HW_ID from S-100 user permit");
        }

    }

    public String getUserPermitString() {
        return userPermitString;
    }

    public void add(String fileName, int edtn, Date permitEndDate, String cryptKey,
            S100ProductSpecification productSpecification) throws GeneralSecurityException {
        add(S100DataPermit.create(fileName, edtn, permitEndDate, cryptKey, hwId,
                productSpecification));
    }

    public void add(S100DataPermit dataPermit) {
        permitsByProductSpecification.put(dataPermit.getProductSpecification(), dataPermit);
    }

    public void addAll(Collection<S100DataPermit> dataPermits) {
        for (S100DataPermit dataPermit : dataPermits) {
            add(dataPermit);
        }
    }

    public Collection<S100DataPermit> get(S100ProductSpecification productSpecification) {
        return Collections
                .unmodifiableCollection(permitsByProductSpecification.get(productSpecification));
    }

    public Collection<S100DataPermit> getDataPermits() {
        return Collections.unmodifiableCollection(permitsByProductSpecification.values());
    }

}
