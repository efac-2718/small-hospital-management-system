/**
 * LAB 3 - Decode a JWT by hand (no libraries, no jwt.io).
 *
 * 1. Log in with Postman and copy the "token" value.
 * 2. Complete the TODO.
 * 3. Run:  java exercises/lab3/DecodeJwt.java <token>
 *
 * Stuck? Compare with exercises/lab3/solution/DecodeJwt.java
 */
public class DecodeJwt {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java DecodeJwt.java <token>");
            return;
        }
        // A token is  base64url(header) . base64url(payload) . base64url(signature)
        String[] parts = args[0].split("\\.", -1);

        System.out.println("Header    : " + base64UrlDecode(parts[0]));
        System.out.println("Payload   : " + base64UrlDecode(parts[1]));
        System.out.println("Signature : " + parts[2]);
        // Question: did you need any key or password to read the role? What does that tell you?
    }

    static String base64UrlDecode(String part) {
        // TODO: decode the Base64URL text into bytes and turn the bytes into a UTF-8 String.
        // Hint: java.util.Base64.getUrlDecoder().decode(part)
        //       new String(bytes, java.nio.charset.StandardCharsets.UTF_8)
        // Question: why does the normal Base64.getDecoder() fail on some tokens?
        return "TODO";
    }
}
