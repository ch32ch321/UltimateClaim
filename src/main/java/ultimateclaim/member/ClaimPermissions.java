package ultimateclaim.member;

import java.util.HashSet;
import java.util.Set;


public class ClaimPermissions {
    private final Set<ClaimPerm> permissions = new HashSet<>();

    public ClaimPermissions setAllowed(ClaimPerm perm, boolean allowed) {
        if (allowed) {
            this.permissions.add(perm);
        } else {
            this.permissions.remove(perm);
        }
        return this;
    }

    public boolean hasPermission(ClaimPerm perm) {
        return this.permissions.contains(perm);
    }

    public String getStatus(ClaimPerm perm) {
        return hasPermission(perm) ? "true" : "false";
    }
}
