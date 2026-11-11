import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * LAB 3 (solution) - Compute and check an HS384 JWT signature yourself.
 *
 *   java exercises/lab3/solution/SignJwt.java verify <token> [base64Secret]
 *   java exercises/lab3/solution/SignJwt.java sign '<payload json>' [base64Secret]
 *
 * base64Secret defaults to the development value of app.jwt.secret in application.properties.
 */
public class SignJwt {

    static final String DEV_SECRET = "Befa/RHbMrr0U+W0nXCBAflEAJZLPy2tXIs6PSfgksNPMetLtx6MJLpybvxBHHwK";

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: java SignJwt.java verify <token> [base64Secret]");
            System.out.println("       java SignJwt.java sign '<payload json>' [base64Secret]");
            return;
        }
        byte[] secret = Base64.getDecoder().decode(args.length > 2 ? args[2] : DEV_SECRET);

        if (args[0].equals("verify")) {
            String[] parts = args[1].split("\\.", -1);
            String signingInput = parts[0] + "." + parts[1];
            String expected = hs384(signingInput, secret);
            System.out.println("Signature in the token : " + parts[2]);
            System.out.println("Signature I computed   : " + expected);
            boolean valid = MessageDigest.isEqual(expected.getBytes(StandardCharsets.US_ASCII),
                    parts[2].getBytes(StandardCharsets.US_ASCII));
            System.out.println(valid ? "VALID - header and payload are exactly what the server signed"
                                     : "INVALID - header/payload were changed, or another secret was used");
        } else if (args[0].equals("sign")) {
            String header = base64Url("{\"alg\":\"HS384\"}".getBytes(StandardCharsets.UTF_8));
            String payload = base64Url(args[1].getBytes(StandardCharsets.UTF_8));
            System.out.println(header + "." + payload + "." + hs384(header + "." + payload, secret));
        } else {
            System.out.println("Unknown mode: " + args[0]);
        }
    }

    /** signature = base64url( HMAC-SHA384( secret, base64url(header) + "." + base64url(payload) ) ) */
    static String hs384(String signingInput, byte[] secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA384");
        mac.init(new SecretKeySpec(secret, "HmacSHA384"));
        return base64Url(mac.doFinal(signingInput.getBytes(StandardCharsets.US_ASCII)));
    }

    static String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
