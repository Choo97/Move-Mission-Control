package com.moving.reservation.availability;

public record PublicOperatingPolicyResponse(OperatingServiceMode serviceMode) {
    public static PublicOperatingPolicyResponse from(OperatingPolicy policy) {
        return new PublicOperatingPolicyResponse(policy.getServiceMode());
    }
}
