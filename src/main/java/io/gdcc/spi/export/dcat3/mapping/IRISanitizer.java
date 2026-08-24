package io.gdcc.spi.export.dcat3.mapping;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIFactory;

public class IRISanitizer {
    private IRISanitizer() {}
    
    public static String sanitize(String iri) {
        // Replace whitespace with underscores, takes care of most issues
        // Note that we could have the whitespace percent encoded as well, 
        // but we don't because underscores are more readable
        iri = iri.replaceAll("\\s+", "_");

        // Try to fix all characters if needed
        if (!IRISanitizer.isValidIri(iri)) { // prevent double encoding
            iri = IRISanitizer.toValidIri(iri);
        }
        return iri;
    }


    private static final String SAFE_AUTH = "-._~!$&'()*+,;=:@[]";
    private static final String SAFE_PATH = "-._~!$&'()*+,;=:@/";
    private static final String SAFE_QUERY_FRAGMENT = "-._~!$&'()*+,;=:@/?";

    private static final IRIFactory IRI_FACTORY = IRIFactory.iriImplementation();

    public static String toValidIri(String raw) {
        if (raw == null) return null;
        Components c = split(raw);

        StringBuilder out = new StringBuilder();
        if (c.scheme != null) out.append(c.scheme).append(':');
        if (c.authority != null) out.append("//").append(encode(c.authority, SAFE_AUTH, true));
        if (c.path != null) out.append(encode(c.path, SAFE_PATH, true));
        if (c.query != null) out.append('?').append(encode(c.query, SAFE_QUERY_FRAGMENT, true));
        if (c.fragment != null) out.append('#').append(encode(c.fragment, SAFE_QUERY_FRAGMENT, true));
        return out.toString();
    }

    public static String toValidUri(String raw) {
        if (raw == null) return null;
        Components c = split(raw);

        StringBuilder out = new StringBuilder();
        if (c.scheme != null) out.append(c.scheme).append(':');
        if (c.authority != null) out.append("//").append(encode(c.authority, SAFE_AUTH, false));
        if (c.path != null) out.append(encode(c.path, SAFE_PATH, false));
        if (c.query != null) out.append('?').append(encode(c.query, SAFE_QUERY_FRAGMENT, false));
        if (c.fragment != null) out.append('#').append(encode(c.fragment, SAFE_QUERY_FRAGMENT, false));
        return out.toString();
    }

    public static boolean isValidIri(String value) {
        if (value == null || value.isBlank()) return false;
        if (!hasScheme(value)) return false; // prevent scheme-less IRIs like "example.org/path" from being considered valid
        if (!hasRequiredAuthorityForNetworkScheme(value)) return false;
        IRI iri = IRI_FACTORY.create(value);
        return !iri.hasViolation(false);
    }

    public static boolean isValidUri(String value) {
        if (value == null || value.isBlank()) return false;
        // URI validity as ASCII-only policy
        String normalized = toValidUri(value);
        return value.equals(normalized) && hasScheme(value) && hasRequiredAuthorityForNetworkScheme(value);
    }

    private static boolean hasScheme(String s) {
        int i = s.indexOf(':');
        if (i <= 0) return false;
        String scheme = s.substring(0, i);
        return scheme.matches("^[a-zA-Z][a-zA-Z0-9+.-]*$");
    }

    private static boolean hasRequiredAuthorityForNetworkScheme(String value) {
        Components c = split(value);
        if (c.scheme == null) return false;

        String schemeLower = c.scheme.toLowerCase();
        if (schemeLower.equals("http") || schemeLower.equals("https") || schemeLower.equals("ftp")) {
            return c.authority != null && !c.authority.isBlank();
        }
        return true;
    }

    private static String encode(String s, String extraSafe, boolean allowUnicodeIriChars) {
        StringBuilder out = new StringBuilder();
        int i = 0;
        while (i < s.length()) {
            int cp = s.codePointAt(i);
            int charCount = Character.charCount(cp);

            // Preserve valid percent-encoded octets
            if (cp == '%' && i + 2 < s.length() && isHex(s.charAt(i + 1)) && isHex(s.charAt(i + 2))) {
                out.append('%').append(s.charAt(i + 1)).append(s.charAt(i + 2));
                i += 3;
                continue;
            }

            boolean safeAscii = cp < 128 && (Character.isLetterOrDigit(cp) || extraSafe.indexOf(cp) >= 0);
            boolean safeUnicode = allowUnicodeIriChars && isUcsChar(cp);

            if (safeAscii || safeUnicode) {
                out.appendCodePoint(cp);
            } else {
                byte[] bytes = new String(Character.toChars(cp)).getBytes(StandardCharsets.UTF_8);
                for (byte b : bytes) out.append('%').append(String.format("%02X", b & 0xFF));
            }

            i += charCount;
        }
        return out.toString();
    }

    private static boolean isHex(char c) {
        return (c >= '0' && c <= '9')
            || (c >= 'a' && c <= 'f')
            || (c >= 'A' && c <= 'F');
    }

    private static boolean isUcsChar(int cp) {
        return (cp >= 0xA0 && cp <= 0xD7FF)
            || (cp >= 0xF900 && cp <= 0xFDCF)
            || (cp >= 0xFDF0 && cp <= 0xFFEF)
            || (cp >= 0x10000 && cp <= 0xEFFFD);
    }

    private static Components split(String raw) {
        try {
            URI u = new URI(raw);
            if (u.isOpaque()) {
                // e.g. urn:uuid:...
                return new Components(u.getScheme(), null, u.getRawSchemeSpecificPart(), null, u.getRawFragment());
            }
            return new Components(u.getScheme(), u.getRawAuthority(), u.getRawPath(), u.getRawQuery(), u.getRawFragment());
        } catch (URISyntaxException e) {
            // Minimal fallback: still split reasonably to encode by component.
            return Components.fallback(raw);
        }
    }

    private static final class Components {
        final String scheme;
        final String authority;
        final String path;
        final String query;
        final String fragment;

        Components(String scheme, String authority, String path, String query, String fragment) {
            this.scheme = scheme;
            this.authority = authority;
            this.path = path;
            this.query = query;
            this.fragment = fragment;
        }

        static Components fallback(String raw) {
            String scheme = null, authority = null, path, query = null, fragment = null;
            int colon = raw.indexOf(':');
            String rest = raw;
            if (colon > 0) {
                scheme = raw.substring(0, colon);
                rest = raw.substring(colon + 1);
            }

            int hash = rest.indexOf('#');
            if (hash >= 0) {
                fragment = rest.substring(hash + 1);
                rest = rest.substring(0, hash);
            }

            int q = rest.indexOf('?');
            if (q >= 0) {
                query = rest.substring(q + 1);
                rest = rest.substring(0, q);
            }

            if (rest.startsWith("//")) {
                String tmp = rest.substring(2);
                int slash = tmp.indexOf('/');
                if (slash >= 0) {
                    authority = tmp.substring(0, slash);
                    path = tmp.substring(slash);
                } else {
                    authority = tmp;
                    path = "";
                }
            } else {
                path = rest;
            }

            return new Components(scheme, authority, path, query, fragment);
        }
    }
}