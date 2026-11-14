package ec.edu.logistpulse;
import ec.edu.logistpulse.bootstrap.SampleData;
import ec.edu.logistpulse.command.*;
import ec.edu.logistpulse.domain.*;
import ec.edu.logistpulse.strategy.picking.*;
import ec.edu.logistpulse.strategy.replenishment.*;
import java.time.*;
public final class ReviewSmokeTest {
    private static final Clock TIME=Clock.fixed(Instant.parse("2026-11-14T14:00:00Z"),ZoneOffset.UTC);
    private static int assertions=0;
    private static void check(boolean condition,String message) {
        assertions++;
        if(!condition) throw new AssertionError(message);
    }
    private static SampleData.Application app(){return SampleData.create(TIME);}
    public static void main(String[] args) {
        var a=app();
        String command=a.ids().next("CMD");
        check(command.equals("CMD-000001"),"Numbered command");
        Command reserve=new ReserveCommand(command,"ana","MED-002","WH-01",7,"AUTO",a.operations());
        CommandResult r=a.invoker().execute(reserve);
        check(r.successful(),"Multi-lot reservation succeeded");
        check(r.objectId().equals("RES-000001"),"Numbered reservation");
        var reservation=a.inventory().reservation(r.objectId());
        check(reservation.allocations().size()==2,"Allocation split");
        check(reservation.allocations().get(0).equals(new Allocation("LOT-200",5)),"FEFO first lot");
        check(reservation.allocations().get(1).equals(new Allocation("LOT-203",2)),"FEFO second lot");
        check(a.inventory().warehouse("WH-01").staged()==7,"Staging capacity updated");
        check(a.invoker().execute(new ReserveCommand(command,"ana","MED-002","WH-01",7,"AUTO",a.operations())).objectId().equals(r.objectId()),"Retry same ID");
        check(a.inventory().warehouse("WH-01").staged()==7,"Retry does not duplicate stock");
        check(a.invoker().execute(new ReserveCommand(command,"ana","MED-002","WH-01",8,"AUTO",a.operations())).rejectedBy().equals("IDEMPOTENCY"),"Conflicting payload rejected");
        var forbidden=a.invoker().execute(new ReleaseCommand(a.ids().next("CMD"),"mallory",r.objectId(),a.operations()));
        check(!forbidden.successful()&&forbidden.rejectedBy().equals("AUTHORIZATION"),"Foreign owner may not release");
        var released=a.invoker().execute(new ReleaseCommand(a.ids().next("CMD"),"ana",r.objectId(),a.operations()));
        check(released.successful(),"Owner releases");
        check(a.inventory().warehouse("WH-01").staged()==0,"Release restores staging");
        var restored=a.invoker().undo(released.commandId(),"ana");
        check(restored.successful()&&reservation.active(),"Release undo restores reserve");
        check(a.invoker().undo(command,"ana").successful(),"Reserve undo releases");
        check(!reservation.active(),"Original reservation inactive after undo");
        check(!a.invoker().undo(command,"mallory").successful() || !reservation.active(),"Replay undo no stock duplication");
        var b=app();
        b.picking().changeDefault(new FifoPickingPolicy());
        var fifo=b.invoker().execute(new ReserveCommand("CMD-10","ana","MED-002","WH-01",1,"AUTO",b.operations()));
        check(b.inventory().reservation(fifo.objectId()).allocations().get(0).lotId().equals("LOT-203"),"FIFO ordering");
        var c=app();
        c.picking().changeDefault(new LifoPickingPolicy());
        var lifo=c.invoker().execute(new ReserveCommand("CMD-11","ana","MED-002","WH-01",1,"AUTO",c.operations()));
        check(c.inventory().reservation(lifo.objectId()).allocations().get(0).lotId().equals("LOT-204"),"LIFO ordering");
        var d=app();
        check(!d.invoker().execute(new ReserveCommand("CMD-12","ana","MED-002","WH-01",1,"LOT-201",d.operations())).successful(),"Quarantine rejected");
        check(!d.invoker().execute(new ReserveCommand("CMD-13","ana","MED-002","WH-01",1,"LOT-202",d.operations())).successful(),"Expired rejected");
        check(d.invoker().execute(new ReserveCommand("CMD-14","ana","MED-002","WH-01",13,"AUTO",d.operations())).rejectedBy().equals("CAPACITY"),"Capacity rejected");
        check(d.invoker().execute(new ReserveCommand("CMD-15","unknown","MED-002","WH-01",1,"AUTO",d.operations())).rejectedBy().equals("IDENTITY"),"Unknown actor rejected");
        check(d.invoker().execute(new AdjustCommand("CMD-16","ana","LOT-200",2,d.operations())).rejectedBy().equals("AUTHORIZATION"),"Adjust role denied");
        var adj=d.invoker().execute(new AdjustCommand("CMD-17","supervisor","LOT-200",2,d.operations()));
        check(adj.successful()&&d.inventory().lot("LOT-200").onHand()==7,"Supervisor adjustment");
        check(d.invoker().undo("CMD-17","supervisor").successful()&&d.inventory().lot("LOT-200").onHand()==5,"Adjustment undo");
        int origin=d.inventory().lot("LOT-203").onHand();
        var move=d.invoker().execute(new TransferCommand("CMD-18","supervisor","LOT-203","WH-02",2,d.operations()));
        check(move.successful() && move.objectId().startsWith("LOT-T-"),"Numbered transfer lot");
        check(d.inventory().lot("LOT-203").onHand()==origin-2,"Transfer debits origin");
        check(d.inventory().lot(move.objectId()).onHand()==2,"Transfer credits destination");
        check(d.invoker().undo("CMD-18","supervisor").successful(),"Transfer undo");
        check(d.inventory().lot(move.objectId())==null && d.inventory().lot("LOT-203").onHand()==origin,"Transfer undo conserves stock");
        check(d.invoker().execute(new TransferCommand("CMD-19","ana","LOT-203","WH-02",1,d.operations())).rejectedBy().equals("AUTHORIZATION"),"Unauthorized transfer denied");
        check(d.ledger().entries().stream().anyMatch(e->e.rejectedBy().equals("IDEMPOTENCY"))==false,"Isolated fixture ledger");
        var params=SampleData.parameters("MED-002",2);
        check(new ReorderPointPolicy().recommend(params).suggestedQuantity()>0,"ROP policy");
        check(new MinMaxPolicy().recommend(params).suggestedQuantity()>0,"MinMax policy");
        check(new EoqPolicy().recommend(params).suggestedQuantity()>0,"EOQ policy");
        System.out.println("SMOKE SUCCESS: "+assertions+" assertions across Command, Chain and Strategy");
    }
}
