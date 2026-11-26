package servidorConcurrente;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import herramientas.*;


public class EstadoCohete {

    // Variables Telemetria
    private double altitud;
    private double velocidad;
    private double cantidadCombustible;
    private int etapa;
    private int segundosRestantes;

    // Variables Control
    private EstadoMision estado;
    private final Map<Rol, Boolean> votosSondeo;
    private final Set<Rol> rolesActivos;

    // Constructor
    public EstadoCohete(){
        this.altitud = 0.0;
        this.velocidad = 0.0;
        this.cantidadCombustible = 100.0;
        this.etapa = 1;
        this.segundosRestantes = 10;

        this.estado = EstadoMision.ESPERA;
        this.votosSondeo = new HashMap<>();
        this.rolesActivos = new HashSet<>();
    }


    // GETTERS Y SETTERS
    public double getAltitud() {
        return altitud;
    }

    public void setAltitud(double altitud) {
        this.altitud = altitud;
    }

    public double getVelocidad() {
        return velocidad;
    }

    public void setVelocidad(double velocidad) {
        this.velocidad = velocidad;
    }

    public double getCantidadCombustible() {
        return cantidadCombustible;
    }

    public void setCantidadCombustible(double cantidadCombustible) {
        this.cantidadCombustible = cantidadCombustible;
    }

    public int getEtapa() {
        return etapa;
    }

    public void setEtapa(int etapa) {
        this.etapa = etapa;
    }

    public int getSegundosRestantes() {
        return segundosRestantes;
    }

    public void setSegundosRestantes(int segundosRestantes) {
        this.segundosRestantes = segundosRestantes;
    }

    public EstadoMision getEstado() {
        return estado;
    }

    public void setEstado(EstadoMision estado) {
        this.estado = estado;
    }

    public Map<Rol, Boolean> getVotosSondeo() {
        return votosSondeo;
    }

    // Metodos 
    public synchronized boolean asignarRol(Rol rol){
        boolean estaAsigando = false;
        if (rol != null && !rolesActivos.contains(rol)){
            rolesActivos.add(rol);
            estaAsigando =  true;
        }
        return estaAsigando;
    }

    public synchronized void liberalRol(Rol rol){
        if(rol != null){
            rolesActivos.remove(rol);
            votosSondeo.remove(rol);
        }
    }

    public synchronized Set<Rol> getRolesActivos() {
        return rolesActivos;
    }

    public synchronized boolean empezarSondeo(){
        boolean seEmpieza = false;
        if(estado == EstadoMision.ESPERA){
            votosSondeo.clear();
            estado = EstadoMision.SONDEO;
            seEmpieza = true;
        }
        return seEmpieza;
    }

    public synchronized boolean registrarVoto(Rol rol, boolean voto){
        boolean estaRegistrado = false;
        if(estado == EstadoMision.SONDEO && rolesActivos.contains(rol)){
            votosSondeo.put(rol, voto);

            // Si se vota negativo, se aborta la ronda de sondeo y se vuelve a reposo
            if(!voto){
                estado = EstadoMision.ESPERA;
            }
            
            estaRegistrado = true;
        }
        return estaRegistrado;
    }

    public synchronized boolean comprobarVotosPositivos(){
        if(estado != EstadoMision.SONDEO || rolesActivos.isEmpty()){
            return false;
        }
        // Comprobamos que todos los roles activos han votado
        if(!votosSondeo.keySet().containsAll(rolesActivos)){
            return false;
        }
        // Comprobamos que todos los votos registrados son positivos
        for(Rol rol : rolesActivos){
            Boolean voto = votosSondeo.get(rol);
            if (voto == null || !voto){
                return false;
            }
        }

        // Todos votan positivo
        estado = EstadoMision.LISTO_DESPEGUE;
        return true;
    }

    public synchronized boolean iniciarCuentaAtras(){
        boolean iniciar = false;
        if(estado == EstadoMision.LISTO_DESPEGUE){
            iniciar = true;
            segundosRestantes = 10;
            estado = EstadoMision.CUENTA_ATRAS;
        }
        return iniciar;
    }

    public synchronized int cuentaAtras(){
        if(estado == EstadoMision.CUENTA_ATRAS){
            if (segundosRestantes > 0) {
            segundosRestantes--;
            } else {
                estado = EstadoMision.VUELO;
            }
        }
        return segundosRestantes;
    }

    public synchronized boolean separarEtapas(){
        boolean separacionCompletada = false;
        if(estado == EstadoMision.VUELO && etapa == 1){
            etapa = 2;
            cantidadCombustible = 100.0;  // Tanque de la 2º etapa lleno
            separacionCompletada = true;
        }
        return separacionCompletada;
    }

    public synchronized boolean abortarMision(String razon){
        boolean misionAbortada = false;
        if(estado != EstadoMision.ABORTADA && estado != EstadoMision.COMPLETADA){
            estado = EstadoMision.ABORTADA;
            misionAbortada = true;
        }
        return misionAbortada;
    }

    public synchronized void actualizarParametrosVuelo(double altitud, double velocidad, double combustible){
        if(estado == EstadoMision.VUELO){
            this.altitud += altitud;
            this.velocidad += velocidad;
            this.cantidadCombustible = Math.max(0.0, this.cantidadCombustible - combustible);
        }
    }

    public synchronized String toStringTelemetriaVuelo(){
        String telemetria = "TELEM|ALT:"+altitud+"|VEL:"+velocidad+"|COMBUSTIBLE:"+cantidadCombustible+"|ETAPA:"+etapa;
        return telemetria;
    }
}
