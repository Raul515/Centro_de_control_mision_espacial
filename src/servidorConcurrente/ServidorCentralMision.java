package servidorConcurrente;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class ServidorCentralMision {

    private static List<ManejadorCliente> clientesConectados;
    private static EstadoCohete estadoCohete = new EstadoCohete();

    public static void main(String[] args) {
        
        int puerto = 9999;

        try(ServerSocket server = new ServerSocket(puerto)){
            System.out.println("Servidor central de la mision iniciado en el puerto: " + puerto);

            while (true) {
                Socket s = server.accept();
                System.out.println("Cliente conectado desde: " + s.getInetAddress());

                //ManejadorCliente manejador = new ManejadorCliente(s, s.rol,ServidorCentralMision, estadoCohete);
                //clientesConectados.add(manejador);
                //Thread hilo = new Thread(manejador);
                //hilo.start();
            }
        } catch (IOException e) {
			e.printStackTrace();
		}
    }

}
