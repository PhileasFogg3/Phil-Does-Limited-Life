package org.phileasfogg3.limitedLife.Utils;

import java.util.UUID;

public class SoulmateLink {
    private final UUID partner;
    private boolean partnerExpired;
    private boolean selfExpired;

    public SoulmateLink(UUID partner, boolean partnerExpired, boolean selfExpired) {
        this.partner = partner;
        this.partnerExpired = partnerExpired;
        this.selfExpired = selfExpired;
    }

    public UUID getPartner() { return partner; }
    public boolean isPartnerExpired() { return partnerExpired; }
    public boolean isSelfExpired() { return selfExpired; }

    public void setPartnerExpired(boolean expired) { this.partnerExpired = expired; }
    public void setSelfExpired(boolean expired) { this.selfExpired = expired; }
}


