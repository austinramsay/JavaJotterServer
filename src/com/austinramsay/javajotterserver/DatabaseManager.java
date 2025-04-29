package com.austinramsay.javajotterserver;

import java.util.ArrayList;
import java.sql.*;
import java.util.Properties;

public class DatabaseManager {

	public final static String USER_ID_COLUMN_NAME = "user_id";
	public final static String USERS_TABLE_NAME = "users";
	public final static String USERS_TABLE_USERNAME_COLUMN_NAME = "username";
	public final static String USERS_TABLE_PASSWORD_COLUMN_NAME = "password";
	public final static String USERS_TABLE_FIRSTNAME_COLUMN_NAME = "first_name";
	public final static String USERS_TABLE_LASTNAME_COLUMN_NAME = "last_name";
	public final static String NOTES_TABLE_NAME = "notes";
	public final static String NOTES_TABLE_NOTE_ID_COLUMN_NAME = "note_id";
	public final static String NOTES_TABLE_NOTEBOOK_ID_COLUMN_NAME = "notebook_id";
	public final static String NOTES_TABLE_TITLE_COLUMN_NAME = "title";
	public final static String NOTES_TABLE_CONTENT_COLUMN_NAME = "content";
	public final static String NOTES_TABLE_IMG_ATTACH_MAP_COLUMN_NAME = "img_attach_map";
	public final static String NOTEBOOKS_TABLE_NAME = "notebooks";
	public final static String NOTEBOOKS_TABLE_NOTEBOOK_ID_COLUMN_NAME = "notebook_id";
	public final static String NOTEBOOKS_TABLE_PARENT_ID_COLUMN_NAME = "parent_id";
	public final static String NOTEBOOKS_TABLE_TITLE_COLUMN_NAME = "title";

	private String dbUsername = "javajotter";
	private String dbPassword = "javajotter";

	private Connection dbConn;

	/*
	 * Create database connection upon instantiation
	 */
	public DatabaseManager() {
		try {
			Properties props = new Properties();
			props.put("user", dbUsername);
			props.put("password", dbPassword);
			dbConn = java.sql.DriverManager.getConnection(Server.dbUrl, props);
		} catch (Exception e) {
			System.out.println("Could not start database manager.");
			e.printStackTrace();
		}
	}

	public void closeConnection() {
		try {
			dbConn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ResultSet getUserIdQuery(String username) {
		PreparedStatement ps = null;
		try {
			ps = dbConn.prepareStatement(SqlTemplate.getUserIdQueryString());
			ps.setString(1, username);
			ResultSet result = ps.executeQuery();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public ResultSet getPasswordQuery(String username) {
		PreparedStatement ps = null;
		try {
			ps = dbConn.prepareStatement(SqlTemplate.getPasswordQueryString());
			ps.setString(1, username);
			ResultSet result = ps.executeQuery();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public ResultSet getChildNbIdsQuery(Integer userId, Integer nbId) {
		PreparedStatement ps = null;
		try {
			ps = dbConn.prepareStatement(SqlTemplate.getChildNbIDsQueryString());
			ps.setInt(1, userId);
			ps.setInt(2, nbId);
			ResultSet result = ps.executeQuery();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public ResultSet getNoteIDsInNotebookQuery(Integer userId, Integer nbId) {
		PreparedStatement ps = null;
		try {
			ps = dbConn.prepareStatement(SqlTemplate.getNoteIDsInNotebookQueryString());
			ps.setInt(1, userId);
			ps.setInt(2, nbId);
			ResultSet result = ps.executeQuery();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public ResultSet getLastNbIdQuery(Integer userId) {
		PreparedStatement ps = null;
		try {
			ps = dbConn.prepareStatement(SqlTemplate.getLastNbIdQueryString());
			ps.setInt(1, userId);
			ResultSet result = ps.executeQuery();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public ResultSet getLastNoteIdQuery(Integer userId) {
		PreparedStatement ps = null;
		try {
			ps = dbConn.prepareStatement(SqlTemplate.getLastNoteIdQueryString());
			ps.setInt(1, userId);
			ResultSet result = ps.executeQuery();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public ResultSet getUserNotebooksQuery(Integer userId) {
		PreparedStatement ps = null;
		try {
			ps = dbConn.prepareStatement(SqlTemplate.getUserNotebooksQueryString());
			ps.setInt(1, userId);
			ResultSet result = ps.executeQuery();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public ResultSet getUserNotesQuery(Integer userId, ArrayList<Integer> nbIdList) {
		PreparedStatement ps = null;
		try {
			ps = dbConn.prepareStatement(SqlTemplate.getUserNotesQueryString(nbIdList));
			int index = 1;
			for (Integer i : nbIdList) {
				ps.setInt(index++, i);
			}
			ps.setInt(index, userId);
			ResultSet result = ps.executeQuery();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public boolean noteExists(Integer userId, Integer noteId) {
		ResultSet result = null;
		try {
			PreparedStatement ps = dbConn.prepareStatement(SqlTemplate.getNoteExistsQueryString());
			ps.setInt(1, userId);
			ps.setInt(2, noteId);
			result = ps.executeQuery();
			return result.first();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			try { if (result != null) result.close(); } catch (Exception e) { e.printStackTrace(); }
		}
	}

	public void insertNotebook(Integer nbId, Integer parentNbId, Integer userId, String title) {
		PreparedStatement ps = null;
		try {
			String sql = SqlTemplate.getInsertNotebookStatementString();
			ps = dbConn.prepareStatement(sql);

			// Put data into statement (notebook_id, parent_id, user_id, title)
			ps.setInt(1, nbId);
			if (parentNbId == null) {
				ps.setNull(2, java.sql.Types.INTEGER);
			} else {
				ps.setInt(2, parentNbId);
			}
			ps.setInt(3, userId);
			ps.setString(4, title);

			ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { if (ps != null) ps.close(); } catch (Exception e) { e.printStackTrace(); }
		}
	}

	public void insertNote(Integer noteId, Integer nbId, Integer userId, String title, Blob content, Blob imgAttachMap) {
		PreparedStatement ps = null;
		try {
			String sql = SqlTemplate.getInsertNoteStatementString();
			ps = dbConn.prepareStatement(sql);

			// Put data into statement (note_id, notebook_id, user_id, title, content, img_attach_map)
			ps.setInt(1, noteId);
			ps.setInt(2, nbId);
			ps.setInt(3, userId);
			ps.setString(4, title);
			ps.setBlob(5, content);
			ps.setBlob(6, imgAttachMap);

			ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { if (ps != null) ps.close(); } catch (Exception e) { e.printStackTrace(); }
		}
	}

	public void deleteNotebook(Integer nbId, Integer userId) {
		PreparedStatement ps = null;
		try {
			String sql = SqlTemplate.getDeleteNotebookStatementString();
			ps = dbConn.prepareStatement(sql);

			// Put data into statement (user_id, notebook_id)
			ps.setInt(1, userId);
			ps.setInt(2, nbId);

			ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { if (ps != null) ps.close(); } catch (Exception e) { e.printStackTrace(); }
		}
	}

	public void deleteNote(Integer noteId, Integer userId) {
		PreparedStatement ps = null;
		try {
			String sql = SqlTemplate.getDeleteNoteStatementString();
			ps = dbConn.prepareStatement(sql);

			// Put data into statement (user_id, note_id)
			ps.setInt(1, userId);
			ps.setInt(2, noteId);

			ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { if (ps != null) ps.close(); } catch (Exception e) { e.printStackTrace(); }
		}
	}

	public void updateGenericStrValRefUserId(
			String tableName,
			String columnToUpdate,
			String newValue,
			String identifyingColumn,
			Integer identifyingValue,
			Integer userId) {
		PreparedStatement ps = null;
		try {
			String sql = SqlTemplate.getUpdateColumnStatementString();
			ps = dbConn.prepareStatement(sql);

			ps.setString(1, tableName);
			ps.setString(2, columnToUpdate);
			ps.setString(3, newValue);
			ps.setString(4, identifyingColumn);
			ps.setInt(5, identifyingValue);
			ps.setInt(6, userId);

			ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { if (ps != null) ps.close(); } catch (Exception e) { e.printStackTrace(); }
		}
	}

	public void updateNoteContent(Integer noteId, Integer userId, Blob content) {
		// UPDATE notes SET content = ? WHERE user_id = ? AND note_id = ?
		PreparedStatement ps = null;
		try {
			String sql = SqlTemplate.getUpdateNoteContentString();
			ps = dbConn.prepareStatement(sql);

			ps.setBlob(1, content);
			ps.setInt(2, userId);
			ps.setInt(3, noteId);

			ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { if (ps != null) ps.close(); } catch (Exception e) { e.printStackTrace(); }
		}
	}

	public void updateNoteImgAttachMap(Integer noteId, Integer userId, Blob imgAttachMap) {
		// UPDATE notes SET img_attach_map = ? WHERE user_id = ? AND note_id = ?
		PreparedStatement ps = null;
		try {
			String sql = SqlTemplate.getUpdateNoteImgAttachMapString();
			ps = dbConn.prepareStatement(sql);

			ps.setBlob(1, imgAttachMap);
			ps.setInt(2, userId);
			ps.setInt(3, noteId);

			ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { if (ps != null) ps.close(); } catch (Exception e) { e.printStackTrace(); }
		}
	}

	public Blob generateBlob(byte[] content) {
		try {
			Blob contentBlob = dbConn.createBlob();
			contentBlob.setBytes(1, content);
			return contentBlob;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void updateNotebookTitle(Integer nbId, Integer userId, String title) {
		// UPDATE notebooks SET title = ? WHERE user_id = ? AND notebook_id = ?
		PreparedStatement ps = null;
		try {
			String sql = SqlTemplate.getUpdateNotebookTitleString();
			ps = dbConn.prepareStatement(sql);

			ps.setString(1, title);
			ps.setInt(2, userId);
			ps.setInt(3, nbId);

			ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { if (ps != null) ps.close(); } catch (Exception e) { e.printStackTrace(); }
		}
	}

	public void updateNotebookParentId(Integer nbId, Integer userId, Integer newParentId) {
		PreparedStatement ps = null;
		try {
			String sql = SqlTemplate.getUpdateNotebookParentIdString();
			ps = dbConn.prepareStatement(sql);

			if (newParentId == null) {
				ps.setNull(1, java.sql.Types.INTEGER);
			} else {
				ps.setInt(1, newParentId);
			}
			ps.setInt(2, userId);
			ps.setInt(3, nbId);

			ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { if (ps != null) ps.close(); } catch (Exception e) { e.printStackTrace(); }
		}
	}

	public void updateNoteMappedNotebookId(Integer noteId, Integer userId, Integer newNotebookId) {
		PreparedStatement ps = null;
		try {
			String sql = SqlTemplate.getUpdateNoteMappedNotebookIdString();
			ps = dbConn.prepareStatement(sql);

			ps.setInt(1, newNotebookId);
			ps.setInt(2, userId);
			ps.setInt(3, noteId);

			ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { if (ps != null) ps.close(); } catch (Exception e) { e.printStackTrace(); }
		}
	}

	public void updateNoteTitle(Integer noteId, Integer userId, String title) {
		// UPDATE notes SET title = ? WHERE user_id = ? AND note_id = ?
		PreparedStatement ps = null;
		try {
			String sql = SqlTemplate.getUpdateNoteTitleString();
			ps = dbConn.prepareStatement(sql);

			ps.setString(1, title);
			ps.setInt(2, userId);
			ps.setInt(3, noteId);

			ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { if (ps != null) ps.close(); } catch (Exception e) { e.printStackTrace(); }
		}
	}
}