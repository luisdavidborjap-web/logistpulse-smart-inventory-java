package ec.edu.logistpulse;

import ec.edu.logistpulse.bootstrap.SampleData;
import ec.edu.logistpulse.command.*;
import ec.edu.logistpulse.domain.*;
import ec.edu.logistpulse.observer.*;
import java.time.*;
import java.util.concurrent.atomic.AtomicInteger;

/** Executable black-box checks requiring only JDK 17 (no JUnit/Maven). */
public final class StateObserverSmokeTest {
    private static final Clock TIME=Clock.fixed(Instant.parse("2026-11-14T14:00:00Z"),ZoneOffset.UTC);
    private static int checks;
    private static void ok(boolean condition,String explanation) {
        checks++;
        if (!condition) throw new AssertionError("State/Observer: " + explanation);
    }
    private static CommandResult reserve(SampleData.Application a,int qty,String lot) {
        return a.invoker().execute(new ReserveCommand(a.ids().next("CMD"),"ana","MED-002","WH-01",qty,lot,a.operations()));
    }
    public static void main(String[] args) {
        var a=SampleData.create(TIME);
        ok(a.inventory().lot("LOT-200").stateName().equals("STORED"),"initial stored state");
        ok(a.inventory().lot("LOT-400").stateName().equals("RECEIVED"),"initial received state");
        ok(a.inventory().lot("LOT-201").stateName().equals("QUARANTINED"),"initial quarantine state");
        ok(a.invoker().execute(new StoreLotCommand("CMD-100","ana","LOT-400",a.operations())).rejectedBy().equals("AUTHORIZATION"),"unauthorized storage denied");
        ok(a.invoker().execute(new StoreLotCommand("CMD-101","supervisor","LOT-400",a.operations())).successful(),"received lot stored");
        ok(a.inventory().lot("LOT-400").stateName().equals("STORED"),"received->stored");
        var res=reserve(a,5,"LOT-200");
        ok(res.successful(),"reserve lot");
        ok(a.inventory().lot("LOT-200").stateName().equals("RESERVED"),"stored->reserved");
        ok(a.inventory().warehouse("WH-01").staged()==5,"staging accounted");
        ok(a.eventJournal().entries().stream().anyMatch(e->e instanceof ReservationChangedEvent),"reservation event");
        var denied=a.invoker().execute(new ShipCommand("CMD-102","mallory",res.objectId(),a.operations()));
        ok(denied.rejectedBy().equals("AUTHORIZATION"),"no foreign shipment");
        int before=a.eventJournal().entries().size();
        ok(a.invoker().execute(new ShipCommand("CMD-103","ana",res.objectId(),a.operations())).successful(),"authorized shipment");
        ok(a.inventory().lot("LOT-200").stateName().equals("SHIPPED"),"reserved->shipped");
        ok(a.inventory().lot("LOT-200").onHand()==0 && a.inventory().lot("LOT-200").reserved()==0,"ship consumes physical and reserved stock");
        ok(a.inventory().warehouse("WH-01").staged()==0,"ship frees staging");
        ok(a.eventJournal().entries().size()>before,"ship events published");
        ok(a.pulseFleet().notifications().stream().anyMatch(s->s.contains("shipment completed")),"fleet observer notified");
        ok(a.invoker().undo("CMD-103","ana").rejectedBy().equals("UNDO"),"shipment intentionally irreversible");
        var b=SampleData.create(TIME);
        ok(b.invoker().execute(new QuarantineLotCommand("CMD-1","supervisor","LOT-100",b.operations())).successful(),"quality quarantine");
        ok(b.inventory().lot("LOT-100").stateName().equals("QUARANTINED"),"stored->quarantine");
        ok(!b.replenishmentAlerts().notifications().isEmpty(),"replenishment alert consumes state event");
        ok(b.invoker().execute(new ReinstateLotCommand("CMD-2","supervisor","LOT-100",b.operations())).successful(),"quality reinstatement");
        ok(b.inventory().lot("LOT-100").stateName().equals("STORED"),"quarantine->stored");
        var c=SampleData.create(TIME);
        c.eventBus().subscribe(e->{throw new IllegalStateException("subscriber down");});
        AtomicInteger seen=new AtomicInteger();InventoryEventListener listener=e->seen.incrementAndGet();
        c.eventBus().subscribe(listener);
        var r=reserve(c,1,"AUTO");
        ok(r.successful(),"failing subscriber must not abort reservation");
        ok(!c.eventBus().deliveryFailures().isEmpty(),"subscriber failure logged");
        ok(seen.get()>0,"observer subscription received event");
        c.eventBus().unsubscribe(listener);int n=seen.get();
        reserve(c,1,"AUTO");
        ok(n==seen.get(),"unsubscription works");
        var d=SampleData.create(TIME);
        var first=d.invoker().execute(new ReserveCommand("CMD-1","ana","MED-002","WH-01",1,"AUTO",d.operations()));
        int published=d.eventJournal().entries().size();
        var second=d.invoker().execute(new ReserveCommand("CMD-1","ana","MED-002","WH-01",1,"AUTO",d.operations()));
        ok(first.successful() && second.successful() && published==d.eventJournal().entries().size(),"idempotency emits once");
        var transfer=d.invoker().execute(new TransferCommand("CMD-2","supervisor","LOT-203","WH-02",2,d.operations()));
        ok(transfer.successful(),"transfer success");
        ok(d.invoker().undo("CMD-2","supervisor").successful(),"transfer undo success");
        ok(d.eventJournal().entries().stream().anyMatch(e->e instanceof LotRemovedEvent),"removed transferred lot is observable");
        System.out.println("STATE + OBSERVER SMOKE SUCCESS: "+checks+" checks");
    }
}
