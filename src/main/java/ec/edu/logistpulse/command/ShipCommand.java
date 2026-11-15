package ec.edu.logistpulse.command;
import ec.edu.logistpulse.service.*;
/** Shipping physically consumes stock and is intentionally non-compensable. */
public final class ShipCommand extends AbstractStockCommand {
    private final String reservationId;
    public ShipCommand(String id,String actor,String reservationId,StockOperations receiver) {
        super(id,actor,receiver);this.reservationId=reservationId;
    }
    public String type() {return "SHIP";}
    public String targetId() {return reservationId;}
    public String fingerprint() {return Fingerprint.of(type(),actor(),reservationId);}
    public OperationResult execute() {return receiver.ship(actor(),reservationId);}
    public OperationResult undo(String actor) {
        return OperationResult.fail("UNDO","Shipment has left inventory; use a separately audited return process");
    }
}
