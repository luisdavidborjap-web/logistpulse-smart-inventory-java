package ec.edu.logistpulse.command;

/** Unambiguous length-prefixed payload signature used for in-memory idempotency. */
final class Fingerprint {
    private Fingerprint() { }
    static String of(String... fields) {
        StringBuilder result=new StringBuilder();
        for (String field:fields) {
            if (field==null) result.append("-1:");
            else result.append(field.length()).append(':').append(field);
        }
        return result.toString();
    }
}
