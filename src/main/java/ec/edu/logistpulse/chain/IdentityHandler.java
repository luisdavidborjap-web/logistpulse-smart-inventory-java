package ec.edu.logistpulse.chain;
import ec.edu.logistpulse.security.ActorDirectory;
import java.util.Objects;
public final class IdentityHandler extends ValidationHandler {
    private final ActorDirectory actors;
    public IdentityHandler(ActorDirectory actors) {this.actors=Objects.requireNonNull(actors);}
    protected ValidationResult check(ValidationContext ctx) {
        String actor=ctx.request().actor();
        if (actor==null || actor.isBlank() || actors.roleOf(actor)==null)
            return ValidationResult.reject("IDENTITY","Unknown actor; demo roles require registration");
        return ValidationResult.allow();
    }
}
