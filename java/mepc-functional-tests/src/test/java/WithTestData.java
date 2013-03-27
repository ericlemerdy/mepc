import static com.google.common.collect.Lists.newArrayList;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;

public class WithTestData {

	@SuppressWarnings("unchecked")
	private static final List<ArrayList<String>> soldiers = newArrayList(
			newArrayList("stallone", "Sylvester Stallone", "This ex-boxer is a vietnâm veteran that really had a rough."), //
			newArrayList("statham", "Jason Statham", "Kickboxing expert, body-to-body, it leaves no chance to your enemies."), //
			newArrayList("li", "Jet Li", "Do not be fooled by its size, this man can send you to the mat quickly thanks to its speed."), //
			newArrayList("lundgren", "Dolph Lundgren", "The Russian soldier has hardened his body to the point it looks like a robot."), //
			newArrayList("norris", "Chuck Norris", "You are not hiring Chuck Norris, this is Chuck Norris that hires you."), //
			newArrayList("van-damme", "Jean-Claude Van Damme", "This warrior philosopher is aware."), //
			newArrayList("willis", "Bruce Willis", "This formidable killer is unstoppable, he learned that the police in New York."), //
			newArrayList(
					"schwarzenegger",
					"Arnold Schwarzenegger",
					"When you're born in the Austrian mountains and you carries the milk down to the valley, you are getting stronger... If you hire him, he will be back."));

	@BeforeClass
	public static void initDb() throws SQLException {
		String url = "jdbc:mysql://localhost:3306/mepcdata";
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Cannot find the driver in the classpath!", e);
		}
		Connection conn = DriverManager.getConnection(url, "root", "root");
		try {
			PreparedStatement insert = conn.prepareStatement("INSERT INTO SOLDIER (id, name, description, hired) VALUES (?, ?, ?, false)");

			for (ArrayList<String> soldier : soldiers) {
				insert.setString(1, soldier.get(0));
				insert.setString(2, soldier.get(1));
				insert.setString(3, soldier.get(2));
				insert.executeUpdate();
			}
		} finally {
			conn.close();
		}
	}

	@AfterClass
	public static void emptyDb() throws SQLException {
		String url = "jdbc:mysql://localhost:3306/mepcdata";
		Connection conn = DriverManager.getConnection(url, "root", "root");
		try {
			conn.createStatement().execute("DELETE FROM SOLDIER");
		} finally {
			conn.close();
		}
	}

}
