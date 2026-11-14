package ec.edu.logistpulse;
import ec.edu.logistpulse.bootstrap.SampleData;
import ec.edu.logistpulse.command.*;
import ec.edu.logistpulse.domain.*;
import ec.edu.logistpulse.strategy.picking.*;
import ec.edu.logistpulse.strategy.replenishment.*;
import java.time.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
public class DesignReviewTest {
    private static final Clock TIME=Clock.fixed(Instant.parse("2026-11-14T14:00:00Z"),ZoneOffset.UTC);
    private SampleData.Application a=SampleData.create(TIME);
    private CommandResult reserve(SampleData.Application f,String actor,int qty,String lot) {
        return reserveWithId(f,f.ids().next("CMD"),actor,qty,lot);
    }
    private CommandResult reserveWithId(SampleData.Application f,String cmdId,String actor,int qty,String lot) {
        return f.invoker().execute(new ReserveCommand(cmdId,actor,"MED-002","WH-01",qty,lot,f.operations()));
    }
    private CommandResult release(SampleData.Application f,String actor,String id) {
        return f.invoker().execute(new ReleaseCommand(f.ids().next("CMD"),actor,id,f.operations()));
    }
    @Test void numberedCommandIds() {
        assertEquals("CMD-000001",a.ids().next("CMD")); assertEquals("CMD-000002",a.ids().next("CMD"));
    }
    @Test void numberedReservationId() {
        var r=reserve(a,"ana",1,"AUTO");assertTrue(r.successful());assertEquals("RES-000001",r.objectId());
    }
    @Test void commandIdentifiesTarget() {
        var c=new ReserveCommand("CMD-77","ana","MED-002","WH-01",1,"AUTO",a.operations()); assertEquals("CMD-77",c.id());assertEquals("MED-002",c.targetId());assertEquals("RESERVE",c.type());
    }
    @Test void commandIdempotentRetry() {
        var c=new ReserveCommand("CMD-1","ana","MED-002","WH-01",1,"AUTO",a.operations());var r=a.invoker().execute(c); var again=a.invoker().execute(new ReserveCommand("CMD-1","ana","MED-002","WH-01",1,"AUTO",a.operations())); assertEquals(r.objectId(),again.objectId());assertEquals(1,a.inventory().warehouse("WH-01").staged());
    }
    @Test void differentPayloadSameIdRejected() {
        reserveWithId(a,"CMD-1","ana",1,"AUTO");var r=reserveWithId(a,"CMD-1","ana",2,"AUTO");assertFalse(r.successful());assertEquals("IDEMPOTENCY",r.rejectedBy());
    }
    @Test void sameIdDifferentActorRejected() {
        reserveWithId(a,"CMD-1","ana",1,"AUTO");var r=reserveWithId(a,"CMD-1","mallory",1,"AUTO");assertEquals("IDEMPOTENCY",r.rejectedBy());
    }
    @Test void failedCommandIsMemoized() {
        reserveWithId(a,"CMD-1","ana",1,"LOT-201");int n=a.ledger().entries().size();reserveWithId(a,"CMD-1","ana",1,"LOT-201");assertEquals(n,a.ledger().entries().size());
    }
    @Test void onlyOwnerCanRelease() {
        var r=reserve(a,"ana",1,"AUTO");var result=release(a,"mallory",r.objectId());assertEquals("AUTHORIZATION",result.rejectedBy());assertTrue(a.inventory().reservation(r.objectId()).active());
    }
    @Test void supervisorCanRelease() {
        var r=reserve(a,"ana",1,"AUTO");assertTrue(release(a,"supervisor",r.objectId()).successful());
    }
    @Test void ownerCanRelease() {
        var r=reserve(a,"ana",1,"AUTO");assertTrue(release(a,"ana",r.objectId()).successful());assertEquals(0,a.inventory().warehouse("WH-01").staged());
    }
    @Test void releaseTwiceRejected() {
        var r=reserve(a,"ana",1,"AUTO");release(a,"ana",r.objectId());assertEquals("RESOURCE",release(a,"ana",r.objectId()).rejectedBy());
    }
    @Test void undoReserve() {
        var cmd=new ReserveCommand("CMD-1","ana","MED-002","WH-01",1,"AUTO",a.operations());a.invoker().execute(cmd);assertTrue(a.invoker().undo("CMD-1","ana").successful());assertEquals(0,a.inventory().warehouse("WH-01").staged());
    }
    @Test void undoRelease() {
        var r=reserve(a,"ana",1,"AUTO");var cmd=new ReleaseCommand("CMD-5","ana",r.objectId(),a.operations());a.invoker().execute(cmd);assertTrue(a.invoker().undo("CMD-5","ana").successful());assertTrue(a.inventory().reservation(r.objectId()).active());
    }
    @Test void unauthorizedUndoRejected() {
        reserveWithId(a,"CMD-1","ana",1,"AUTO");var result=a.invoker().undo("CMD-1","mallory");assertEquals("AUTHORIZATION",result.rejectedBy());assertEquals(1,a.inventory().warehouse("WH-01").staged());
    }
    @Test void repeatedUndoDoesNotMutate() {
        reserveWithId(a,"CMD-1","ana",1,"AUTO");assertTrue(a.invoker().undo("CMD-1","ana").successful());assertTrue(a.invoker().undo("CMD-1","ana").successful());assertEquals(0,a.inventory().warehouse("WH-01").staged());
    }
    @Test void alreadyUndoneCommandCannotBeReplayed() {
        reserveWithId(a,"CMD-1","ana",1,"AUTO");a.invoker().undo("CMD-1","ana");assertEquals("UNDO",reserveWithId(a,"CMD-1","ana",1,"AUTO").rejectedBy());
    }
    @Test void unknownActorRejected() {
        var r=reserve(a,"stranger",1,"AUTO");assertEquals("IDENTITY",r.rejectedBy());
    }
    @Test void unknownSkuRejected() {
        var r=a.invoker().execute(new ReserveCommand("CMD-1","ana","INVALID","WH-01",1,"AUTO",a.operations()));assertEquals("RESOURCE",r.rejectedBy());
    }
    @Test void invalidWarehouseRejected() {
        var r=a.invoker().execute(new ReserveCommand("CMD-1","ana","MED-002","WH-999",1,"AUTO",a.operations()));assertEquals("RESOURCE",r.rejectedBy());
    }
    @Test void insufficientStockRejected() {
        var r=reserve(a,"ana",100,"AUTO");assertEquals("STOCK",r.rejectedBy());
    }
    @Test void invalidQtyRejected() {
        var r=reserve(a,"ana",0,"AUTO");assertEquals("STOCK",r.rejectedBy());
    }
    @Test void capacityRejected() {
        var r=reserve(a,"ana",13,"AUTO");assertEquals("CAPACITY",r.rejectedBy());
    }
    @Test void quarantineRejected() {
        assertEquals("LOT",reserve(a,"ana",1,"LOT-201").rejectedBy());
    }
    @Test void expiryRejected() {
        assertEquals("LOT",reserve(a,"ana",1,"LOT-202").rejectedBy());
    }
    @Test void unknownLotRejected() {
        assertEquals("LOT",reserve(a,"ana",1,"LOT-999").rejectedBy());
    }
    @Test void lotWrongSkuRejected() {
        assertEquals("LOT",reserve(a,"ana",1,"LOT-300").rejectedBy());
    }
    @Test void fefoPicksSoonestExpiration() {
        var r=reserve(a,"ana",1,"AUTO");assertEquals("LOT-200",a.inventory().reservation(r.objectId()).allocations().get(0).lotId());
    }
    @Test void fifoPicksOldestReceipt() {
        a.picking().changeDefault(new FifoPickingPolicy());var r=reserve(a,"ana",1,"AUTO");assertEquals("LOT-203",a.inventory().reservation(r.objectId()).allocations().get(0).lotId());
    }
    @Test void lifoPicksNewestReceipt() {
        a.picking().changeDefault(new LifoPickingPolicy());var r=reserve(a,"ana",1,"AUTO");assertEquals("LOT-204",a.inventory().reservation(r.objectId()).allocations().get(0).lotId());
    }
    @Test void perSkuPolicyOverridesGlobal() {
        a.picking().changeForSku("MED-002",new LifoPickingPolicy());var r=reserve(a,"ana",1,"AUTO");assertEquals("LOT-204",a.inventory().reservation(r.objectId()).allocations().get(0).lotId());
    }
    @Test void reservationSpansMultipleLots() {
        var r=reserve(a,"ana",7,"AUTO");var allocations=a.inventory().reservation(r.objectId()).allocations();assertEquals(2,allocations.size());assertEquals(new Allocation("LOT-200",5),allocations.get(0));assertEquals(new Allocation("LOT-203",2),allocations.get(1));
    }
    @Test void manualPickingSingleLot() {
        var r=reserve(a,"ana",2,"LOT-203");assertTrue(r.successful());assertEquals("LOT-203",a.inventory().reservation(r.objectId()).allocations().get(0).lotId());
    }
    @Test void manualLotInsufficientStock() {
        assertEquals("STOCK",reserve(a,"ana",6,"LOT-200").rejectedBy());
    }
    @Test void adjustRequiresSupervisor() {
        var r=a.invoker().execute(new AdjustCommand("CMD-1","ana","LOT-200",1,a.operations()));assertEquals("AUTHORIZATION",r.rejectedBy());
    }
    @Test void supervisorCanAdjust() {
        var r=a.invoker().execute(new AdjustCommand("CMD-1","supervisor","LOT-200",3,a.operations()));assertTrue(r.successful());assertEquals(8,a.inventory().lot("LOT-200").onHand());
    }
    @Test void adjustCannotTouchReservedStock() {
        reserve(a,"ana",5,"LOT-200");var r=a.invoker().execute(new AdjustCommand("CMD-1","supervisor","LOT-200",-1,a.operations()));assertEquals("STOCK",r.rejectedBy());
    }
    @Test void adjustUndoRestoresQuantity() {
        a.invoker().execute(new AdjustCommand("CMD-1","supervisor","LOT-200",3,a.operations()));assertTrue(a.invoker().undo("CMD-1","supervisor").successful());assertEquals(5,a.inventory().lot("LOT-200").onHand());
    }
    @Test void transferRequiresSupervisor() {
        var r=a.invoker().execute(new TransferCommand("CMD-1","ana","LOT-200","WH-02",2,a.operations()));assertEquals("AUTHORIZATION",r.rejectedBy());
    }
    @Test void transferConservesStock() {
        int before=a.inventory().lot("LOT-200").onHand();var r=a.invoker().execute(new TransferCommand("CMD-1","supervisor","LOT-200","WH-02",2,a.operations()));assertTrue(r.successful());assertEquals(before-2,a.inventory().lot("LOT-200").onHand());assertEquals(2,a.inventory().lot(r.objectId()).onHand());
    }
    @Test void transferHasNumberedLot() {
        var r=a.invoker().execute(new TransferCommand("CMD-1","supervisor","LOT-200","WH-02",2,a.operations()));assertEquals("LOT-T-000001",r.objectId());
    }
    @Test void transferToSameWarehouseRejected() {
        var r=a.invoker().execute(new TransferCommand("CMD-1","supervisor","LOT-200","WH-01",2,a.operations()));assertEquals("LOT",r.rejectedBy());
    }
    @Test void transferUndoRestoresStock() {
        var r=a.invoker().execute(new TransferCommand("CMD-1","supervisor","LOT-200","WH-02",2,a.operations()));assertTrue(a.invoker().undo("CMD-1","supervisor").successful());assertNull(a.inventory().lot(r.objectId()));assertEquals(5,a.inventory().lot("LOT-200").onHand());
    }
    @Test void transferUndoFailsIfDestinationReserved() {
        var r=a.invoker().execute(new TransferCommand("CMD-1","supervisor","LOT-200","WH-02",2,a.operations()));var reservation=a.invoker().execute(new ReserveCommand("CMD-2","ana","MED-002","WH-02",1,"AUTO",a.operations()));assertTrue(reservation.successful());assertEquals("UNDO",a.invoker().undo("CMD-1","supervisor").rejectedBy());
    }
    @Test void replenishmentRop() {
        var p=SampleData.parameters("MED-002",1);assertTrue(new ReorderPointPolicy().recommend(p).suggestedQuantity()>0);
    }
    @Test void replenishmentMinMax() {
        var p=SampleData.parameters("MED-002",1);assertEquals(39,new MinMaxPolicy().recommend(p).suggestedQuantity());
    }
    @Test void replenishmentEoq() {
        var p=SampleData.parameters("MED-002",1);assertTrue(new EoqPolicy().recommend(p).suggestedQuantity()>0);
    }
    @Test void auditContainsRejectionStage() {
        reserve(a,"ana",1,"LOT-201");assertEquals("LOT",a.ledger().entries().get(0).rejectedBy());
    }
    @Test void auditContainsIdsAndActor() {
        reserveWithId(a,"CMD-1","ana",1,"AUTO");var entry=a.ledger().entries().get(0);assertEquals("CMD-1",entry.commandId());assertEquals("ana",entry.actor());assertEquals("MED-002",entry.targetId());
    }
    @Test void skuIsImmutable() {
        assertThrows(IllegalArgumentException.class,()->new Product("","invalid"));
    }
    @Test void lotInvariant() {
        var l=a.inventory().lot("LOT-200");assertThrows(IllegalStateException.class,()->l.adjust(-999));assertEquals(5,l.onHand());
    }
    @Test void warehouseInvariant() {
        assertThrows(IllegalStateException.class,()->a.inventory().warehouse("WH-01").stage(99));
    }
    @Test void transferUndoRejectedAfterDestinationWasEverReserved() {
        var transfer=a.invoker().execute(new TransferCommand("CMD-1","supervisor","LOT-200","WH-02",2,a.operations()));
        var reserve=a.invoker().execute(new ReserveCommand("CMD-2","ana","MED-002","WH-02",1,"AUTO",a.operations()));
        assertTrue(reserve.successful());
        assertTrue(a.invoker().execute(new ReleaseCommand("CMD-3","ana",reserve.objectId(),a.operations())).successful());
        assertEquals("UNDO",a.invoker().undo("CMD-1","supervisor").rejectedBy());
    }
    @Test void unauthorizedUndoAfterSuccessStillRejected() {
        reserveWithId(a,"CMD-1","ana",1,"AUTO");
        assertTrue(a.invoker().undo("CMD-1","ana").successful());
        assertEquals("AUTHORIZATION",a.invoker().undo("CMD-1","mallory").rejectedBy());
    }

}
