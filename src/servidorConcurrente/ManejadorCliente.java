package servidorConcurrente;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import herramientas.Protocolo;
import herramientas.Rol;

public class ManejadorCliente implements Runnable{

    private Socket s;
    private Rol rol;
    private ServidorCentralMision servidorCentral;
    private EstadoCohete estadoCohete;

    private PrintWriter salida;
    private BufferedReader entrada;
    

    public ManejadorCliente(Socket s, Rol rol, ServidorCentralMision servidorCentral, EstadoCohete estadoCohete) {
        this.s = s;
        this.rol = rol;
        this.servidorCentral = servidorCentral;
        this.estadoCohete = estadoCohete;
    }

    @Override
    public void run() {
        
        try{
			salida = new PrintWriter(s.getOutputStream(), true); 
            entrada = new BufferedReader(new InputStreamReader(s.getInputStream()));

			salida.println("Conexion Establecida - Centro de control operativo");
            salida.println("Por favor, identificate con: LOGIN <ROL> (Roles disponibles: VUELO, PROPULSION, DINAMICA)");
			

            String linea;
			boolean continuar = true;
				
			while(continuar && (linea = entrada.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()){
                    continue;
                }
                
                continuar = procesarComando(linea); // Devuelve false cuando es "SALIR" 
				
			}	
		} catch (IOException e) {
			System.err.println("Error comunicación cliente: " + e.getMessage());
        } finally {
            try {
                if (entrada != null) entrada.close();
                if (salida != null) salida.close();
                if (s != null && !s.isClosed()) s.close();
            } catch (IOException e) { /* Ignoramos */ }
            System.out.println("Cliente finalizado.");
        }

    }

    // Metodo auxiliar para procesar el comando

    private boolean procesarComando(String linea){
        String[] partes = linea.split(" ");
        String comando = partes[0].toUpperCase();

        switch (comando) {
            case Protocolo.CMD_ABORTAR:
                // Hacer logica aborto
                break;

            case Protocolo.CMD_CUENTA_ATRAS:
                // Hacer logica cuenta atras
                break;

            case Protocolo.CMD_INICIAR_SONDEO:
                // Hacer logica inicio sondeo
                break;

            case Protocolo.CMD_LOGIN:
                // Hacer logica login
                break;
            
            case Protocolo.CMD_SALIR:
                salida.println("Sesion finalizada.");
                return false;

            case Protocolo.CMD_SEPARAR_ETAPAS:
                // Hacer logica separacion
                break;

            case Protocolo.CMD_TELEMETRIA:
                // Hacer logica telemetria
                break;

            case Protocolo.CMD_VOTAR:
                // Hacer logica voto
                break;
            default:
                salida.println(" Comando no reconocido: " + comando); 
                break;
        }

        return true;
    }

}
