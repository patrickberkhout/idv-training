package local;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

public class Main {

	String username;// = "teiidUser";
	String password;// = "XXXX";
	String url;// = "jdbc:teiid:ModeShape@mm://localhost:31000";
	String query;// = "select * from sys.tables";

	public Main(String url, String username, String password, String query){
		this.url = url;
		this.username = username;
		this.password = password;
		this.query = query;
		
	}
	public static void main(String[] args)  {
		try {
			if (args.length != 4){
				System.out.println("Usage: java local.Main \"jdbc:teiid:VDB@mm://HOSTNAME:PORT\" \"USER\" \"PASSOWRD\" \"select * from sys.tables\"");
			}
			Class.forName("org.teiid.jdbc.TeiidDriver");
			new Main(args[0], args[1], args[2], args[3]).executeQuery();
		} catch (SQLException | IOException | ClassNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
	}
	public  void executeQuery() throws SQLException, IOException{
		Date start = Calendar.getInstance().getTime();
		System.out.println("Start time: " + start);
		Statement statement = getConnection().createStatement();
		statement.execute("set showplan debug");
		ResultSet mainQ = statement.executeQuery(query);
		Long rowCount = 0L;
		while(mainQ.next()){
			rowCount++;
		}
		Date end = Calendar.getInstance().getTime();

		System.out.println("Row count: " + rowCount);
		System.out.println("End time: " + end);
		Long s = end.getTime() - start.getTime();
		System.out.println("Duration: " + s + " milliseconds");

		ResultSet planQ = statement.executeQuery("show plan");
		planQ.next();
		new File("output").mkdirs();
		write("output/plan.txt", planQ.getString(1));
		write("output/plan.xml", planQ.getString(2));
		write("output/debug.txt", planQ.getString(3));

	}

		public void write(String file, String text) throws IOException{
			FileWriter fileWriter = new FileWriter(file);
			try {
				System.out.println("Output: " + file);
				fileWriter.write(text);
			}finally{
				fileWriter.close();
			}

		}
	public  Connection getConnection() throws SQLException {

		Connection conn = null;
		Properties connectionProps = new Properties();
		connectionProps.put("user", this.username);
		connectionProps.put("password", this.password);
		conn = DriverManager.getConnection(this.url,this.username, this.password);

		return conn;
	}

}
