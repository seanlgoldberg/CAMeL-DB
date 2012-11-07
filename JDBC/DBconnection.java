import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.mysql.jdbc.Statement;


public class DBconnection {
	void insert(int key) throws ClassNotFoundException{

		//Will be changed if the database changes
		Class.forName("com.mysql.jdbc.Driver");
		
		Connection con=null;
		Statement st=null;
		try {
			//Format is as follows(StringToGetConnection, UserName, Password)
			//Stringtogetconnection is given by "jdbc:sub protocol://hostIp:port/schema name"
			con=DriverManager.getConnection("jdbc:mysql://localhost:3306/myschema","root","root123root");
			st=(Statement) con.createStatement();
			st.execute("insert into test values("+key+")");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void retrieve() throws ClassNotFoundException{

		//Will be changed if the database  changes
		Class.forName("com.mysql.jdbc.Driver");
		
		Connection con=null;
		Statement st=null;
		try {
			//Format is as follows(StringToGetConnection, UserName, Password)
			con=DriverManager.getConnection("jdbc:mysql://localhost:3306/myschema","root","root123root");
			st=(Statement) con.createStatement();
			ResultSet rs=st.executeQuery("select * from test");
			
			while (rs.next()){
				//rs.getString(columnNo) fetches the  columnNo in string format, similarly we even have getInt, getBolean
				//rs.next goes to the next record, we even have rs.first and rs.last for going  the first and last method
				System.out.println(rs.getString(1));
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws ClassNotFoundException {
		DBconnection dbc=new DBconnection();
		
		dbc.insert(1);
		dbc.retrieve();
	}
}
