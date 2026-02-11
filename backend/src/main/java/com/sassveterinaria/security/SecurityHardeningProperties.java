package com.sassveterinaria.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.security")
public class SecurityHardeningProperties {

    private final TwoFa twoFa = new TwoFa();
    private final Lockout lockout = new Lockout();
    private final RateLimit rateLimit = new RateLimit();

    public TwoFa getTwoFa() {
        return twoFa;
    }

    public Lockout getLockout() {
        return lockout;
    }

    public RateLimit getRateLimit() {
        return rateLimit;
    }

    public static class TwoFa {
        private boolean enforcementEnabled = true;
        private boolean allowLoginWithoutEnrollment = true;
        private long challengeSeconds = 300;
        private String issuer = "SaaSVeterinaria";

        public boolean isEnforcementEnabled() {
            return enforcementEnabled;
        }

        public void setEnforcementEnabled(boolean enforcementEnabled) {
            this.enforcementEnabled = enforcementEnabled;
        }

        public boolean isAllowLoginWithoutEnrollment() {
            return allowLoginWithoutEnrollment;
        }

        public void setAllowLoginWithoutEnrollment(boolean allowLoginWithoutEnrollment) {
            this.allowLoginWithoutEnrollment = allowLoginWithoutEnrollment;
        }

        public long getChallengeSeconds() {
            return challengeSeconds;
        }

        public void setChallengeSeconds(long challengeSeconds) {
            this.challengeSeconds = challengeSeconds;
        }

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }
    }

    public static class Lockout {
        private int maxAttempts = 4;
        private int windowMinutes = 15;
        private int durationMinutes = 15;

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public int getWindowMinutes() {
            return windowMinutes;
        }

        public void setWindowMinutes(int windowMinutes) {
            this.windowMinutes = windowMinutes;
        }

        public int getDurationMinutes() {
            return durationMinutes;
        }

        public void setDurationMinutes(int durationMinutes) {
            this.durationMinutes = durationMinutes;
        }
    }

    public static class RateLimit {
        private int loginLimit = 10;
        private int loginWindowSeconds = 900;
        private int refreshLimit = 30;
        private int refreshWindowSeconds = 900;
        private int reportLimit = 20;
        private int reportWindowSeconds = 300;

        public int getLoginLimit() {
            return loginLimit;
        }

        public void setLoginLimit(int loginLimit) {
            this.loginLimit = loginLimit;
        }

        public int getLoginWindowSeconds() {
            return loginWindowSeconds;
        }

        public void setLoginWindowSeconds(int loginWindowSeconds) {
            this.loginWindowSeconds = loginWindowSeconds;
        }

        public int getRefreshLimit() {
            return refreshLimit;
        }

        public void setRefreshLimit(int refreshLimit) {
            this.refreshLimit = refreshLimit;
        }

        public int getRefreshWindowSeconds() {
            return refreshWindowSeconds;
        }

        public void setRefreshWindowSeconds(int refreshWindowSeconds) {
            this.refreshWindowSeconds = refreshWindowSeconds;
        }

        public int getReportLimit() {
            return reportLimit;
        }

        public void setReportLimit(int reportLimit) {
            this.reportLimit = reportLimit;
        }

        public int getReportWindowSeconds() {
            return reportWindowSeconds;
        }

        public void setReportWindowSeconds(int reportWindowSeconds) {
            this.reportWindowSeconds = reportWindowSeconds;
        }
    }
}
