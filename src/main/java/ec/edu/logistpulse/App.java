package ec.edu.logistpulse;
import ec.edu.logistpulse.bootstrap.SampleData;
import ec.edu.logistpulse.command.*;
import ec.edu.logistpulse.domain.*;
import ec.edu.logistpulse.strategy.picking.*;
import ec.edu.logistpulse.strategy.replenishment.*;
import java.time.LocalDate;
import java.util.Scanner;
/** Console interface: infrastructure/composition is in SampleData, domain stays separate. */
public final class App {
    private App() {}
    public static void main(String[] args) {
        SampleData.Application app=SampleData.create();
        Scanner sc=new Scanner(System.in);
        System.out.println("LOGISTPULSE v3 | Command + Chain + Strategy");
        System.out.println("Demo actors: ana, mallory (operators), supervisor (supervisor)");
        while(true) {
            System.out.println("\n1 Inventory   2 Reserve   3 Release    4 Adjust    5 Transfer");
            System.out.println("6 Reservations 7 Audit     8 Picking   9 Replenishment policy");
            System.out.println("10 Recommend  11 Undo command       0 Exit");
            System.out.println("Picking="+app.picking().defaultPolicy().name()+
                    " | Replenishment="+app.replenishment().defaultPolicy().name());
            String op=ask(sc,"Choice");
            try {
                switch(op) {
                    case "1" -> inventory(app);
                    case "2" -> {
                        String actor=ask(sc,"Actor"); String sku=ask(sc,"SKU");
                        String wh=ask(sc,"Warehouse [WH-01]"); if(wh.isBlank()) wh="WH-01";
                        int qty=Integer.parseInt(ask(sc,"Units"));
                        String lot=ask(sc,"Lot ID or AUTO");
                        execute(app,new ReserveCommand(app.ids().next("CMD"),actor,sku,wh,qty,lot,app.operations()));
                    }
                    case "3" -> {
                        String actor=ask(sc,"Actor"), reservationId=ask(sc,"Reservation ID");
                        execute(app,new ReleaseCommand(app.ids().next("CMD"),actor,reservationId,app.operations()));
                    }
                    case "4" -> {
                        String actor=ask(sc,"Supervisor actor"),lot=ask(sc,"Lot ID");
                        int delta=Integer.parseInt(ask(sc,"Signed delta (+/-)"));
                        execute(app,new AdjustCommand(app.ids().next("CMD"),actor,lot,delta,app.operations()));
                    }
                    case "5" -> {
                        String actor=ask(sc,"Supervisor actor"),lot=ask(sc,"Source lot ID"),
                               destination=ask(sc,"Destination warehouse [WH-02]");
                        if(destination.isBlank()) destination="WH-02";
                        int qty=Integer.parseInt(ask(sc,"Units"));
                        execute(app,new TransferCommand(app.ids().next("CMD"),actor,lot,destination,qty,app.operations()));
                    }
                    case "6" -> app.inventory().reservations().forEach(r->
                            System.out.println(r.id()+" owner="+r.owner()+" "+(r.active()?"ACTIVE":"RELEASED")+
                                    " allocations="+r.allocations()));
                    case "7" -> app.ledger().entries().forEach(System.out::println);
                    case "8" -> {
                        String choice=ask(sc,"Picking 1 FEFO / 2 FIFO / 3 LIFO");
                        PickingPolicy p=switch(choice) {
                            case "1"->new FefoPickingPolicy();case "2"->new FifoPickingPolicy();
                            case "3"->new LifoPickingPolicy();
                            default->throw new IllegalArgumentException("Unknown picking policy");
                        };
                        String sku=ask(sc,"SKU override (blank for all)");
                        if(sku.isBlank()) app.picking().changeDefault(p);else app.picking().changeForSku(sku,p);
                        System.out.println("Now using "+p.name());
                    }
                    case "9" -> {
                        String choice=ask(sc,"Replenishment 1 ROP / 2 MinMax / 3 EOQ");
                        ReplenishmentPolicy p=switch(choice) {
                            case "1"->new ReorderPointPolicy();case "2"->new MinMaxPolicy();
                            case "3"->new EoqPolicy();
                            default->throw new IllegalArgumentException("Unknown replenishment policy");
                        };
                        String sku=ask(sc,"SKU override (blank for all)");
                        if(sku.isBlank()) app.replenishment().changeDefault(p);
                        else app.replenishment().changeForSku(sku,p);
                        System.out.println("Now using "+p.name());
                    }
                    case "10" -> {
                        String sku=ask(sc,"SKU");
                        if(app.inventory().product(sku)==null) throw new IllegalArgumentException("Unknown SKU");
                        int stock=app.inventory().available(sku,"WH-01",LocalDate.now());
                        System.out.println(app.replenishment().recommend(SampleData.parameters(sku,stock)));
                        System.out.println("Recommendation only: no purchase order is created.");
                    }
                    case "11" -> {
                        String commandId=ask(sc,"Command ID to undo (CMD-000001)");
                        String actor=ask(sc,"Requesting actor");
                        print(app.invoker().undo(commandId,actor));
                    }
                    case "0" -> {return;}
                    default -> System.out.println("Unknown option");
                }
            } catch(IllegalArgumentException ex) {System.out.println("INVALID INPUT: "+ex.getMessage());}
        }
    }
    private static void inventory(SampleData.Application app) {
        for(Warehouse w:app.inventory().warehouses())
            System.out.println(w.id()+" staged="+w.staged()+"/"+w.stagingCapacity()+
                    " | storage="+app.inventory().warehouseOnHand(w.id())+"/"+w.storageCapacity());
        for(StockLot lot:app.inventory().lots())
            System.out.println(lot.id()+" sku="+lot.sku()+" wh="+lot.warehouseId()+" available="+
                    lot.available()+" reserved="+lot.reserved()+" received="+lot.receivedAt()+
                    " expires="+lot.expiresAt()+" quarantine="+lot.quarantine());
    }
    private static String ask(Scanner sc,String label) {
        System.out.print(label+": ");return sc.nextLine().trim();
    }
    private static void execute(SampleData.Application app,Command c) {print(app.invoker().execute(c));}
    private static void print(CommandResult r) {
        System.out.println((r.successful()?"OK":"REJECTED")+" | command="+r.commandId()+
            " | object="+r.objectId()+" | stage="+r.rejectedBy()+" | "+r.message());
    }
}
