package config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import com.mysql.jdbc.DatabaseMetaData;
import com.mysql.jdbc.Statement;

import jdk.nashorn.api.tree.TryTree;


public class ConexionBD {

	private static final String CONTROLADOR = "com.mysql.jdbc.Driver";
	private static final String URL = "jdbc:mysql://localhost/adat_vuelos";
	private static final String USUARIO = "root";
	private static final String PASS = "";

	static {
		try {
			Class.forName(CONTROLADOR);
		} catch (ClassNotFoundException e) {
			System.out.println("Error al cargar el controlador");
			e.printStackTrace();
		}
	}

	public Connection get_conexion() {

		Connection conexion = null;

		try {

			conexion = (Connection) DriverManager.getConnection(URL, USUARIO, PASS);
			System.out.println("Conexión OK");

		} catch (SQLException e) {
			// TODO: handle exception
			System.out.println("Error en la conexión");
			e.printStackTrace();
		}

		return conexion;

	}

	public static void main(String[] args) throws SQLException, IOException {
		
		ConexionBD conexion = new ConexionBD();
		Connection conn = conexion.get_conexion();
		ArrayList<String> lista_tablas = getTablesList(conn);
		PreparedStatement stm = null;
		PreparedStatement stm1 = null;
		ResultSet rs = null;
		ResultSet rs1 = null;
		
		for (int i = 0; i < lista_tablas.size(); i++) {
			
			String clase = lista_tablas.get(i).substring(0, 1).toUpperCase() + lista_tablas.get(i).substring(1).toLowerCase();
						
						
			String ruta = "./src/models/"+clase+".java";
			File archivo = new File(ruta);
			BufferedWriter bw;

			FileWriter fichero = null;
			PrintWriter pw = null;
			
			Map<String, String> arreglo_asociativo = new HashMap<String, String>();
			
			if(archivo.exists()) {
				  bw = new BufferedWriter(new FileWriter(archivo));		      
			      			      
			} else {
				 bw = new BufferedWriter(new FileWriter(archivo));
				 
				 try {
						fichero = new FileWriter(ruta, true);
						pw = new PrintWriter(fichero);
						String[] parts = ruta.split("/");
						
						pw.println("package " + parts[2] +";");
						pw.println("public class " + clase +"{");
						
						String query = "SHOW COLUMNS FROM "+lista_tablas.get(i);
						String query1 = "SHOW COLUMNS FROM "+lista_tablas.get(i);
						
						stm = conn.prepareStatement(query);
						stm1 = conn.prepareStatement(query1);
						
						rs = stm.executeQuery();						
						rs1 = stm1.executeQuery();
						
						while (rs.next()) {
							String tipo = rs.getString("Type");							
							
							if(tipo.contains("int")) {
								tipo = "int";
							}else if(tipo.contains("varchar")) {
								tipo = "String";
							}else if(tipo.contains("datetime")) {
								tipo = "Date";
							}
							
							pw.println("private "+tipo+ " "+rs.getString("Field")+ ";");
							arreglo_asociativo.put(tipo, rs.getString("Field"));
						}
						
						pw.print("public "+ clase+"(");
						
						
						while (rs1.next()) {
							
							String tipo = rs1.getString("Type");							
							
							if(tipo.contains("int")) {
								tipo = "int";
							}else if(tipo.contains("varchar")) {
								tipo = "String";
							}else if(tipo.contains("datetime")) {
								tipo = "Date";
							}
							if(rs1.isLast()){
								pw.print(tipo+ " "+rs1.getString("Field"));
							}else {
								pw.print(tipo+ " "+rs1.getString("Field")+ ",");								
							}
						}
						
						pw.print("){");
						pw.println("");
						pw.println("super();");
						pw.println("}");
						pw.println("}");

					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						try {
							if (null != fichero)
								fichero.close();
						} catch (Exception e2) {
							e2.printStackTrace();
						}
					}
			}
		}
				
	}
	
	public static ArrayList<String> getTablesList(Connection conn)
            throws SQLException {

        ArrayList<String> listofTable = new ArrayList<String>();

        java.sql.DatabaseMetaData md = conn.getMetaData();

        ResultSet rs = md.getTables(null, null, "%", null);

        while (rs.next()) {
            if (rs.getString(4).equalsIgnoreCase("TABLE")) {
                listofTable.add(rs.getString(3));
            }
        }
        return listofTable;
    }

}
