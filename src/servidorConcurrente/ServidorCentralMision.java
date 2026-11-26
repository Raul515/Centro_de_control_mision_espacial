package servidorConcurrente;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ServidorCentralMision {

    public static final int PUERTO = 9999;

    public static List<ManejadorCliente> clientesConectados = Collections.synchronizedList(new ArrayList<>());
    public static EstadoCohete estadoCohete = new EstadoCohete();



    public static void main(String[] args) {

        ExecutorService poolClientes = Executors.newCachedThreadPool();
        ScheduledExecutorService schedulerVuelo = Executors.newSingleThreadScheduledExecutor();
        
        System.out.println("=================================================");
        System.out.println(" CENTRO DE CONTROL ESPACIAL - SERVIDOR CENTRAL ");
        System.out.println("=================================================");        

        try(ServerSocket server = new ServerSocket(PUERTO)){
            System.out.println("Servidor central de la mision iniciado en el puerto: " + PUERTO);
            System.out.println("Esperando operadores de vuelo...\\n");

            //SimulacionVuelo simulador = new SimulacionVuelo(estadoCohete);
            //schedulerVuelo.scheduleAtFixedRate(simulador, 0, 1, TimeUnit.SECONDS)

            while (true) {
                try {
                    Socket s = server.accept();
                    System.out.println("Nuevo operador conectado desde: " + s.getInetAddress());

                    ManejadorCliente manejador = new ManejadorCliente(s, estadoCohete);
                    clientesConectados.add(manejador);

                    poolClientes.execute(manejador);

                    /* Podria hacerlo con hilos individuales, pero lo voy a hacer mejor con una pool de hilos como en la practica 8
                    Thread hilo = new Thread(manejador);
                    hilo.start();
                    */
                } catch (IOException ex) {
                    System.err.println("Error al aceptar la conexion del cliente: " + ex.getMessage());
                }
                
            }
        } catch (IOException e) {
            System.err.println("Error al iniciar el servidor en el puerto " + PUERTO + ": " + e.getMessage());
			e.printStackTrace();
		}finally {
            schedulerVuelo.shutdown();
            poolClientes.shutdown();
            System.out.println("Servidor central detenido.");
        }
    }

    // Envía un mensaje a todos los operadores conectados de forma segura
    public static void difundir(String mensaje){
        synchronized(clientesConectados){
            for(ManejadorCliente c : clientesConectados){
                c.enviarMensaje(mensaje);
            }
        }
    }

    // Elimina al operador desconectado de la lista activa
    public static void eliminarCliente(ManejadorCliente c){
        clientesConectados.remove(c);
        String rol = (c.getRolAsignado() != null) ? c.getRolAsignado().name() : "SIN_ROL";
        System.out.println("Operador [" + rol + "] desconectado. Quedan activos: " + clientesConectados.size());
    }

}
