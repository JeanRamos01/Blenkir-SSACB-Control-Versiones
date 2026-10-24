package pe.edu.blenkir.ssacb;

public final class AccountPasswordPolicy {
    private AccountPasswordPolicy(){}
    public static String requireInitial(String password){
        if(password==null || password.length()<12)
            throw new IllegalArgumentException("La contraseña inicial debe tener al menos 12 caracteres.");
        return password;
    }
}
