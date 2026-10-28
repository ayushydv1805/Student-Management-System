package edu.college.sms.servlet;

/** Small input-validation helpers shared by servlet handlers. */
public final class RequestValidation {
    private RequestValidation() {}

    /**
     * Returns a positive 32-bit identifier, or null for missing, zero,
     * negative, malformed, or out-of-range input.
     */
    public static Integer positiveInt(String value) {
        if (value == null || !value.matches("\\d{1,10}")) return null;
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
