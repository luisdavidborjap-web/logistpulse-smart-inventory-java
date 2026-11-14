package ec.edu.logistpulse.service;
/** Dependency inversion: Command implementations know only this receiver contract. */
public interface StockOperations {
    OperationResult reserve(String actor,String sku,String warehouseId,int quantity,String lotOrAuto);
    OperationResult release(String actor,String reservationId);
    OperationResult restore(String actor,String reservationId);
    OperationResult adjust(String actor,String lotId,int delta);
    OperationResult transfer(String actor,String sourceLotId,String destinationWarehouseId,int quantity);
    OperationResult undoTransfer(String actor,String sourceLotId,String transferredLotId,int quantity);
}
