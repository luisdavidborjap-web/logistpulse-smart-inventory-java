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
            case RELEASE, ADJUST -> { }
        }
        return ValidationResult.allow();
    }
}
