package ec.edu.logistpulse.bootstrap;
import ec.edu.logistpulse.chain.*;
import ec.edu.logistpulse.domain.*;
import ec.edu.logistpulse.security.*;
import ec.edu.logistpulse.service.*;
import ec.edu.logistpulse.strategy.picking.*;
import ec.edu.logistpulse.strategy.replenishment.*;
import ec.edu.logistpulse.audit.*;
import ec.edu.logistpulse.command.*;
import java.time.*;
import java.util.Map;
/** Composition root: all dependencies are injected here, never instantiated in StockService. */
public final class SampleData {
    private SampleData() {}
    public record Application(Inventory inventory, StockOperations operations, CommandInvoker invoker,
                              PickingContext picking, ReplenishmentContext replenishment,
                              AuditLedger ledger, IdSequence ids, ActorDirectory actors) {}
    public static Application create() {return create(Clock.systemDefaultZone());}
    public static Application create(Clock clock) {
        Inventory inv=new Inventory();
        inv.addWarehouse(new Warehouse("WH-01",12,250));
        inv.addWarehouse(new Warehouse("WH-02",12,200));
        inv.addProduct(new Product("MED-001","Vaccines"));
        inv.addProduct(new Product("MED-002","Medical gloves"));
        inv.addProduct(new Product("TEC-001","IoT sensors"));
        inv.addLot(new StockLot("LOT-100","MED-001","WH-01",10,LocalDate.of(2026,8,15),
                LocalDate.of(2099,12,31),false));
        inv.addLot(new StockLot("LOT-200","MED-002","WH-01",5,LocalDate.of(2026,9,10),
                LocalDate.of(2027,2,15),false));
        inv.addLot(new StockLot("LOT-203","MED-002","WH-01",6,LocalDate.of(2026,8,1),
                LocalDate.of(2028,12,31),false));
        inv.addLot(new StockLot("LOT-204","MED-002","WH-01",8,LocalDate.of(2026,11,1),
                LocalDate.of(2029,6,30),false));
        inv.addLot(new StockLot("LOT-201","MED-002","WH-01",5,LocalDate.of(2026,7,1),
                LocalDate.of(2099,12,31),true));
        inv.addLot(new StockLot("LOT-202","MED-002","WH-01",4,LocalDate.of(2019,8,1),
                LocalDate.of(2020,1,1),false));
        inv.addLot(new StockLot("LOT-300","TEC-001","WH-01",3,LocalDate.of(2026,10,1),
                LocalDate.of(2099,12,31),false));
        ActorDirectory actors=new DemoActorDirectory(Map.of("ana",Role.OPERATOR,
                "mallory",Role.OPERATOR,"supervisor",Role.SUPERVISOR));
        ValidationHandler chain=new IdentityHandler(actors);
        chain.linkWith(new ResourceHandler()).linkWith(new AuthorizationHandler(actors))
            .linkWith(new StockHandler()).linkWith(new LotHandler()).linkWith(new CapacityHandler());
        IdSequence ids=new IdSequence();
        PickingContext picking=new PickingContext(new FefoPickingPolicy());
        ReplenishmentContext replenishment=new ReplenishmentContext(new ReorderPointPolicy());
        StockOperations operations=new StockService(
            new ReservationService(inv,picking,chain,clock,ids,actors),
            new StockMutationService(inv,chain,clock,ids,actors));
        AuditLedger ledger=new InMemoryAuditLedger();
        return new Application(inv,operations,new CommandInvoker(ledger,actors,clock),
                               picking,replenishment,ledger,ids,actors);
    }
    public static ReplenishmentParameters parameters(String sku,int stock) {
        return switch(sku) {
            case "MED-001" -> new ReplenishmentParameters(sku,stock,2,3,2,25,730,3,10);
            case "MED-002" -> new ReplenishmentParameters(sku,stock,4,3,4,40,1460,3,10);
            case "TEC-001" -> new ReplenishmentParameters(sku,stock,1,5,2,15,365,2,12);
            default -> throw new IllegalArgumentException("SKU without demo parameters: "+sku);
        };
    }
}
