package ec.edu.logistpulse.domain;
public record Allocation(String lotId, int quantity) {
    public Allocation { if (lotId == null || lotId.isBlank() || quantity <= 0)
        throw new IllegalArgumentException("Invalid lot allocation"); }
}
