package ec.edu.logistpulse.command;
import ec.edu.logistpulse.service.StockOperations;
import java.util.Objects;
public abstract class AbstractStockCommand implements Command {
    private final String id,actor;
    protected final StockOperations receiver;
    protected AbstractStockCommand(String id,String actor,StockOperations receiver) {
        if (id==null || id.isBlank() || actor==null || actor.isBlank())
            throw new IllegalArgumentException("Command ID and actor are required");
        this.id=id; this.actor=actor; this.receiver=Objects.requireNonNull(receiver);
    }
    public final String id() {return id;}
    public final String actor() {return actor;}
}
