package ec.edu.logistpulse.security;
import java.util.Map;
public final class DemoActorDirectory implements ActorDirectory {
    private final Map<String,Role> roles;
    public DemoActorDirectory(Map<String,Role> roles) {this.roles=Map.copyOf(roles);}
    public Role roleOf(String actor) {return roles.get(actor);}
}
