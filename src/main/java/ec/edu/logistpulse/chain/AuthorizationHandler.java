package ec.edu.logistpulse.chain;
import ec.edu.logistpulse.security.ActorDirectory;
import ec.edu.logistpulse.domain.Reservation;
import java.util.Objects;
public final class AuthorizationHandler extends ValidationHandler {
    private final ActorDirectory actors;
    public AuthorizationHandler(ActorDirectory actors) {this.actors=Objects.requireNonNull(actors);}
    protected ValidationResult check(ValidationContext ctx) {
        OperationRequest r=ctx.request();
        if ((r.type()==OperationType.RELEASE || r.type()==OperationType.SHIP)) {
            Reservation reservation=ctx.inventory().reservation(r.reservationId());
            if (!reservation.owner().equals(r.actor()) && !actors.isSupervisor(r.actor()))
                return ValidationResult.reject("AUTHORIZATION","Only reservation owner or supervisor may release it");
        }
        if ((r.type()==OperationType.ADJUST || r.type()==OperationType.TRANSFER || r.type()==OperationType.QUARANTINE
                || r.type()==OperationType.REINSTATE || r.type()==OperationType.STORE
                || r.type()==OperationType.UNSTORE)
                && !actors.isSupervisor(r.actor()))
            return ValidationResult.reject("AUTHORIZATION","Supervisor role required");
        return ValidationResult.allow();
    }
}
