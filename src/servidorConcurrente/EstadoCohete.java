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
        this.cantidadCombustible = 0.0;
        this.etapa = 1;
        this.segundosRestantes = 10;

        this.estado = EstadoMision.ESPERA;
        this.votosSondeo = new HashMap<>();
        this.rolesActivos = new HashSet<>();
    }


    public synchronized boolean asignarRol(Rol rol){
        if (rol == null){
            return false;
        }
        if(rolesActivos.contains(rol)){
            return false;
        }else{
            rolesActivos.add(rol);
            return true;
        }
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
        if(estado != EstadoMision.ESPERA){
            return false;
        }
        votosSondeo.clear();
        estado = EstadoMision.SONDEO;
        return true;
    }

    public synchronized boolean registrarVoto(Rol rol, boolean voto){
        if(estado != EstadoMision.SONDEO || !rolesActivos.contains(rol)){
            return false;
        }
        votosSondeo.put(rol, voto);

        // Si se vota negativo, se aborta la ronda de sondeo y se vuelve a reposo
        if(!voto){
            estado = EstadoMision.ESPERA;
        }

        return true;
    }

    public synchronized boolean comprobarVotosPositivos(){
        if(estado != EstadoMision.SONDEO || !rolesActivos.isEmpty()){
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
}
