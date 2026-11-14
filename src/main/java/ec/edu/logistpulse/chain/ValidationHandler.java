package ec.edu.logistpulse.chain;
import java.util.Objects;
/** Chain of Responsibility. All four operations go through the same ordered chain. */
public abstract class ValidationHandler {
    private ValidationHandler next;
    public final ValidationHandler linkWith(ValidationHandler successor) {
        this.next=Objects.requireNonNull(successor);
        return successor;
    }
    public final ValidationResult handle(ValidationContext context) {
        ValidationResult outcome=check(Objects.requireNonNull(context));
        if (!outcome.valid()) return outcome;
        return next==null?ValidationResult.allow():next.handle(context);
    }
    protected abstract ValidationResult check(ValidationContext context);
}
