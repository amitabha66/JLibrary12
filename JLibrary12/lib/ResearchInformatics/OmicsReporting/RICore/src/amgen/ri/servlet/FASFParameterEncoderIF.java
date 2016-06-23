package amgen.ri.servlet;

/**
 * Thsi class provides a way for an application to encode parameters which get bounced from the FASF, SiteMinder security system.
 * It is used ot avoid any issues with the redirects. An implementation is provided by the application and provided by FASF filter init parameter
 * @version $Id
 */
public interface FASFParameterEncoderIF {
    /**
     * For a given parameter, return the encoded value.
     *
     * @param name String
     * @param value String
     * @return String
     */
    public String encodeParameter(String name, String value);
}
