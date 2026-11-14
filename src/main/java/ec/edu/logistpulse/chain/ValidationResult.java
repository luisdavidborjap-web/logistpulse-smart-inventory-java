package ec.edu.logistpulse.chain;
public record ValidationResult(boolean valid, String rejectedBy, String reason) {
    public static ValidationResult allow() {return new ValidationResult(true,"","");}
    public static ValidationResult reject(String handler,String reason) {
        return new ValidationResult(false,handler,reason);
    }
}
