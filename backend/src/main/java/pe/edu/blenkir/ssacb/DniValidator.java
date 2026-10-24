package pe.edu.blenkir.ssacb;

public final class DniValidator {
    private DniValidator() {}

    public static String normalize(String dni) {
        return dni == null ? null : dni.trim();
    }

    public static boolean isValid(String dni) {
        String normalized = normalize(dni);
        return normalized != null && normalized.matches("^\\d{8}$");
    }

    public static String requireValid(String dni) {
        String normalized = normalize(dni);
        if (!isValid(normalized)) {
            throw new IllegalArgumentException("El DNI debe tener exactamente 8 dígitos numéricos.");
        }
        return normalized;
    }
}
