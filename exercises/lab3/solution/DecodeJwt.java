import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * LAB 3 (solution) - Decode a JWT by hand.
 *
 * Run:  java exercises/lab3/solution/DecodeJwt.java <token>
 */
public class DecodeJwt {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java DecodeJwt.java <token>");
            return;
        }
        String[] parts = args[0].split("\\.", -1);
        if (parts.length != 3) {
            System.out.println("A JWS token has exactly 3 parts separated by '.', this one has " + parts.length);
            return;
        }
        System.out.println("Header    : " + base64UrlDecode(parts[0]));
        System.out.println("Payload   : " + base64UrlDecode(parts[1]));
        System.out.println("Signature : " + parts[2] + "  (binary HMAC bytes, Base64URL encoded - not readable text)");
        System.out.println();
        System.out.println("Nothing above needed a password or key: a JWT is SIGNED, not ENCRYPTED.");
        System.out.println("Never put secrets (passwords, card numbers...) inside a JWT payload.");
    }

    /** Base64URL uses '-' and '_' instead of '+' and '/', and drops the '=' padding. */
    static String base64UrlDecode(String part) {
        return new String(Base64.getUrlDecoder().decode(part), StandardCharsets.UTF_8);
    }
}
