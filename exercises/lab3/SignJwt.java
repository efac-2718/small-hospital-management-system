import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * LAB 3 - Compute an HS384 JWT signature yourself, to see WHY a modified token is rejected.
 *
 *   java exercises/lab3/SignJwt.java verify <token> [base64Secret]
 *   java exercises/lab3/SignJwt.java sign '<payload json>' [base64Secret]
 *
 * base64Secret defaults to the development value of app.jwt.secret in application.properties.
 *
 * Experiments (after completing the TODO):
 *   1. verify a real token from /api/auth/login               -> VALID
 *   2. verify the token you edited on jwt.io (role -> ADMIN)  -> INVALID   (that's Task 2!)
 *   3. verify a real token with a different secret            -> INVALID
 *   4. sign '{"sub":"admin","role":"ADMIN","exp":1999999999}' and call GET /api/admin/patients
 *      with it. It WORKS. Why is that scary, and what must you protect at all costs?
 *
 * Stuck? Compare with exercises/lab3/solution/SignJwt.java
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
            String signingInput = parts[0] + "." + parts[1];   // the signature covers header AND payload
            String expected = hs384(signingInput, secret);
            System.out.println("Signature in the token : " + parts[2]);
            System.out.println("Signature I computed   : " + expected);
            System.out.println(expected.equals(parts[2]) ? "VALID" : "INVALID");
        } else if (args[0].equals("sign")) {
            String header = base64Url("{\"alg\":\"HS384\"}".getBytes(StandardCharsets.UTF_8));
            String payload = base64Url(args[1].getBytes(StandardCharsets.UTF_8));
            System.out.println(header + "." + payload + "." + hs384(header + "." + payload, secret));
        }
    }

    /** signature = base64url( HMAC-SHA384( secret, base64url(header) + "." + base64url(payload) ) ) */
    static String hs384(String signingInput, byte[] secret) throws Exception {
        // TODO 1: Mac mac = Mac.getInstance("HmacSHA384");
        // TODO 2: mac.init(new SecretKeySpec(secret, "HmacSHA384"));
        // TODO 3: byte[] hmac = mac.doFinal(signingInput.getBytes(StandardCharsets.US_ASCII));
        // TODO 4: return base64Url(hmac);
        // Question: HMAC is a one-way function. Can you get the secret back from a token's signature?
        return "TODO";
    }

    static String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
