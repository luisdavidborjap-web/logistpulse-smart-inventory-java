package ec.edu.logistpulse;

import ec.edu.logistpulse.bootstrap.SampleData;
import ec.edu.logistpulse.command.*;
import ec.edu.logistpulse.domain.*;
import ec.edu.logistpulse.observer.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** End-to-end and unit tests for the actual State/Observer behavior. */
class StateObserverTest {
    private static final Clock TIME=Clock.fixed(Instant.parse("2026-11-14T14:00:00Z"),ZoneOffset.UTC);
    private final SampleData.Application app=SampleData.create(TIME);
    private CommandResult reserve(int qty,String lot) {
        return app.invoker().execute(new ReserveCommand(app.ids().next("CMD"),"ana","MED-002","WH-01",qty,lot,app.operations()));
    }
    private CommandResult ship(String actor,String reservation) {
        return app.invoker().execute(new ShipCommand(app.ids().next("CMD"),actor,reservation,app.operations()));
    }
    @Test void samplesStartInAppropriateStates() {
        assertEquals("STORED",app.inventory().lot("LOT-200").stateName());
        assertEquals("QUARANTINED",app.inventory().lot("LOT-201").stateName());
        assertEquals("RECEIVED",app.inventory().lot("LOT-400").stateName());
    }
    @Test void receivedLotCannotBeReservedBeforePutaway() {
        var cmd=new ReserveCommand("CMD-1","ana","TEC-001","WH-02",1,"LOT-400",app.operations());
        assertEquals("STOCK",app.invoker().execute(cmd).rejectedBy());
        assertEquals("RECEIVED",app.inventory().lot("LOT-400").stateName());
    }
    @Test void onlySupervisorCanStoreReceivedLot() {
        var denied=app.invoker().execute(new StoreLotCommand("CMD-1","ana","LOT-400",app.operations()));
        assertEquals("AUTHORIZATION",denied.rejectedBy());
        assertTrue(app.invoker().execute(new StoreLotCommand("CMD-2","supervisor","LOT-400",app.operations())).successful());
        assertEquals("STORED",app.inventory().lot("LOT-400").stateName());
    }
    @Test void storedLotBecomesReservedAndBack() {
        var r=reserve(2,"LOT-200");assertTrue(r.successful());
        assertEquals("RESERVED",app.inventory().lot("LOT-200").stateName());
        assertTrue(app.invoker().execute(new ReleaseCommand("CMD-200","ana",r.objectId(),app.operations())).successful());
        assertEquals("STORED",app.inventory().lot("LOT-200").stateName());
    }
    @Test void remainingAllocationKeepsLotReserved() {
        var r1=reserve(2,"LOT-200");var r2=reserve(2,"LOT-200");
        assertTrue(r1.successful() && r2.successful());
        app.invoker().execute(new ReleaseCommand("CMD-200","ana",r1.objectId(),app.operations()));
        assertEquals("RESERVED",app.inventory().lot("LOT-200").stateName());
        assertEquals(2,app.inventory().lot("LOT-200").reserved());
    }
    @Test void shippingAllUnitsReachesTerminalState() {
        var r=reserve(5,"LOT-200");assertTrue(r.successful());
        assertTrue(ship("ana",r.objectId()).successful());
        assertEquals("SHIPPED",app.inventory().lot("LOT-200").stateName());
        assertEquals(0,app.inventory().lot("LOT-200").onHand());
        assertEquals(0,app.inventory().lot("LOT-200").reserved());
        assertEquals(0,app.inventory().warehouse("WH-01").staged());
        assertEquals(ReservationStatus.SHIPPED,app.inventory().reservation(r.objectId()).status());
    }
    @Test void shippingPartOfBatchLeavesItStored() {
        var r=reserve(2,"LOT-200");assertTrue(ship("ana",r.objectId()).successful());
        assertEquals("STORED",app.inventory().lot("LOT-200").stateName());
        assertEquals(3,app.inventory().lot("LOT-200").onHand());
    }
    @Test void multiLotShippingChangesEachLotAppropriately() {
        var r=reserve(7,"AUTO");assertTrue(ship("ana",r.objectId()).successful());
        assertEquals("SHIPPED",app.inventory().lot("LOT-200").stateName());
        assertEquals("STORED",app.inventory().lot("LOT-203").stateName());
        assertEquals(4,app.inventory().lot("LOT-203").onHand());
    }
    @Test void foreignUserCannotShipAnotherReservation() {
        var r=reserve(2,"AUTO");int before=app.eventJournal().entries().size();
        assertEquals("AUTHORIZATION",ship("mallory",r.objectId()).rejectedBy());
        assertEquals(before,app.eventJournal().entries().size());
        assertTrue(app.inventory().reservation(r.objectId()).active());
    }
    @Test void shippedReservationCannotBeReleasedTwice() {
        var r=reserve(1,"AUTO");ship("ana",r.objectId());
        var result=app.invoker().execute(new ReleaseCommand("CMD-99","ana",r.objectId(),app.operations()));
        assertEquals("RESOURCE",result.rejectedBy());
    }
    @Test void shippingIsExplicitlyNonUndoable() {
        var r=reserve(1,"AUTO");var shipped=ship("ana",r.objectId());
        assertEquals("UNDO",app.invoker().undo(shipped.commandId(),"ana").rejectedBy());
        assertEquals(ReservationStatus.SHIPPED,app.inventory().reservation(r.objectId()).status());
    }
    @Test void quarantineRequiresSupervisorAndBlocksReservation() {
        assertEquals("AUTHORIZATION",app.invoker().execute(new QuarantineLotCommand("CMD-1","ana","LOT-200",app.operations())).rejectedBy());
        assertTrue(app.invoker().execute(new QuarantineLotCommand("CMD-2","supervisor","LOT-200",app.operations())).successful());
        assertEquals("QUARANTINED",app.inventory().lot("LOT-200").stateName());
        assertEquals("LOT",reserve(1,"LOT-200").rejectedBy());
    }
    @Test void cannotQuarantineLotWithActiveReservations() {
        reserve(1,"LOT-200");
        assertEquals("LOT",app.invoker().execute(new QuarantineLotCommand("CMD-99","supervisor","LOT-200",app.operations())).rejectedBy());
    }
    @Test void reinstateLotMakesItAvailableAgain() {
        app.invoker().execute(new QuarantineLotCommand("CMD-1","supervisor","LOT-200",app.operations()));
        assertTrue(app.invoker().execute(new ReinstateLotCommand("CMD-2","supervisor","LOT-200",app.operations())).successful());
        assertEquals("STORED",app.inventory().lot("LOT-200").stateName());
        assertTrue(reserve(1,"LOT-200").successful());
    }
    @Test void quarantineUndoIsValidWhenNoActiveReservations() {
        app.invoker().execute(new QuarantineLotCommand("CMD-1","supervisor","LOT-200",app.operations()));
        assertTrue(app.invoker().undo("CMD-1","supervisor").successful());
        assertEquals("STORED",app.inventory().lot("LOT-200").stateName());
    }
    @Test void shippedStateRejectsFutureMutation() {
        var r=reserve(5,"LOT-200");ship("ana",r.objectId());
        assertThrows(IllegalStateException.class,()->app.inventory().lot("LOT-200").adjust(2));
        assertThrows(IllegalStateException.class,()->app.inventory().lot("LOT-200").reserve(1));
    }
    @Test void stockAndStateEventsAreEmittedAfterSuccessfulReserve() {
        reserve(1,"LOT-200");
        var list=app.eventJournal().entries();
        assertTrue(list.stream().anyMatch(e->e instanceof StockChangedEvent));
        assertTrue(list.stream().anyMatch(e->e instanceof LotStateChangedEvent s
                && s.previousState().equals("STORED") && s.currentState().equals("RESERVED")));
        assertTrue(list.stream().anyMatch(e->e instanceof ReservationChangedEvent));
    }
    @Test void invalidRequestsPublishNoInventoryEvents() {
        var result=app.invoker().execute(new ReserveCommand("CMD-1","ana","MED-002","WH-01",1,"LOT-201",app.operations()));
        assertFalse(result.successful());assertTrue(app.eventJournal().entries().isEmpty());
    }
    @Test void exactIdempotentRetryProducesNoExtraEvents() {
        var cmd=new ReserveCommand("CMD-1","ana","MED-002","WH-01",2,"AUTO",app.operations());
        app.invoker().execute(cmd);int before=app.eventJournal().entries().size();
        app.invoker().execute(new ReserveCommand("CMD-1","ana","MED-002","WH-01",2,"AUTO",app.operations()));
        assertEquals(before,app.eventJournal().entries().size());
    }
    @Test void differentCommandIdSameEffectCreatesEvents() {
        reserve(1,"LOT-200");int before=app.eventJournal().entries().size();
        reserve(1,"LOT-200");assertTrue(app.eventJournal().entries().size()>before);
    }
    @Test void subscriberFailureDoesNotAbortInventoryOperation() {
        app.eventBus().subscribe(e->{throw new IllegalStateException("offline");});
        var result=reserve(1,"AUTO");
        assertTrue(result.successful());
        assertFalse(app.eventBus().deliveryFailures().isEmpty());
        assertFalse(app.eventJournal().entries().isEmpty());
    }
    @Test void subscribeAndUnsubscribeWork() {
        AtomicInteger count=new AtomicInteger();
        InventoryEventListener listener=e->count.incrementAndGet();
        app.eventBus().subscribe(listener);
        reserve(1,"AUTO");assertTrue(count.get()>0);
        int before=count.get();app.eventBus().unsubscribe(listener);
        reserve(1,"AUTO");assertEquals(before,count.get());
    }
    @Test void quarantineTriggersStateEventAndPotentialReplenishment() {
        var result=app.invoker().execute(new QuarantineLotCommand("CMD-1","supervisor","LOT-100",app.operations()));
        assertTrue(result.successful());
        assertTrue(app.eventJournal().entries().stream().anyMatch(e->e instanceof LotStateChangedEvent));
        assertFalse(app.replenishmentAlerts().notifications().isEmpty());
    }
    @Test void pulseFleetNotifiedForNewAndShippedReservations() {
        var r=reserve(1,"AUTO");
        assertTrue(app.pulseFleet().notifications().stream().anyMatch(s->s.contains("picking task")));
        ship("ana",r.objectId());
        assertTrue(app.pulseFleet().notifications().stream().anyMatch(s->s.contains("shipment completed")));
    }
    @Test void eventRecordsHaveConsistentFixedTimestampAndActor() {
        reserve(1,"AUTO");
        for (var e:app.eventJournal().entries()) {
            assertEquals(Instant.parse("2026-11-14T14:00:00Z"),e.occurredAt());
            assertEquals("ana",e.actor());
            assertEquals("RESERVE",e.operation());
        }
    }
    @Test void allFailedLotTransitionsKeepTheirOriginalState() {
        var lot=app.inventory().lot("LOT-201");
        assertThrows(IllegalStateException.class,()->lot.reserve(1));
        assertEquals("QUARANTINED",lot.stateName());assertEquals(0,lot.reserved());
    }
    @Test void transferredStockAndUndoEmitStockRemovedEvent() {
        var transfer=app.invoker().execute(new TransferCommand("CMD-1","supervisor","LOT-203","WH-02",2,app.operations()));
        assertTrue(transfer.successful());
        assertTrue(app.invoker().undo("CMD-1","supervisor").successful());
        assertTrue(app.eventJournal().entries().stream().anyMatch(e->e instanceof LotRemovedEvent));
    }
    @Test void reservationStateAndLotStateHaveIndependentSemantics() {
        var result=reserve(1,"LOT-200");
        assertEquals(ReservationStatus.ACTIVE,app.inventory().reservation(result.objectId()).status());
        assertEquals("RESERVED",app.inventory().lot("LOT-200").stateName());
        ship("ana",result.objectId());
        assertEquals(ReservationStatus.SHIPPED,app.inventory().reservation(result.objectId()).status());
        assertEquals("STORED",app.inventory().lot("LOT-200").stateName());
    }

    @Test void fingerprintCannotCollideWhenFieldsContainDelimiters() {
        var first=new ReserveCommand("CMD-1","ana","A|B","C",1,"AUTO",app.operations());
        var second=new ReserveCommand("CMD-1","ana","A","B|C",1,"AUTO",app.operations());
        assertFalse(first.fingerprint().equals(second.fingerprint()));
        assertEquals("RESOURCE",app.invoker().execute(first).rejectedBy());
        assertEquals("IDEMPOTENCY",app.invoker().execute(second).rejectedBy());
    }
    @Test void storeCommandUndoRestoresReceivedWhenUntouched() {
        var stored=app.invoker().execute(new StoreLotCommand("CMD-1","supervisor","LOT-400",app.operations()));
        assertTrue(stored.successful());
        assertEquals("STORED",app.inventory().lot("LOT-400").stateName());
        assertTrue(app.invoker().undo("CMD-1","supervisor").successful());
        assertEquals("RECEIVED",app.inventory().lot("LOT-400").stateName());
        assertTrue(app.eventJournal().entries().stream().anyMatch(e ->
            e instanceof LotStateChangedEvent x && x.operation().equals("UNSTORE")));
    }
    @Test void storeCommandUndoIsRejectedAfterLotWasReserved() {
        var stored=app.invoker().execute(new StoreLotCommand("CMD-1","supervisor","LOT-400",app.operations()));
        assertTrue(stored.successful());
        var reserved=app.invoker().execute(new ReserveCommand("CMD-2","ana","TEC-001","WH-02",1,"LOT-400",app.operations()));
        assertTrue(reserved.successful());
        assertTrue(app.invoker().execute(new ReleaseCommand("CMD-3","ana",reserved.objectId(),app.operations())).successful());
        assertEquals("LOT",app.invoker().undo("CMD-1","supervisor").rejectedBy());
        assertEquals("STORED",app.inventory().lot("LOT-400").stateName());
    }
    @Test void shippingMultilotDoesNotCreatePhantomExtraUnits() {
        int original=app.inventory().warehouseOnHand("WH-01");
        var r=reserve(7,"AUTO");
        assertEquals(original,app.inventory().warehouseOnHand("WH-01"));
        ship("ana",r.objectId());
        assertEquals(original-7,app.inventory().warehouseOnHand("WH-01"));
    }
    @Test void failedShipmentNeverEmitsEventsOrChangesQuantities() {
        var r=reserve(2,"LOT-200");
        var lot=app.inventory().lot("LOT-200");
        int beforeEvents=app.eventJournal().entries().size();
        var denied=ship("mallory",r.objectId());
        assertEquals("AUTHORIZATION",denied.rejectedBy());
        assertEquals(beforeEvents,app.eventJournal().entries().size());
        assertEquals(5,lot.onHand());assertEquals(2,lot.reserved());
    }
}
