package ec.edu.logistpulse.security;
public interface ActorDirectory {
    Role roleOf(String actor);
    default boolean isSupervisor(String actor) {return roleOf(actor)==Role.SUPERVISOR;}
}
