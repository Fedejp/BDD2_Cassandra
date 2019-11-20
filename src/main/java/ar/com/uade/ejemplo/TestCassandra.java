package ar.com.uade.ejemplo;

import java.util.Scanner;

public class TestCassandra {
	  public static void main(String[] args) {

		    FuncionesCassandra client = new FuncionesCassandra();
		    
		    try {
		    	client.connect();
		    	System.out.println("######################### ---------   Ejemplos de lectura de la base de datos");
		    	System.out.println("···························Consulta 1");
		    	client.bandasQueEscuchaUsuario(1);
		    	System.out.println("··························Consulta 6");
		    	client.resenasDelAlbumQueContienCancion(12);
		    	System.out.println("····························Consulta 4");
		    	client.musicoConMasCancionesInterpretadas();
		    	System.out.println("######################### ---------  CRUDL De reseñas");
		    	int ultima_resena = client.agregarResenaAlbum(1,"Muy Bueno!", 1);
		    	System.out.println("########################Comprobación de que se agregó: ");
		    	client.resenasDelAlbumQueContienCancion(2);
		    	client.editarTextoResena(1, ultima_resena, 1, "Cambié de opinion, malísimo");
		    	System.out.println("########################Comprobación de que se cambió: ");
		    	client.resenasDelAlbumQueContienCancion(2);
		    	client.eliminarResena(ultima_resena, 1);
		    	System.out.println("########################Comprobación de que se eliminó: ");
		    	client.resenasDelAlbumQueContienCancion(2);
		    	client.close();
		    } catch (Exception ex) {
		      ex.printStackTrace();
		    } finally {
		      client.close();
		    }
		  }

}
