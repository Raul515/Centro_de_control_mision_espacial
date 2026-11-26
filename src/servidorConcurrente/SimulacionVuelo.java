package servidorConcurrente;
import herramientas.EstadoMision;


public class SimulacionVuelo implements Runnable{

    private final EstadoCohete estadoCohete;
    
    // Constantes físicas de la simulación (por cada segundo / tick)
    private static final double ALTITUD_OBJETIVO = 200.0;        // km para alcanzar órbita
    private static final double INCREMENTO_ALTITUD = 12.5;       // km/s
    private static final double INCREMENTO_VELOCIDAD = 450.0;    // m/s por tick
    private static final double GASTO_COMBUSTIBLE = 8.0;         // % por tick
    private static final double UMBRAL_COMBUSTIBLE_BAJO = 20.0;  // % para avisar en etapa 1


    public SimulacionVuelo(EstadoCohete estadoCohete) {
        this.estadoCohete = estadoCohete;
    }

    @Override
    public void run() {
        switch (estadoCohete.getEstado()) {
            case EstadoMision.CUENTA_ATRAS:
                int segundosRestantes = estadoCohete.getSegundosRestantes();
                if(segundosRestantes>0){
                    ServidorCentralMision.difundir("[CUENTA ATRAS] T-" + segundosRestantes + " segundos");
                    estadoCohete.cuentaAtras();
                }else{
                    estadoCohete.cuentaAtras();
                    ServidorCentralMision.difundir("[DESPEGUE] T-0! ¡¡DESPEGAMOS!!");
                }
                break;
            
            case EstadoMision.VUELO:
                estadoCohete.actualizarParametrosVuelo(INCREMENTO_ALTITUD, INCREMENTO_VELOCIDAD, GASTO_COMBUSTIBLE);
                ServidorCentralMision.difundir(estadoCohete.toStringTelemetriaVuelo());

                if (estadoCohete.getCantidadCombustible() <= 0.0) {
                    if (estadoCohete.abortarMision("Combustible agotado")) {
                        ServidorCentralMision.difundir("[ALARMA] ABORTO AUTOMATICO: Combustible agotado. Motores apagados.");
                    }
                } else if (estadoCohete.getAltitud() >= ALTITUD_OBJETIVO) {
                    estadoCohete.setEstado(EstadoMision.COMPLETADA);
                    ServidorCentralMision.difundir("[MISION COMPLETADA] Orbita alcanzada: " + ALTITUD_OBJETIVO + " km.");
                } else if (estadoCohete.getEtapa() == 1 && estadoCohete.getCantidadCombustible() <= UMBRAL_COMBUSTIBLE_BAJO) {
                    // Aviso a PROPULSION / DINAMICA para que ejecuten SEPARAR_ETAPAS
                    ServidorCentralMision.difundir("[ALERTA] Combustible bajo en Etapa 1 ("
                            + estadoCohete.getCantidadCombustible() + "%). Se requiere SEPARAR_ETAPAS.");
                }
                break;
            
            default:
                break;
        }
    }

}
