package ec.edu.logistpulse.chain;
import ec.edu.logistpulse.domain.*;
public final class ResourceHandler extends ValidationHandler {
    protected ValidationResult check(ValidationContext ctx) {
        OperationRequest r=ctx.request(); Inventory inv=ctx.inventory();
        return switch (r.type()) {
            case RESERVE -> inv.product(r.sku())==null
                    ? ValidationResult.reject("RESOURCE","Unknown SKU: "+r.sku())
                    : inv.warehouse(r.warehouseId())==null
                    ? ValidationResult.reject("RESOURCE","Unknown warehouse: "+r.warehouseId())
                    : ValidationResult.allow();
            case RELEASE, SHIP -> {
                Reservation res=inv.reservation(r.reservationId());
                if (res==null) yield ValidationResult.reject("RESOURCE","Unknown reservation: "+r.reservationId());
                if (!res.active()) yield ValidationResult.reject("RESOURCE","Reservation already released");
                yield ValidationResult.allow();
            }
            case ADJUST, QUARANTINE, REINSTATE, STORE, UNSTORE -> inv.lot(r.lotId())==null
                    ?ValidationResult.reject("RESOURCE","Unknown lot: "+r.lotId()):ValidationResult.allow();
            case TRANSFER -> inv.lot(r.lotId())==null
                    ?ValidationResult.reject("RESOURCE","Unknown source lot: "+r.lotId())
                    :inv.warehouse(r.destinationWarehouseId())==null
                    ?ValidationResult.reject("RESOURCE","Unknown destination warehouse: "+r.destinationWarehouseId())
                    :ValidationResult.allow();
        };
    }
}
