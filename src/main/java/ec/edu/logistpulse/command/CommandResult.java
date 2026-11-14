package ec.edu.logistpulse.command;
public record CommandResult(String commandId,boolean successful,String objectId,String rejectedBy,String message) { }
