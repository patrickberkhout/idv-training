package local;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import org.teiid.client.plan.PlanNode;
import org.teiid.jdbc.TeiidStatement;

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
				System.out.println("Usage: java local.Main \"jdbc:teiid:VDB@mm://HOSTNAME:PORT\" \"USER\" \"PASSOWRD\" input/in.sql");
			}
			Class.forName("org.teiid.jdbc.TeiidDriver");
			new Main(args[0], args[1], args[2], args[3]).executeQuery();
		} catch (SQLException | IOException | ClassNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
	}
	public  void executeQuery() throws SQLException, IOException{
		
		String queryString = new String(Files.readAllBytes(Paths.get(query)));
		Date start = Calendar.getInstance().getTime();
		Statement statement = getConnection().createStatement();
	
		statement.execute("set showplan debug");
		ResultSet mainQ = statement.executeQuery(queryString);
		
		TeiidStatement tstatement = statement.unwrap(TeiidStatement.class);
		PlanNode queryPlan = tstatement.getPlanDescription();
		new File("output").mkdirs();
		
		
		write("output/plan.txt", queryPlan.toString());
		write("output/plan.xml", queryPlan.toXml());
		Files.deleteIfExists(Paths.get("output/debug.txt"));

		
		System.out.println("Plan is written to output folder.");
		System.out.println("Now we are executing the query to produce debug output.");
		System.out.println("CTRL-C to stop.");
		System.out.println("Start time: " + start);

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
