package ec.edu.logistpulse.chain;
import ec.edu.logistpulse.domain.*;
public final class CapacityHandler extends ValidationHandler {
    protected ValidationResult check(ValidationContext ctx) {
        OperationRequest r=ctx.request(); Inventory inv=ctx.inventory();
        if (r.type()==OperationType.RESERVE && inv.warehouse(r.warehouseId()).freeStaging()<r.quantity())
            return ValidationResult.reject("CAPACITY","Insufficient staging capacity");
        if (r.type()==OperationType.TRANSFER) {
            Warehouse wh=inv.warehouse(r.destinationWarehouseId());
            if ((long)inv.warehouseOnHand(wh.id())+r.quantity()>wh.storageCapacity())
                return ValidationResult.reject("CAPACITY","Destination storage capacity exceeded");
        }
        if (r.type()==OperationType.ADJUST && r.quantity()>0) {
            StockLot lot=inv.lot(r.lotId()); Warehouse wh=inv.warehouse(lot.warehouseId());
            if ((long)inv.warehouseOnHand(wh.id())+r.quantity()>wh.storageCapacity())
                return ValidationResult.reject("CAPACITY","Warehouse storage capacity exceeded");
        }
        return ValidationResult.allow();
    }
}
