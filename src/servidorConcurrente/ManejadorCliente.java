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
    private Rol rolAsignado;
    private final EstadoCohete estadoCohete;

    private PrintWriter salida;
    private BufferedReader entrada;
    

    public ManejadorCliente(Socket s, EstadoCohete estadoCohete) {
        this.s = s;
        this.estadoCohete = estadoCohete;
    }

    @Override
    public void run() {
        
        try{
			salida = new PrintWriter(s.getOutputStream(), true); 
            entrada = new BufferedReader(new InputStreamReader(s.getInputStream()));

			enviarMensaje("Conexion Establecida - Centro de control operativo");
            enviarMensaje("Por favor, identificate con: LOGIN <ROL> (Roles disponibles: VUELO, PROPULSION, DINAMICA)");
			

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
                if (s != null && !s.isClosed()){
                    s.close();
                } 
            } catch (IOException e) {
                System.err.println("Error al cerrar socket de cliente: " + e.getMessage());
            }
            
        }

    }

    // Getter del rol
    public Rol getRolAsignado() {
        return rolAsignado;
    }

    // Metodo auxiliar para enviar un mensaje
    // Lo hago synchronized para evitar que varios hilos puedan mezclar lineas
    public synchronized void enviarMensaje(String mensaje) {
        if (salida != null) {
            salida.println(mensaje);
        }
    }

    // Metodo auxiliar para procesar los comandos
    private boolean procesarComando(String linea){
        String[] partes = linea.split(Protocolo.separador);
        String comando = partes[0].toUpperCase();

        switch (comando) {
            case Protocolo.CMD_ABORTAR:
                if (rolAsignado != null){
                    boolean abortado = estadoCohete.abortarMision("Orden de abortar emitida por: " + rolAsignado);
                    if(abortado){
                        salida.println("Aborto Confirmado");
                    }else{
                        salida.println("La mision ya estaba abortada o completada");
                    }
                }else{
                    salida.println("No estas autenticado, no puedes abortar");
                }
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
                if(rolAsignado == Rol.PROPULSION || rolAsignado == Rol.DINAMICA){
                    boolean separado = estadoCohete.separarEtapas();
                    if(separado){
                        salida.println("Separacion de las etapas correcta");
                    }else{
                        salida.println("No se pueden separar las etapas (el cohete no esta en vuelo o ya esta en etapa 2)");
                    }
                }else{
                    salida.println("Solo los directores de PROPULSION y DINAMICA pueden ordenar la separacion de etapas.");
                }   

                break;

            case Protocolo.CMD_TELEMETRIA:
                // Cualquiera puede consultar la telemetría 
                salida.println(estadoCohete.toStringTelemetriaVuelo());
                break;

            case Protocolo.CMD_VOTAR:
                if(rolAsignado != null){
                    if(partes.length == 2){
                        String opcion = partes[1].toUpperCase();
                        boolean esGO = false;
                        if(opcion.equals("GO")){
                            esGO = true;
                        }else if (opcion.equals("NOGO")) {
                            esGO = false;
                        }else{
                            salida.println("Opcion no valida, usa GO o NOGO");
                            return false;
                        }

                        boolean registrado =  estadoCohete.registrarVoto(rolAsignado, esGO);
                        if(registrado){
                            salida.println("Voto registrado correctamente, voto: " + (esGO ? "GO" : "NO-GO"));
                        }

                    }else{
                        salida.println("Uso correcto: VOTAR GO o VOTAR NOGO");
                    }

                }else{
                    salida.print("Debes identificarte primero con LOGIN");
                }
                break;
            default:
                salida.println(" Comando no reconocido: " + comando); 
                break;
        }

        return true;
    }

}
