package ec.edu.logistpulse.service;
public record OperationResult(boolean successful, String objectId, String rejectedBy, String message) {
    public static OperationResult ok(String id,String message) {return new OperationResult(true,id,"",message);}
    public static OperationResult fail(String stage,String message) {return new OperationResult(false,"",stage,message);}
}
