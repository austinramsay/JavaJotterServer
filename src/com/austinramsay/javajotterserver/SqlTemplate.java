package com.austinramsay.javajotterserver;

import java.util.ArrayList;

public class SqlTemplate {
	public final static String GET_ALL_NOTEBOOKS = "SELECT * FROM notebooks";

	public static String getPasswordQueryString() {
		return String.format("SELECT password FROM users WHERE username = ?");
	}

	public static String getUserNotebooksQueryString() {
		return String.format("SELECT * FROM notebooks WHERE user_id = ?");
	}

	public static String getUserNotesQueryString(ArrayList<Integer> nbIdList) {
		return String.format("SELECT * FROM notes WHERE notebook_id IN (%s) AND user_id = ?", buildNotebookIdListString(nbIdList));
	}

	public static String buildNotebookIdListString(ArrayList<Integer> nbIdList) {
		StringBuilder placeholders = new StringBuilder();
		for (int i = 0; i < nbIdList.size(); i++) {
			placeholders.append("?");
			if (i != nbIdList.size() - 1) {
				placeholders.append(", ");
			}
		}
		return placeholders.toString();
	}

	public static String getNoteIDsInNotebookQueryString() {
		//return String.format("SELECT note_id FROM notes WHERE user_id = ? AND notebook_id = ?");
		return String.format("SELECT %s FROM %s WHERE %s = ? AND %s = ?",
				DatabaseManager.NOTES_TABLE_NOTE_ID_COLUMN_NAME,
				DatabaseManager.NOTES_TABLE_NAME,
				DatabaseManager.USER_ID_COLUMN_NAME,
				DatabaseManager.NOTES_TABLE_NOTEBOOK_ID_COLUMN_NAME);
	}

	public static String getUserIdQueryString() {
		//return String.format("SELECT user_id FROM users WHERE username = ?");
		return String.format("SELECT %s FROM %s WHERE %s = ?",
				DatabaseManager.USER_ID_COLUMN_NAME,
				DatabaseManager.USERS_TABLE_NAME,
				DatabaseManager.USERS_TABLE_USERNAME_COLUMN_NAME);
	}

	public static String getLastNbIdQueryString() {
		//return String.format("SELECT MAX(notebook_id) FROM notebooks WHERE user_id = ?");
		return String.format("SELECT MAX(%s) FROM %s WHERE %s = ?",
				DatabaseManager.NOTEBOOKS_TABLE_NOTEBOOK_ID_COLUMN_NAME,
				DatabaseManager.NOTEBOOKS_TABLE_NAME,
				DatabaseManager.USER_ID_COLUMN_NAME);
	}

	public static String getLastNoteIdQueryString() {
		//return String.format("SELECT MAX(note_id) FROM notes WHERE user_id = ?");
		return String.format("SELECT MAX(%s) FROM %s WHERE %s = ?",
				DatabaseManager.NOTES_TABLE_NOTE_ID_COLUMN_NAME,
				DatabaseManager.NOTES_TABLE_NAME,
				DatabaseManager.USER_ID_COLUMN_NAME);
	}

	public static String getInsertNotebookStatementString() {
		//return String.format("INSERT INTO notebooks (notebook_id, parent_id, user_id, title) VALUES (?, ?, ?, ?)");
		return String.format("INSERT INTO %s (%s, %s, %s, %s) VALUES (?, ?, ?, ?)",
				DatabaseManager.NOTEBOOKS_TABLE_NAME,
				DatabaseManager.NOTEBOOKS_TABLE_NOTEBOOK_ID_COLUMN_NAME,
				DatabaseManager.NOTEBOOKS_TABLE_PARENT_ID_COLUMN_NAME,
				DatabaseManager.USER_ID_COLUMN_NAME,
				DatabaseManager.NOTEBOOKS_TABLE_TITLE_COLUMN_NAME);
	}

	public static String getInsertNoteStatementString() {
		//return String.format("INSERT INTO notes (note_id, notebook_id, user_id, title, content) VALUES (?, ?, ?, ?, ?)");
		return String.format("INSERT INTO %s (%s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?)",
				DatabaseManager.NOTES_TABLE_NAME,
				DatabaseManager.NOTES_TABLE_NOTE_ID_COLUMN_NAME,
				DatabaseManager.NOTES_TABLE_NOTEBOOK_ID_COLUMN_NAME,
				DatabaseManager.USER_ID_COLUMN_NAME,
				DatabaseManager.NOTES_TABLE_TITLE_COLUMN_NAME,
				DatabaseManager.NOTES_TABLE_CONTENT_COLUMN_NAME,
				DatabaseManager.NOTES_TABLE_IMG_ATTACH_MAP_COLUMN_NAME);
	}

	public static String getDeleteNotebookStatementString() {
		//return String.format("DELETE FROM notebooks WHERE user_id = ? AND notebook_id = ?");
		return String.format("DELETE FROM %s WHERE %s = ? AND %s = ?",
				DatabaseManager.NOTEBOOKS_TABLE_NAME,
				DatabaseManager.USER_ID_COLUMN_NAME,
				DatabaseManager.NOTEBOOKS_TABLE_NOTEBOOK_ID_COLUMN_NAME);
	}

	public static String getDeleteNoteStatementString() {
		//return String.format("DELETE FROM notes WHERE user_id = ? AND note_id = ?");
		return String.format("DELETE FROM %s WHERE %s = ? AND %s = ?",
				DatabaseManager.NOTES_TABLE_NAME,
				DatabaseManager.USER_ID_COLUMN_NAME,
				DatabaseManager.NOTES_TABLE_NOTE_ID_COLUMN_NAME);
	}

	public static String getChildNbIDsQueryString() {
		//return String.format("SELECT notebook_id FROM notebooks WHERE user_id = ? AND parent_id = ?");
		return String.format("SELECT %s FROM %s WHERE %s = ? AND %s = ?",
				DatabaseManager.NOTEBOOKS_TABLE_NOTEBOOK_ID_COLUMN_NAME,
				DatabaseManager.NOTEBOOKS_TABLE_NAME,
				DatabaseManager.USER_ID_COLUMN_NAME,
				DatabaseManager.NOTEBOOKS_TABLE_PARENT_ID_COLUMN_NAME);
	}

	public static String getNoteExistsQueryString() {
		//return String.format("SELECT * FROM notes WHERE user_id = ? AND note_id = ?");
		return String.format("SELECT * FROM %s WHERE %s = ? AND %s = ?",
				DatabaseManager.NOTES_TABLE_NAME,
				DatabaseManager.USER_ID_COLUMN_NAME,
				DatabaseManager.NOTES_TABLE_NOTE_ID_COLUMN_NAME);
	}

	public static String getUpdateColumnStatementString() {
		// UPDATE table_name SET column_to_update = new_value WHERE column_name = identifier
		return String.format("UPDATE ? SET ? = ? WHERE ? = ? AND %s = ?",
				DatabaseManager.USER_ID_COLUMN_NAME);
	}

	public static String getUpdateNotebookTitleString() {
		return String.format("UPDATE %s SET %s = ? WHERE %s = ? AND %s = ?",
				DatabaseManager.NOTEBOOKS_TABLE_NAME,
				DatabaseManager.NOTEBOOKS_TABLE_TITLE_COLUMN_NAME,
				DatabaseManager.USER_ID_COLUMN_NAME,
				DatabaseManager.NOTEBOOKS_TABLE_NOTEBOOK_ID_COLUMN_NAME);
	}

	public static String getUpdateNotebookParentIdString() {
		return String.format("UPDATE %s SET %s = ? WHERE %s = ? AND %s = ?",
				DatabaseManager.NOTEBOOKS_TABLE_NAME,
				DatabaseManager.NOTEBOOKS_TABLE_PARENT_ID_COLUMN_NAME,
				DatabaseManager.USER_ID_COLUMN_NAME,
				DatabaseManager.NOTEBOOKS_TABLE_NOTEBOOK_ID_COLUMN_NAME);
	}

	public static String getUpdateNoteMappedNotebookIdString() {
		return String.format("UPDATE %s SET %s = ? WHERE %s = ? AND %s = ?",
				DatabaseManager.NOTES_TABLE_NAME,
				DatabaseManager.NOTES_TABLE_NOTEBOOK_ID_COLUMN_NAME,
				DatabaseManager.USER_ID_COLUMN_NAME,
				DatabaseManager.NOTES_TABLE_NOTE_ID_COLUMN_NAME);
	}

	public static String getUpdateNoteContentString() {
		return String.format("UPDATE %s SET %s = ? WHERE %s = ? AND %s = ?",
				DatabaseManager.NOTES_TABLE_NAME,
				DatabaseManager.NOTES_TABLE_CONTENT_COLUMN_NAME,
				DatabaseManager.USER_ID_COLUMN_NAME,
				DatabaseManager.NOTES_TABLE_NOTE_ID_COLUMN_NAME);
	}

	public static String getUpdateNoteImgAttachMapString() {
		return String.format("UPDATE %s SET %s = ? WHERE %s = ? AND %s = ?",
				DatabaseManager.NOTES_TABLE_NAME,
				DatabaseManager.NOTES_TABLE_IMG_ATTACH_MAP_COLUMN_NAME,
				DatabaseManager.USER_ID_COLUMN_NAME,
				DatabaseManager.NOTES_TABLE_NOTE_ID_COLUMN_NAME);
	}

	public static String getUpdateNoteTitleString() {
		return String.format("UPDATE %s SET %s = ? WHERE %s = ? AND %s = ?",
				DatabaseManager.NOTES_TABLE_NAME,
				DatabaseManager.NOTES_TABLE_TITLE_COLUMN_NAME,
				DatabaseManager.USER_ID_COLUMN_NAME,
				DatabaseManager.NOTES_TABLE_NOTE_ID_COLUMN_NAME);
	}
}