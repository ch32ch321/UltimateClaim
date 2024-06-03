package ultimateclaim.claim;

import java.util.HashSet;
import java.util.Set;

public class ClaimSettings {
    private final Set<ClaimSetting> settings = new HashSet<>();

    public ClaimSettings setEnabled(ClaimSetting setting, boolean enabled) {
        if (enabled) {
            this.settings.add(setting);
        } else {
            this.settings.remove(setting);
        }

        return this;
    }

    public boolean isEnabled(ClaimSetting setting) {
        return this.settings.contains(setting);
    }

    public String getStatus(ClaimSetting setting) {
        return isEnabled(setting) ? "true" : "false";
    }
}
