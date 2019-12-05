package no.ecc.s100;

/**
 * A S-100 product specification. Like S-101 or S-102.
 */
public class S100ProductSpecification implements Comparable<S100ProductSpecification> {

    private final int nr;

    public S100ProductSpecification(int nr) {
        if (nr < 100 || nr > 999) {
            throw new IllegalArgumentException("Illegal S-100 product specification number: " + nr);
        }
        this.nr = nr;
    }

    /**
     * Construct a {@link S100ProductSpecification} based on a string that can
     * be like "101", "S101", "S-101", a S-100 data set id, a S-100 data set
     * file name.
     * 
     * @param s
     * @throws IllegalArgumentException
     *             if a product specification can not be found in the given
     *             {@link String} argument.
     */
    public S100ProductSpecification(String s) {
        try {
            if (s.matches("^[0-9]+$")) {
                this.nr = Integer.parseInt(s);
            } else if (s.length() == 4 && s.startsWith("S")) {
                this.nr = Integer.parseInt(s.substring(1));
            } else if (s.length() == 5 && s.startsWith("S-")) {
                this.nr = Integer.parseInt(s.substring(2));
            } else {
                Integer snr = S100FileName.standardNumber(s);
                if (snr == null) {
                    String pattern = ".*S-([0-9]{3}).*";
                    if (s.matches(pattern)) {
                        snr = Integer.valueOf(s.replaceAll(pattern, "$1"));
                    }
                }
                if (snr == null) {
                    throw new IllegalArgumentException(
                            "Could not extract S-100 product specification number from: " + s);
                }
                this.nr = snr.intValue();
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Could not extract S-100 product specification number from: "
                            + s);
        }
    }
    
    public static S100ProductSpecification createOrNull(String s) {
        try {
            return new S100ProductSpecification(s);
        } catch (RuntimeException e) {
            return null;
        }
    }

    public int getNumber() {
        return nr;
    }

    /**
     * @return a {@link String} with the name like "S-101". Like
     *         {@link #getShortName()}, but with "-".
     */
    public String getName() {
        return "S-" + nr;
    }

    /**
     * @return a {@link String} with the name like "S101". Like
     *         {@link #getName()}, but without "-".
     */
    public String getShortName() {
        return "S" + nr;
    }
    
    public String exchangeSetCatalogueFileName() {
        switch (nr) {
        case 101:
            return "S101ed1.CAT";
        default:
            return getShortName() + ".CAT";
        }
    }

    public String exchangeSetDirectoryName() {
        return getShortName();
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(nr);
    }

    @Override
    public int compareTo(S100ProductSpecification o) {
        return Integer.compare(nr, o.nr);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof S100ProductSpecification)) {
            return false;
        }
        S100ProductSpecification o = (S100ProductSpecification) obj;
        return nr == o.nr;
    }

}
