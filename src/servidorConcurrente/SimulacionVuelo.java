package servidorConcurrente;
import herramientas.EstadoMision;


public class SimulacionVuelo implements Runnable{

    private final EstadoCohete estadoCohete;
    private double altitudObjetivo;
    private double velocidadActual;
    private double altitudActual;
    private double consumoCombustible;
    private double umbralCombustibleBajoEtapa1;


    public SimulacionVuelo(EstadoCohete estadoCohete) {
        this.estadoCohete = estadoCohete;
    }



    @Override
    public void run() {
        switch (estadoCohete.getEstado()) {
            case EstadoMision.CUENTA_ATRAS:
                // implementar logica cuenda esta en la cuenta atras
                break;
            
            case EstadoMision.VUELO:
                // implementar logica cuando esta en vuelo
                break;
            
            default:
                break;
        }
    }

}
