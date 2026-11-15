package ec.edu.logistpulse.chain;
import ec.edu.logistpulse.domain.*;
import java.util.HashSet;
import java.util.Set;
public final class StockHandler extends ValidationHandler {
    protected ValidationResult check(ValidationContext ctx) {
        OperationRequest r=ctx.request(); Inventory inv=ctx.inventory();
        switch(r.type()) {
            case RESERVE -> {
                if (r.quantity()<=0) return ValidationResult.reject("STOCK","Quantity must be positive");
                int available=inv.available(r.sku(),r.warehouseId(),ctx.today());
                if (available<r.quantity()) return ValidationResult.reject("STOCK",
                        "Not enough stock: available="+available+", requested="+r.quantity());
                long allocated=0; Set<String> seen=new HashSet<>();
                for (Allocation a:r.plan()) {
                    allocated+=a.quantity();
                    if (!seen.add(a.lotId())) return ValidationResult.reject("STOCK","Repeated lot allocation");
                    StockLot l=inv.lot(a.lotId());
                    if (l!=null && l.available()<a.quantity())
                        return ValidationResult.reject("STOCK","Insufficient stock in lot "+l.id());
                }
                if (allocated!=r.quantity()) return ValidationResult.reject("STOCK",
                        "Allocation plan incomplete: selected="+allocated+", requested="+r.quantity());
            }
            case ADJUST -> {
                StockLot lot=inv.lot(r.lotId());
                if (r.quantity()==0 || r.quantity()==Integer.MIN_VALUE)
                    return ValidationResult.reject("STOCK","Adjustment delta must be nonzero and reversible");
                long after=(long)lot.onHand()+r.quantity();
                if (after<lot.reserved() || after>Integer.MAX_VALUE)
                    return ValidationResult.reject("STOCK","Adjustment violates available stock / integer limits");
            }
            case TRANSFER -> {
                StockLot lot=inv.lot(r.lotId());
                if (r.quantity()<=0 || r.quantity()>lot.available())
                    return ValidationResult.reject("STOCK","Insufficient transferable units");
            }
            case SHIP -> {
                Reservation reservation=inv.reservation(r.reservationId());
                for (Allocation allocation:reservation.allocations()) {
                    StockLot lot=inv.lot(allocation.lotId());
                    if (lot==null || lot.reserved()<allocation.quantity())
                        return ValidationResult.reject("STOCK","Reservation allocation no longer available");
                }
            }
            case RELEASE, QUARANTINE, REINSTATE, STORE, UNSTORE -> { }
        }
        return ValidationResult.allow();
    }
}
