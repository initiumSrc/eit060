package utilities;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Database {

	private static Connection conn;
	private ArrayList<User> users;
	private ArrayList<Record> records;
	private static String db_url;
	private static String db_class;
	private static String db_name;
	private static String db_password;

	private Database() {
		db_class = "com.mysql.jdbc.Driver";
		db_url = "jdbc:mysql://puccini.cs.lth.se/";
		db_name = "db142";
		db_password = "classic";

		/* Creation of an instance of the connection statement */

		conn = setConnection();
	}

	/* Private method charge to set the connection statement */

	private static Connection setConnection() {
		try {
			try {
				Class.forName(db_class);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			java.sql.Connection conn = DriverManager.getConnection(db_url + db_name, db_name, db_password);
			return conn;
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	/*
	 * Private inner class responsible for instantiating the single instance of
	 * the singleton
	 */

	private static class DatabaseHolder {
		private final static Database instance = new Database();
	}

	/**
	 * Public method, which is the only method allowed to return an instance of
	 * the singleton
	 */

	public static Database getInstance() {
		try {
			return DatabaseHolder.instance;
		} catch (ExceptionInInitializerError ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public void closeConnection() {
		try {
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
		}
		conn = null;
	}

	public boolean isConnected() {
		return conn != null;
	}
	
	/***
	 * Retrieves all records 
	 * @return array of all records.
	 */

	public ArrayList<Record> getRecords() {

		PreparedStatement statement = null;
		try {
			String sql = "SELECT doctor, nurse, patient, division, medicalData, id FROM records";
			statement = conn.prepareStatement(sql);
			ResultSet result = statement.executeQuery();

			records = new ArrayList<Record>();

			while (result.next()) {

				String doctorCertNbr = result.getString("doctor");
				String nurseCertNbr = result.getString("nurse");
				String patientCertNbr = result.getString("patient");
				Division division = new Division(result.getString("division"));
				String medicalData = result.getString("medicalData");
				long id = result.getLong("id");

				Doctor doctor = null;
				Nurse nurse = null;
				Patient patient = null;

				for (User u : users) {
					if (u.getCertNbr().equals(doctorCertNbr)) {
						doctor = (Doctor) u;
					} else if (u.getCertNbr().equals(nurseCertNbr) 
							|| (u.getPermissions().equals(PermissionLevel.Nurse) && u.getDivision().equals(division))) {
						nurse = (Nurse) u;
					} else if (u.getCertNbr().equals(patientCertNbr)) {
						patient = (Patient) u;
					}
				}

				Record r = null;

				if (doctor != null && nurse != null && patient != null) {
					r = new Record(doctor, nurse, patient, division, medicalData);
					r.setRecordId(id);
				} else {
					System.out.println("NULL FAILURE!");
				}

				records.add(r);
			}

			return records;

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;

	}
	
	/***
	 * Retrieves all users 
	 * @return array of all users.
	 */

	public ArrayList<User> getUsers() {

		PreparedStatement statement = null;
		try {
			String sql = "SELECT username, division, certNbr, permissionLevel, certNbr FROM users2";
			statement = conn.prepareStatement(sql);
			ResultSet result = statement.executeQuery();

			users = new ArrayList<User>();

			while (result.next()) {

				String permLevel = result.getString("permissionLevel");
				String username = result.getString("username");
				Division division = new Division(result.getString("division"));
				String certNbr = result.getString("certNbr");
				
				User u = null;
				

				if (permLevel.equalsIgnoreCase("agency")) {
					u = new Agency(username, division, certNbr);
					System.out.println("CREATED AGENCY: " + u.toString());
				}

				if (permLevel.equalsIgnoreCase("doctor")) {
					u = new Doctor(username, division, certNbr);
					System.out.println("CREATED DOCTOR: " + u.toString());
				}

				if (permLevel.equalsIgnoreCase("nurse")) {
					u = new Nurse(username, division, certNbr);
					System.out.println("CREATED NURSE: " + u.toString());
				}

				if (permLevel.equalsIgnoreCase("patient")) {
					u = new Patient(username, division, certNbr);
					System.out.println("CREATED PATIENT: " + u.toString());
				}

				if (u != null) {
					users.add(u);
				}
			}

			return users;

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/***
	 * Inserts a user 
	 * @param user
	 */

	public void insertUser(User u) {

		PreparedStatement statement = null;
		try {
			String sql = "INSERT INTO users2(username, division, permissionLevel, certNbr) VALUES(?,?,?,?)";
			statement = conn.prepareStatement(sql);
			statement.setString(1, u.getUsername());
			statement.setString(2, u.getDivision().getName());
			statement.setString(3, u.getPermissions().toString());
			statement.setString(4, u.getCertNbr());
			statement.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	/***
	 * Inserts a record
	 * @param record
	 */

	public void insertRecord(Record r) {

		PreparedStatement statement = null;
		try {
			String sql = "INSERT INTO records(doctor, nurse, patient, division, medicalData) VALUES(?,?,?,?,?)";
			statement = conn.prepareStatement(sql);
			statement.setString(1, r.getDoctorCertNbr());
			statement.setString(2, r.getNurseCertNbr());
			statement.setString(3, r.getPatientCertNbr());
			statement.setString(4, r.getDivision().getName());
			statement.setString(5, r.getMedicalData());
			statement.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	/***
	 * Writes to a record with new medical data
	 * @param medicalData, id
	 */

	public void writeRecord(String medicalData, long id) {
		PreparedStatement statement = null;
		try {
			String sql = "UPDATE records SET medicalData = ? WHERE id = ?";
			statement = conn.prepareStatement(sql);
			statement.setString(1, medicalData);
			statement.setLong(2, id);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	/***
	 * Deletes a specific record with id
	 * @param id 
	 */

	public void deleteRecord(long id) {
		PreparedStatement statement = null;
		try {
			String sql = "DELETE FROM records WHERE id = ?";
			statement = conn.prepareStatement(sql);
			statement.setLong(1, id);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public User getUserFromName(String username) {
		for(User u : users) {
			if(u.getUsername().equals(username)) {
				return u;
			}
		}
		return null;
	}
	
	public Division getDivision(String name) {
		for(Record r : records) {
			if(r.getDivision().equals(name)) {
				return r.getDivision();
			}
		}
		return null;
	}
	
	/***
	 * Updates the records in the database
	 */

	public ArrayList<Record> updateRecords() {
		return getRecords();
	}
	
	/***
	 * Updates the users in the database
	 */

	public ArrayList<User> updateUsers() {
		return getUsers();
	}

	/** public void loadTestData() {
		Doctor doc_1 = new Doctor("doctor", User.DIV_EMERGENCY, "13334610649522941717");
		Nurse nurse_1 = new Nurse("nurse", User.DIV_REHAB, "2");
		Nurse nurse_2 = new Nurse("nurse2", User.DIV_EMERGENCY, "3");
		Patient patient_1 = new Patient("patient", User.DIV_REHAB, "4");
		Patient patient_2 = new Patient("patient2", User.DIV_REHAB, "5");
		Agency agency_1 = new Agency("agency", User.DIV_REHAB, "6");

		Record r = new Record(doc_1, nurse_1, patient_1, User.DIV_REHAB, "Ont i benet");
		Record r2 = new Record(doc_1, nurse_1, patient_1, User.DIV_REHAB, "Ont i armen");
		Record r3 = new Record(doc_1, nurse_2, patient_2, User.DIV_EMERGENCY, "Ont i huvudet");
		Record r4 = new Record(doc_1, nurse_2, patient_2, User.DIV_EMERGENCY, "Ont i örat");

		this.insertUser(doc_1);
		this.insertUser(nurse_1);
		this.insertUser(nurse_2);
		this.insertUser(patient_1);
		this.insertUser(patient_2);
		this.insertUser(agency_1);

		this.insertRecord(r);
		this.insertRecord(r2);
		this.insertRecord(r3);
		this.insertRecord(r4);
	} **/

}
