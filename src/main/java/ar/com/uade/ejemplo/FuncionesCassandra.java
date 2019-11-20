package ar.com.uade.ejemplo;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

/**
 * Creates a keyspace and tables, and loads some data into them.
 *
 * <p>Preconditions:
 *
 * <ul>
 *   <li>An Apache Cassandra(R) cluster is running and accessible through the contacts points
 *       identified by basic.contact-points (see application.conf).
 * </ul>
 *
 * <p>Side effects:
 *
 * <ul>
 *   <li>creates a new keyspace "simplex" in the session. If a keyspace with this name already
 *       exists, it will be reused;
 *   <li>creates two tables "simplex.songs" and "simplex.playlists". If they exist already, they
 *       will be reused;
 *   <li>inserts a row in each table.
 * </ul>
 *
 * @see <a href="https://docs.datastax.com/en/developer/java-driver/4.0">Java driver online
 *     manual</a>
 */
@SuppressWarnings("CatchAndPrintStackTrace")
public class FuncionesCassandra {

  private static CqlSession session;

  /** Initiates a connection to the session specified by the application.conf. */
  public void connect() {

    session = CqlSession.builder().build();

    System.out.printf("Connected session: %s%n", session.getName());
  }

  /** Closes the session. */
  public void close() {
    if (session != null) {
      session.close();
    }
  }
  /** Respuesta a consulta 4*/
  public void musicoConMasCancionesInterpretadas() {
	  ResultSet max =
			  session.execute(
					  "SELECT max(cant_canciones) FROM tpo_musica.consulta4;"
					  );
	  long maxcant = max.one().getLong(0); 
//	  System.out.println("$$$$$$$$$$$$$$$$$$ Maximo: " + maxcant);
	  ResultSet interpretes =
			  session.execute(
					  "SELECT * from tpo_musica.consulta4;");
	  ArrayList<Integer> ids_interpretes = new ArrayList<Integer>();
	  for(Row r : interpretes) {
		  if(r.getLong("cant_canciones") == maxcant) {
			  ids_interpretes.add(r.getInt("id_interprete"));
			  }
	  }
	  System.out.println("#### Músico(s) con más canciones interpretadas ####");
	  for(int i : ids_interpretes) {
		  ResultSet musico = 
				  session.execute("SELECT id_interprete_cancion, nombre_musico_interprete, apellido_musico_interprete "
				  		+ "from tpo_musica.consulta234 where id_interprete_cancion = "+i+" LIMIT 1 ALLOW FILTERING ;");
		  for(Row r : musico){
		  System.out.println("Id musico: "+r.getInt("id_interprete_cancion"));
		  System.out.println("Nombre músico: "+r.getString("nombre_musico_interprete"));
		  System.out.println("Apellido músico: "+r.getString("apellido_musico_interprete"));
		  System.out.println("-------------------------------------------");
		  }
	  }

  }
  /** Respuesta a consulta 1*/
  public void bandasQueEscuchaUsuario(int idusr) {
	  boolean primero= true;
	  ResultSet bandas =
			  session.execute("SELECT * from tpo_musica.consulta1 WHERE id_usuario = " + idusr);
	  for(Row r : bandas) {
		  if(primero) {
			  System.out.println("#### El usuario " + r.getInt("id_usuario")
					  +" ("+r.getString("apellido_usuario")+", "+ r.getString("nombre_usuario") + ") Escucha las bandas: "); 
			  primero = !primero;
		  }
		  System.out.println("Id musico: "+r.getInt("id_musico_interprete"));
		  System.out.println("Nombre: "+r.getString("nombre_musico"));
		  System.out.println("Apellido: "+r.getString("apellido_musico"));
		  System.out.println("-----------------------");
	  }
  }
  /**Respuesta a consulta 6*/
  public void resenasDelAlbumQueContienCancion(int idcancion) {
	  ResultSet resena =
			  session.execute("select id_resena, id_autor_resena, apellido_autor_resena,nombre_autor_resena, fecha_resena, texto_resena "
			  		+ "from tpo_musica.consulta6 where id_canciones contains " + idcancion +";");
	  for (Row r: resena) {
		  System.out.println("Id reseña: "+ r.getInt("id_resena"));
		  System.out.println("Id autor reseña: "+ r.getInt("id_autor_resena") );
		  System.out.println("Nombre y apellido: "+ r.getString("nombre_autor_resena") + " " +r.getString("apellido_autor_resena"));
		  System.out.println("Reseña: \n \t" + r.getString("texto_resena"));
		  System.out.println("--------------------------------------");
	  }
  }
  
  public int agregarResenaAlbum(int id_album, String texto_resena, int id_usr) {
	  int id_resena = (int) session.execute("select count(*) from tpo_musica.consulta6").one().getLong(0) +1; //obtiene el largo de la tabla y suma uno para creaciòn de ID
//	  System.out.println(id_resena);
	  Hashtable<String, String> info_album = getInfoAlbum(id_album);
	  ResultSet usuario = session.execute("select id_usuario, nombre_usuario, apellido_usuario from tpo_musica.consulta1 where id_usuario = " +id_usr+" limit 1;");
	  for (Row r : usuario) {
	  session.execute("INSERT INTO tpo_musica.consulta6 (id_album, nombre_album, id_autor_resena, nombre_autor_resena, apellido_autor_resena,id_resena, texto_resena, fecha_resena, id_canciones)"
	  		+ " VALUES ("
	  		+ info_album.get("id_album")+ " ,"
	  		+ "'" +info_album.get("nombre_album")+ "' ,"
	  		+ r.getInt("id_usuario")+ " ,"
	  		+ "'"+r.getString("nombre_usuario")+ "' ,"
	  		+ "'"+r.getString("apellido_usuario") +"' ,"
	  		+ id_resena +" ,"
	  		+ "'"+texto_resena+"' ,"
	  		+ System.currentTimeMillis() + " ,"
	  		+ info_album.get("id_canciones") + ");");
	  System.out.println("La reseña ha sido creada con el numero "+ id_resena);
	  }
	  return id_resena;
  }
  
  public void editarTextoResena(int id_album, int id_resena, int id_usuario, String texto_resena) {
	  Hashtable<String, String> info_album = getInfoAlbum(id_album);
	  session.execute("UPDATE tpo_musica.consulta6 set texto_resena = '"+ texto_resena+ "' where id_album = "+info_album.get("id_album")+" AND nombre_album = '"+info_album.get("nombre_album") +"' AND id_autor_resena="+id_usuario+ " AND id_resena= "+id_resena+ ";");
	  
  }
  
  public void eliminarResena(int id_resena, int id_album) {
	  Hashtable<String, String> info_album = getInfoAlbum(id_album);
	  session.execute("Delete from tpo_musica.consulta6 where id_album = "+info_album.get("id_album") +" AND nombre_album = '"+ info_album.get("nombre_album")+"' AND id_resena = "+id_resena +";");
  }
  private Hashtable<String,String> getInfoAlbum(int id_album) {
	Hashtable<String, String> res = new Hashtable<String, String>();
	String id_canciones = "[";
	boolean primero = true;
	ResultSet canciones = 
			session.execute("select id_cancion, id_album, nombre_album from tpo_musica.consulta578 where id_album = "+id_album+" ALLOW FILTERING;");
	for(Row r : canciones) {
		if(!primero) {
			id_canciones = id_canciones.concat(", ");
		}else {
			res.put("id_album", ""+r.getInt("id_album"));
			res.put("nombre_album", r.getString("nombre_album"));
			primero = !primero;
		}
		id_canciones= id_canciones.concat(""+r.getInt("id_cancion"));
	}
	id_canciones = id_canciones.concat("]");
	res.put("id_canciones", id_canciones);
//	System.out.println(res);
	return res;
}

/** Creates the schema (keyspace) and tables for this example. */
  public void createSchema() {

    session.execute(
        "CREATE KEYSPACE IF NOT EXISTS simplex WITH replication "
            + "= {'class':'SimpleStrategy', 'replication_factor':1};");

    session.execute(
        "CREATE TABLE IF NOT EXISTS simplex.songs ("
            + "id uuid PRIMARY KEY,"
            + "title text,"
            + "album text,"
            + "artist text,"
            + "tags set<text>,"
            + "data blob"
            + ");");

    session.execute(
        "CREATE TABLE IF NOT EXISTS simplex.playlists ("
            + "id uuid,"
            + "title text,"
            + "album text, "
            + "artist text,"
            + "song_id uuid,"
            + "PRIMARY KEY (id, title, album, artist)"
            + ");");
  }

  /** Inserts data into the tables. */
  public void loadData() {

    session.execute(
        "INSERT INTO simplex.songs (id, title, album, artist, tags) "
            + "VALUES ("
            + "756716f7-2e54-4715-9f00-91dcbea6cf50,"
            + "'La Petite Tonkinoise',"
            + "'Bye Bye Blackbird',"
            + "'Joséphine Baker',"
            + "{'jazz', '2013'})"
            + ";");

    session.execute(
        "INSERT INTO simplex.playlists (id, song_id, title, album, artist) "
            + "VALUES ("
            + "2cc9ccb7-6221-4ccb-8387-f22b6a1b354d,"
            + "756716f7-2e54-4715-9f00-91dcbea6cf50,"
            + "'La Petite Tonkinoise',"
            + "'Bye Bye Blackbird',"
            + "'Joséphine Baker'"
            + ");");
  }

  /** Queries and displays data. */
  public void querySchema() {

    ResultSet results =
        session.execute(
            "SELECT * FROM simplex.playlists "
                + "WHERE id = 2cc9ccb7-6221-4ccb-8387-f22b6a1b354d;");

    System.out.printf("%-30s\t%-20s\t%-20s%n", "title", "album", "artist");
    System.out.println(
        "-------------------------------+-----------------------+--------------------");

    for (Row row : results) {

      System.out.printf(
          "%-30s\t%-20s\t%-20s%n",
          row.getString("title"), row.getString("album"), row.getString("artist"));
    }
  }


}