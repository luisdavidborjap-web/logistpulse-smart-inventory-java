package ec.edu.logistpulse.domain;
public final class Warehouse {
    private final String id;
    private final int stagingCapacity;
    private final int storageCapacity;
    private int staged;
    public Warehouse(String id, int stagingCapacity, int storageCapacity) {
        if (id == null || id.isBlank() || stagingCapacity < 0 || storageCapacity < 0)
            throw new IllegalArgumentException("Invalid warehouse");
        this.id=id; this.stagingCapacity=stagingCapacity; this.storageCapacity=storageCapacity;
    }
    public String id() { return id; }
    public int stagingCapacity() { return stagingCapacity; }
    public int storageCapacity() { return storageCapacity; }
    public int staged() { return staged; }
    public int freeStaging() { return stagingCapacity-staged; }
    public void stage(int units) {
        if (units <= 0 || units > freeStaging()) throw new IllegalStateException("Insufficient staging capacity");
        staged+=units;
    }
    public void unstage(int units) {
        if (units <= 0 || units > staged) throw new IllegalStateException("Invalid unstaging amount");
        staged-=units;
    }
}
