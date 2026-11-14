package ec.edu.logistpulse.chain;
import ec.edu.logistpulse.domain.Inventory;
import java.time.LocalDate;
public record ValidationContext(OperationRequest request, Inventory inventory, LocalDate today) { }
