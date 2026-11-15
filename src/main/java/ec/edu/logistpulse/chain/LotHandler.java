package ec.edu.logistpulse.chain;
import ec.edu.logistpulse.domain.*;
public final class LotHandler extends ValidationHandler {
    protected ValidationResult check(ValidationContext ctx) {
        OperationRequest r=ctx.request(); Inventory inv=ctx.inventory();
        switch(r.type()) {
            case RESERVE -> {
                if (r.plan().isEmpty()) return ValidationResult.reject("LOT","No lots selected");
                for (Allocation a:r.plan()) {
                    StockLot lot=inv.lot(a.lotId());
                    if (lot==null) return ValidationResult.reject("LOT","Unknown lot: "+a.lotId());
                    if (!lot.sku().equals(r.sku()) || !lot.warehouseId().equals(r.warehouseId()))
                        return ValidationResult.reject("LOT","Lot does not belong to the requested SKU/warehouse");
                    if (!lot.eligible(ctx.today()))
                        return ValidationResult.reject("LOT","Lot expired or in quarantine: "+lot.id());
                }
            }
            case TRANSFER -> {
                StockLot lot=inv.lot(r.lotId());
                if (!lot.eligible(ctx.today()))
                    return ValidationResult.reject("LOT","Cannot transfer expired/quarantined lot");
                if (lot.warehouseId().equals(r.destinationWarehouseId()))
                    return ValidationResult.reject("LOT","Destination must differ from origin");
            }
            case SHIP -> {
                Reservation reservation=inv.reservation(r.reservationId());
                for (Allocation a:reservation.allocations()) {
                    StockLot lot=inv.lot(a.lotId());
                    if (lot==null || !lot.eligible(ctx.today()) || !lot.stateName().equals("RESERVED"))
                        return ValidationResult.reject("LOT","Cannot dispatch unavailable, expired or quarantined lot");
                }
            }
            case QUARANTINE -> {
                StockLot lot=inv.lot(r.lotId());
                if (lot.reserved()>0 || !(lot.stateName().equals("STORED") || lot.stateName().equals("RECEIVED")))
                    return ValidationResult.reject("LOT","Only unreserved received/stored lots may enter quarantine");
            }
            case REINSTATE -> {
                if (!inv.lot(r.lotId()).stateName().equals("QUARANTINED"))
                    return ValidationResult.reject("LOT","Lot is not in quarantine");
            }
            case UNSTORE -> {
                if (!inv.lot(r.lotId()).canUndoStorage())
                    return ValidationResult.reject("LOT","Lot was handled after put-away; unsafe to undo");
            }
            case STORE -> {
                if (!inv.lot(r.lotId()).stateName().equals("RECEIVED"))
                    return ValidationResult.reject("LOT","Only received lots may be put away");
            }
            case RELEASE, ADJUST -> { }
        }
        return ValidationResult.allow();
    }
}
