package gsi.investalia.server.db;

import es.upm.multidimensional.RecommendationGenerator;
import gsi.investalia.domain.Message;
import gsi.investalia.domain.Tag;
import gsi.investalia.domain.User;
import gsi.investalia.server.conf.ConfigManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Saves and gets all the needed information of the database. It works with the
 * domain classes.
 * 
 * @author luis
 */
public class MysqlInterface {
	private static Connection con;
	private static Statement stmt;

	public static final int LIMIT_INFINITY = -1;
	public static final int LIMIT_ALL = 20;
	public static final int LIMIT_SUBSCRIBED = 20;
	public static final int LIMIT_RECOMMENDATIONS = 20;
	
	public static final String CONNECTION_URL = ConfigManager.getDatabaseUrl();
	public static final String CONNECTION_DBNAME = ConfigManager.getDatabaseName();
	public static final String CONNECTION_USER = ConfigManager.getDatabaseUser();
	public static final String CONNECTION_PASS = ConfigManager.getDatabasePass();

	/**
	 * Connects to the database
	 */
	public static void connectToDatabase() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection(CONNECTION_URL + CONNECTION_DBNAME, 
					CONNECTION_USER, CONNECTION_PASS);
			stmt = con.createStatement();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Saves a new message
	 */
	public static void saveMessage(Message message) {

		// If the author is not registered, save on the database
		String authorName = message.getUserName();
		User author = getUser(authorName);
		if (author == null) {
			author = new User(authorName, authorName);
			saveNewUser(author);
			author.setId(getUser(authorName).getId());
		}

		int maxLengthTitle = 30;
		if (message.getTitle().length() > maxLengthTitle)
			message.setTitle(message.getTitle()
					.substring(0, maxLengthTitle - 1));

		int maxLengthText = 440;
		if (message.getText().length() > maxLengthText)
			message.setText(message.getText().substring(0, maxLengthText - 1));

		connectToDatabase();
		// Save the message itself
		String query = "INSERT INTO messages VALUES (Null, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement prep;
		try {
			prep = con.prepareStatement(query);
			prep.setInt(1, author.getId());
			prep.setString(2, message.getTitle());
			prep.setString(3, message.getText());
			prep.setTimestamp(4, new Timestamp(message.getDate().getTime()));
			prep.setInt(5, 0); // Rating = 0 at start
			prep.setInt(6, 0); // Not still read
			prep.setLong(7, message.getIdMessageAPI());
			prep.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// Set the id generated by the db
		setMessageId(message, author.getId());

		// Save the tags
		query = "INSERT INTO messages_tags VALUES (Null, ?, ?)";
		try {
			prep = con.prepareStatement(query);
			for (Tag tag : message.getTags()) {
				prep.setInt(1, message.getId());
				prep.setInt(2, tag.getId());
				prep.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		closeConnectionDatabase();
	}
	

	/**
	 * Updates the read and liked properties for a message and user
	 */
	public static boolean updateRead(Message m, int idUser) {

		try {
			// Increase timesRead
			connectToDatabase();
			int rs = stmt.executeUpdate("UPDATE messages"
					+ " SET times_read = (times_read + 1)"
					+ " WHERE idMessage = " + m.getId());
			// Add row in read table
			stmt.executeUpdate("INSERT INTO users_messages VALUES (Null, "
					+ m.getId() + ", " + idUser + ", "
					+ (m.isLiked() ? 1 : 0) + ", Null)");
			closeConnectionDatabase();

			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean updateDate(Message m) {

		try {
			// Increase timesRead
			connectToDatabase();
			stmt.executeUpdate("UPDATE messages"
					+ " SET date = '" + getDateTimestamp(m) + "'"
					+ " WHERE idMessage = " + m.getId());
			
			closeConnectionDatabase();

			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}


	/**
	 * Updates the read and liked properties for a message and user
	 */
	public static boolean updateReadAndLiked(Message m, int idUser) {

		// If the "new message" is not read, the method does nothing
		if (!m.isRead()) {
			return true;
		}
		
		// Previous state
		boolean isRead = false;
		boolean liked = false;
		
		connectToDatabase();
		try {
			ResultSet rs = stmt
					.executeQuery("SELECT liked FROM users_messages" +
							" WHERE idMessage = " + m.getId() + " AND idUser = " + idUser);
			if (rs.next()) {
				isRead = true;
				if (rs.getInt("liked") == 1)
					liked = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		closeConnectionDatabase();
		
		if (isRead) {
			try {
				// If liked does not change, the method does nothing
				if (m.isLiked() == liked) {
					return true;
				}
				// Liked has changed, update the db
				connectToDatabase();
				stmt.executeUpdate("UPDATE users_messages" + " SET liked = "
						+ (m.isLiked() ? 1 : 0) + " WHERE idmessage = "
						+ m.getId() + " AND idUser = " + idUser);
				closeConnectionDatabase();
				/*
				 * // Old disliked and new likes: rating++ if (m.isLiked()) {
				 * stmt.executeUpdate("UPDATE messages " +
				 * "SET rating = (rating + 1) WHERE idMessage = " + m.getId());
				 * } // Old liked and new dislikes: rating-- else {
				 * stmt.executeUpdate("UPDATE messages " +
				 * "SET rating = (rating - 1) WHERE idMessage = " + m.getId());
				 * }
				 */
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		} else {
			try {
				// Increase timesRead
				connectToDatabase();
				stmt.executeUpdate("UPDATE messages"
						+ " SET times_read = (times_read + 1)"
						+ " WHERE idMessage = " + m.getId());
				// Add row in read table
				stmt.executeUpdate("INSERT INTO users_messages VALUES (Null, "
						+ m.getId() + ", " + idUser + ", "
						+ (m.isLiked() ? 1 : 0) + ", Null)");
				closeConnectionDatabase();
				/*
				 * // If liked, rating++ if (m.isLiked()) {
				 * stmt.executeUpdate("UPDATE messages " +
				 * "SET rating = (rating + 1) WHERE idMessage = " + m.getId());
				 * }
				 */
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		}
	}

	/**
	 * Gets a user by its userName
	 */
	public static User getUser(String userName) {
		return getUserFromQuery("SELECT * FROM users WHERE userName = '"
				+ userName + "'");
	}

	/**
	 * Gets a user by its id
	 */
	public static User getUser(int idUser) {
		return getUserFromQuery("SELECT * FROM users WHERE idUser = " + idUser);
	}

	/**
	 * Gets a user by its userName and password
	 */
	public static User getUser(String userName, String password) {
		return getUserFromQuery("SELECT * FROM users WHERE userName = '"
				+ userName + "' AND password = '" + password + "'");
	}

	public static boolean saveNewUser(User user) {

		// Check the username is not already used
		User alreadyRegisteredUser = getUser(user.getUserName());
		if (alreadyRegisteredUser != null) {
			System.out.println("Already registered user");
			if (alreadyRegisteredUser.getPassword().equalsIgnoreCase(
					alreadyRegisteredUser.getUserName())) {
				user.setId(alreadyRegisteredUser.getId());
				updateUser(user);
				return false;
			}
		}

		connectToDatabase();
		boolean saved = false;
		// Save the user
		String query = "INSERT INTO users (iduser, username, password, name, location, email) values (Null, ?, ?, ?, ?, ?)";
		PreparedStatement prep;
		try {
			prep = con.prepareStatement(query);
			prep.setString(1, user.getUserName());
			prep.setString(2, user.getPassword());
			prep.setString(3, user.getName());
			prep.setString(4, user.getLocation());
			prep.setString(5, user.getEmail());
			prep.executeUpdate();
			saved = true;
		} catch (SQLException e) {
			System.out.println("SQL exception inserting new user" + e);
		}

		// Save the user tags
		query = "INSERT INTO users_tags values (Null, ?, ?)";
		try {
			prep = con.prepareStatement(query);
			for (Tag tag : user.getTagsFollowing()) {
				prep.setInt(1, user.getId());
				prep.setInt(2, tag.getId());
				prep.executeUpdate();
				saved = true;
			}
		} catch (SQLException e) {
			System.out.println("SQL exception inserting new user" + e);
		}

		closeConnectionDatabase();
		return saved;
	}

	public static boolean updateUser(User user) {

		// User with the new username from database
		// User newUsernameUser = getUser(user.getUserName());
		/*
		 * // Check if the new username (if changed) is not used if
		 * (newUsernameUser != null && newUsernameUser.getId() != user.getId())
		 * { System.out.println("Updating a wrong user"); return false; }
		 */
		System.out.println("Updating user " + user.getId());

		connectToDatabase();

		// Update the user
		String query = "UPDATE users SET username=?, password=?, name=?, location=?, email=? WHERE iduser=?";
		PreparedStatement prep;
		try {
			prep = con.prepareStatement(query);
			prep.setString(1, user.getUserName());
			prep.setString(2, user.getPassword());
			prep.setString(3, user.getName());
			prep.setString(4, user.getLocation());
			prep.setString(5, user.getEmail());
			prep.setInt(6, user.getId());
			prep.executeUpdate();
		} catch (SQLException e) {
			System.out.println("SQL exception updating user details");
			return false;
		}

		// Delete old tags
		query = "DELETE FROM users_tags WHERE iduser = ?";
		try {
			prep = con.prepareStatement(query);
			prep.setInt(1, user.getId());
			prep.executeUpdate();
		} catch (SQLException e) {
			System.out.println("SQL exception deleting the old tags");
			return false;
		}

		// Save the new tags
		query = "INSERT INTO users_tags VALUES (Null, ?, ?)";
		try {
			prep = con.prepareStatement(query);
			for (Tag tag : user.getTagsFollowing()) {
				prep.setInt(1, user.getId());
				prep.setInt(2, tag.getId());
				prep.executeUpdate();
			}
		} catch (SQLException e) {
			System.out.println("SQL exception inserting the new tags");
			return false;
		}

		closeConnectionDatabase();
		return true;
	}

	/**
	 * Gets the list of all the messages that a user is following
	 */
	public static List<Message> getUserMessages(String userName, int limit) {
		int idUser = getUser(userName).getId();
		String limitStr = "";
		if (limit > 0) {
			limitStr = "LIMIT = " + limit;
		}
		return getMessagesFromQuery(
				"SELECT DISTINCT m.* FROM messages AS m, messages_tags AS mt WHERE m.idmessage = mt.idmessage AND idtag IN (SELECT idtag FROM users_tags WHERE iduser = "
						+ idUser + ") " + limitStr, idUser);
	}

	/**
	 * Gets the list of the messages that a user is following after one given
	 */
	public static List<Message> getUserMessagesSinceLast(String userName,
			int idMessageLast) {
		int idUser = getUser(userName).getId();
		String query = "SELECT DISTINCT m.* FROM messages AS m, messages_tags AS mt WHERE m.idmessage = mt.idmessage AND idtag IN (SELECT idtag FROM users_tags WHERE iduser = "
				+ idUser + ") AND m.idmessage > " + idMessageLast
				+ " ORDER BY m.idmessage DESC LIMIT " + LIMIT_SUBSCRIBED;
		return getMessagesFromQuery(query, idUser);
	}

	/**
	 * Gets the list of all the messages (followed and not followed) The
	 * userName is used to determine if the messages have been read or not
	 */
	public static List<Message> getAllMessages(String userName, int limit) {
		int idUser = 0;
		if(userName != null) {
			idUser = getUser(userName).getId();
		}
		String limitStr = "";
		if (limit > 0) {
			limitStr = "LIMIT = " + limit;
		}
		return getMessagesFromQuery("SELECT * FROM messages AS m LEFT JOIN users_recommendations AS ur "
			+ " ON m.idMessage = ur.idmessage " + limitStr, idUser);
	}

	/**
	 * Gets the list of the messages that a user is following after one given
	 * Uses the default limit
	 */
	public static List<Message> getAllMessagesSinceLast(String userName,
			int idMessageLast) {
		int idUser = getUser(userName).getId();
		String query = "SELECT * FROM messages AS m LEFT JOIN users_recommendations AS ur "
			+ " ON m.idMessage = ur.idmessage " +
				" LIMIT " + LIMIT_ALL;
		return getMessagesFromQuery(query, idUser);
	}
	
	/**
	 * Gets some messages: subscribed, not subscribed and recommended
	 * according to the limits 
	 * Uses the default limit
	 */
	public static List<Message> getMessagesIncludingRecommended(String userName,
			Message lastMessage) {
		int idUser = getUser(userName).getId();
		String query = "(SELECT * FROM messages AS m, "
			+ "users_recommendations AS ur WHERE m.idmessage = ur.idmessage "
			+ "AND ur.iduser = "+ idUser + " AND m.date >= '" + getDateTimestamp(lastMessage)
			+ "' AND m.date > " + lastMessage.getId() 
			+ " ORDER BY m.date DESC LIMIT " + LIMIT_RECOMMENDATIONS 
			+ ") UNION (SELECT * FROM messages AS m LEFT JOIN "
			+ "users_recommendations AS ur ON m.idmessage = ur.idmessage "
			+ "WHERE m.date >= '" + getDateTimestamp(lastMessage)
			+ "' AND m.date > " + lastMessage.getId() 
			+ " ORDER BY m.date DESC LIMIT " + LIMIT_ALL + ") UNION "
			+ "(SELECT DISTINCT m.*, ur.* FROM (messages AS m LEFT JOIN "
			+ "users_recommendations AS ur ON m.idmessage = ur.idmessage "
			+ "AND ur.iduser = "+ idUser + "), "
			+ "messages_tags AS mt WHERE m.idmessage = mt.idmessage AND "
			+ "idtag IN (SELECT idtag FROM users_tags WHERE iduser = " 
			+ idUser + ") AND m.date >= '" + getDateTimestamp(lastMessage)
			+ "' AND m.idmessage > " + lastMessage.getId()  
			+ " ORDER BY m.date DESC LIMIT " + LIMIT_SUBSCRIBED +");";
		return getMessagesFromQuery(query, idUser);
	}
	
	/**
	 * Gets some old messages: subscribed, not subscribed and recommended
	 * according to the limits 
	 * Uses the default limit
	 */
	public static List<Message> getOldMessagesIncludingRecommended(String userName,
			Message lastMessage, Message lastMessageFollowing, Message lastMessageRecommended) {
		int idUser = getUser(userName).getId();
		String query = "(SELECT * FROM messages AS m, "
			+ "users_recommendations AS ur WHERE m.idmessage = ur.idmessage "
			+ "AND ur.iduser = "+ idUser + " AND m.date < '" + getDateTimestamp(lastMessageRecommended)
			+ "' ORDER BY m.date DESC LIMIT " + LIMIT_RECOMMENDATIONS 
			+ ") UNION (SELECT * FROM messages AS m LEFT JOIN "
			+ "users_recommendations AS ur ON m.idmessage = ur.idmessage "
			+ "WHERE m.date < '" + getDateTimestamp(lastMessage)
			+ "' ORDER BY m.date DESC LIMIT " + LIMIT_ALL + ") UNION "
			+ "(SELECT DISTINCT m.*, ur.* FROM (messages AS m LEFT JOIN "
			+ "users_recommendations AS ur ON m.idmessage = ur.idmessage "
			+ "AND ur.iduser = "+ idUser + "), "
			+ "messages_tags AS mt WHERE m.idmessage = mt.idmessage AND "
			+ "idtag IN (SELECT idtag FROM users_tags WHERE iduser = " 
			+ idUser + ") AND m.date < '" + getDateTimestamp(lastMessageFollowing)
			+ "' ORDER BY m.date DESC LIMIT " + LIMIT_SUBSCRIBED +");";
		System.out.println(query); // TODO
		return getMessagesFromQuery(query, idUser);
	}
	
	private static String getDateTimestamp(Message message) {
		return new Timestamp(message.getDate().getTime()).toString();
	}
	
	

	public static Message getMessageByItsIdAPI(long idMessageAPI, int idUser) {
		
		if(idMessageAPI == 0) {
			return null;
		}
		connectToDatabase();
		Message message = null;
		try {
			ResultSet rs = stmt
					.executeQuery("SELECT * FROM messages WHERE idMessageAPI = "
							+ idMessageAPI + " LIMIT 1");
			if (rs.next()) {
				message = getMessageFromRS(rs, idUser);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		closeConnectionDatabase();
		return message;
	}
	
	public static String getMessageTitleByItsIdAPI(long idMessageAPI) {
		
		connectToDatabase();
		String title = null;
		try {
			ResultSet rs = stmt
					.executeQuery("SELECT title FROM messages WHERE idMessageAPI = "
							+ idMessageAPI + " LIMIT 1");
			if (rs.next()) {
				title = rs.getString("title");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		closeConnectionDatabase();
		return title;
	}
	
	private static List<Message> getMessagesFromQuery(String query, int idUser) {
		connectToDatabase();
		// TODO
		System.out.println(query + " iduser " + idUser);
		List<Message> messages = new ArrayList<Message>();
		try {
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				messages.add(getMessageFromRS(rs, idUser));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		closeConnectionDatabase();
		return messages;
	}

	private static List<Tag> getTagListFromQuery(String query) {
		List<Tag> tags = new ArrayList<Tag>();
		try {
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				tags.add(new Tag(rs.getInt(1), rs.getString(2)));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return tags;
	}

	private static User getUserFromQuery(String query) {
		connectToDatabase();
		User user = null;
		try {
			ResultSet rs = stmt.executeQuery(query);
			if (rs.next()) {
				user = new User(rs.getInt("idUser"), rs.getString("userName"),
						rs.getString("password"), rs.getString("name"), rs
								.getString("location"), rs.getString("email"),
						getTagsFollowing(rs.getInt("idUser")));
				// Constructor:
				// id,userName,password,name,location,email,tagsFollowing
				// Db:
				// IDUSER,USERNAME,PASSWORD,NAME,SURNAME,SEX,URL,LOCATION,EMAIL,INITIALDATE
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		closeConnectionDatabase();
		return user;
	}
	
	private static String getUserName(int idUser) {
		connectToDatabase();
		String userName = null;
		try {
			ResultSet rs = stmt.executeQuery("SELECT userName FROM users WHERE idUser = "
					+ idUser);
			if (rs.next()) {
				userName = rs.getString("userName");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		//closeConnectionDatabase();
		return userName;
	}
	
    /**
     * Updates the read and liked properties for a message list and user
     */
    public static void updateReadAndLiked(List<Message> messages, int idUser) {
            for (Message m : messages) {
                    updateReadAndLiked(m, idUser);
            }
    }

	public static List<Tag> getTagsFollowing(int idUser) {
		return getTagListFromQuery("SELECT t.* FROM users_tags AS ut, tags AS t WHERE t.idTag = ut.idTag AND idUser = "
				+ idUser);
	}

	private static List<Tag> getMessageTags(int idMessage) {
		return getTagListFromQuery("SELECT t.* FROM messages_tags AS mt, tags AS t WHERE t.idTag = mt.idTag AND idMessage = "
				+ idMessage);
	}

	public static List<Tag> getTagsSinceLast(int idLastTag) {
		connectToDatabase();
		List<Tag> tags = getTagListFromQuery("SELECT * FROM tags WHERE idtag > "
				+ idLastTag);
		closeConnectionDatabase();
		return tags;
	}

	private static void setMessageId(Message message, int idAuthor) {

		String query = "SELECT idMessage" + " FROM messages " +
		// " WHERE iduser = " + idAuthor +
				// " AND text = '" + message.getText() + "'" +
				" ORDER BY idmessage DESC LIMIT 1;";
		try {
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				message.setId(rs.getInt("idmessage"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Checks and sets the read and liked attribute for a given message and user
	 */
	private static void setReadAndLiked(Message m, int idUser) {
		try {
			ResultSet rs = stmt
					.executeQuery("SELECT liked FROM users_messages WHERE iduser = "
							+ idUser + " AND idmessage = " + m.getId());
			if (rs.next()) {
				m.setRead(true);
				if (rs.getInt(1) == 1)
					m.setLiked(true);
				else
					m.setLiked(false);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static Message getMessageFromRS(ResultSet rs, int idUser) {
		// Create the message; read and liked false by default
		Message m;
		try {
			m = new Message(rs.getInt(1), getUserName(rs.getInt(2)),
					rs.getString(3), rs.getString(4), getMessageTags(rs
							.getInt(1)),
					new Date(rs.getTimestamp(5).getTime()), false, false, rs
							.getInt(6), rs.getInt(7), rs.getDouble(11), 
							rs.getLong(12));
		} catch (SQLException e) {
			System.out.println("SQL Exception");
			return null;
		}
		if(idUser > 0) {
			// Set the read and liked properties
			setReadAndLiked(m, idUser);
		}
		return m;
		// Constructor:
		// id,userName,title,text,tags,date,read,liked,rating,timesRead
		// Db: IDMESSAGE,IDUSER,TITLE,TEXT,DATE,RATING,TIMES_READ
	}

	public static void takeRecommendationData(
			HashMap<String, Object> recommendationData, String dimensions[],
			String table, String userColumn, String messageColumn,
			String likedColumn) {

		connectToDatabase();

		String query = "SELECT " + userColumn + "," + messageColumn + ","
				+ likedColumn + " from " + table + " order by " + userColumn
				+ "," + messageColumn;
		try {
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				int user = rs.getInt(userColumn);
				int message = rs.getInt(messageColumn);
				int liked = rs.getInt(likedColumn);
				HashMap<Long, HashMap<Long, Float>> readData = (HashMap<Long, HashMap<Long, Float>>) recommendationData
						.get(dimensions[0]);
				HashMap<Long, Float> hashMapByUid = (HashMap<Long, Float>) readData
						.get(Long.valueOf(user));
				if (hashMapByUid == null) {
					HashMap<Long, Float> newUserHashMap = new HashMap<Long, Float>();
					newUserHashMap.put(Long.valueOf(message), 1f);
					readData.put(Long.valueOf(user), newUserHashMap);
				} else
					hashMapByUid.put(Long.valueOf(message), Float
							.valueOf(liked));
				if (liked != 0) {
					HashMap<Long, HashMap<Long, Float>> likedData = (HashMap<Long, HashMap<Long, Float>>) recommendationData
							.get(dimensions[1]);
					hashMapByUid = (HashMap<Long, Float>) likedData.get(Long
							.valueOf(user));
					if (hashMapByUid == null) {
						HashMap<Long, Float> newUserHashMap = new HashMap<Long, Float>();
						newUserHashMap.put(Long.valueOf(message), Float
								.valueOf(liked));
						likedData.put(Long.valueOf(user), newUserHashMap);
					} else
						hashMapByUid.put(Long.valueOf(message), Float
								.valueOf(liked));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		closeConnectionDatabase();
	}

	public static void updateRecommendationData(
			RecommendationGenerator recommender, String dimensions[],
			Date date, String table, String userColumn, String messageColumn,
			String likedColumn, String updateDate) {
		connectToDatabase();

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String query = "SELECT " + userColumn + "," + messageColumn + ","
				+ likedColumn + " from " + table + " where " + updateDate
				+ " > \'" + dateFormat.format(date) + "\' order by "
				+ userColumn + "," + messageColumn;
		try {
			System.out.println(query);
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				int user = rs.getInt(userColumn);
				int message = rs.getInt(messageColumn);
				int liked = rs.getInt(likedColumn);
				System.out.println(user + "," + message + "," + liked);
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
				}
				recommender.putRating(dimensions[0], user, message, 1);
				if (liked != 0)
					recommender.putRating(dimensions[1], user, message, liked);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		closeConnectionDatabase();
	}

	public static void saveUserRecommendationData(int userID,
			HashMap<Long, Float> userRecommendations) {

		String query = "INSERT INTO users_recommendations VALUES (Null, ?, ?, ?)";
		PreparedStatement prep;
		try {
			prep = con.prepareStatement(query);

			prep.setInt(2, userID);

			Iterator<Long> recomendationsIterator = userRecommendations
					.keySet().iterator();
			while (recomendationsIterator.hasNext()) {
				Long idMessage = recomendationsIterator.next();
				prep.setInt(1, idMessage.intValue());
				prep.setFloat(3, userRecommendations.get(idMessage));
				prep.executeUpdate();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void deleteUserRecommendationData(int userID) {

		try {
			stmt.executeUpdate("DELETE FROM users_recommendations"
					+ " WHERE idUser = " + userID);
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public static void updateUserRecommendationData(int userID,
			HashMap<Long, Float> userRecommendations) {

		connectToDatabase();

		deleteUserRecommendationData(userID);

		saveUserRecommendationData(userID, userRecommendations);

		closeConnectionDatabase();

	}

	public static List<Tag> getAllTags() {
		connectToDatabase();
		List<Tag> tags = new ArrayList<Tag>();
		tags = getTagListFromQuery("SELECT * FROM tags");
		closeConnectionDatabase();
		return tags;
	}

	public static void insertTag(String abb, String description) {

		int maxLengthAbb = 25;
		if(abb.length() > maxLengthAbb)
			abb.substring(0, maxLengthAbb-1);
		
		connectToDatabase();
		String query = "insert into tags(TAGABBREVIATION, DESCRIPTION) values ('"
				+ abb + "', '" + description + "')";
		try {
			stmt.executeUpdate(query);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		closeConnectionDatabase();
	}
	
	public static int getIdTag(String abb) {

		connectToDatabase();
		int idTag = -1;
		String query = "select idTag from tags where TAGABBREVIATION = '" + abb + "'";
		try {
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				idTag = rs.getInt("idTag");
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		closeConnectionDatabase();
		return idTag;
	}

	public static HashMap<Long, Float> getUserRecommendationData(String userName) {

		connectToDatabase();
		HashMap<Long, Float> userRecommendationData = new HashMap<Long, Float>();

		String query = "SELECT idMessage, user_affinity"
				+ " FROM users_recommendations" + " WHERE idUser = "
				+ getUser(userName).getId();

		try {
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				userRecommendationData.put(new Long(rs.getInt("idMessage")), rs
						.getFloat("user_affinity"));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		closeConnectionDatabase();
		return userRecommendationData;
	}

	public static Float getUserRecommendationForAMessage(int userID,
			long idMessage) {

		String query = "SELECT idMessage, user_affinity"
				+ " FROM users_recommendations" + " WHERE idUser = " + userID
				+ " AND idMessage = " + idMessage;

		try {
			ResultSet rs = stmt.executeQuery(query);
			if (rs.next()) {
				return rs.getFloat("user_affinity");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return new Float(0);
	}

	public static void closeConnectionDatabase() {
		try {
			if (stmt != null)
				stmt.close();
			if (con != null)
				con.close();
		} catch (SQLException e) {

		}
	}

	public static Timestamp getDateLastMessage(String tag) {
		connectToDatabase();

		String query = "SELECT m.date"
				+ " FROM messages as m, messages_tags as mt, tags as t"
				+ " WHERE m.idMessage = mt.idMessage AND mt.idTag = t.idTag and t.tagabbreviation = '"
				+ tag + "'" + " ORDER BY m.date DESC LIMIT 1";

		try {
			ResultSet rs = stmt.executeQuery(query);
			if (rs.next()) {
				Timestamp t = rs.getTimestamp("m.date");
				closeConnectionDatabase();
				return t;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		closeConnectionDatabase();
		return new Timestamp(0);
	}
}