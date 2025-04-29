package com.austinramsay.javajotterserver;

import com.austinramsay.javajotterlibrary.*;
import java.net.Socket;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.sql.Blob;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.net.URL;

public class ClientManager implements Runnable {

	private Server server;
	private ClientNetworkManager cnm;
	private DatabaseManager dbm;

	private String username;
	private Integer userId;
	private boolean isAuthorized = false;

	public ClientManager(Server server, Socket clientConn) {
		this.server = server;
		cnm = new ClientNetworkManager(this, clientConn);
		dbm = new DatabaseManager();
	}

	@Override
	public void run() {
		cnm.receive();
	}

	public void process(Object recvObj) {
		if (recvObj instanceof Authenticator) {
			Authenticator auth = (Authenticator) recvObj;
			authenticate(auth);
		} else if (recvObj instanceof Request) {
			Request req = (Request) recvObj;
			processRequest(req);
		} else if (recvObj instanceof Transaction) {
			Transaction t = (Transaction) recvObj;
			processTransaction(t);
		} else if (recvObj instanceof Notebook) {
			Notebook nb = (Notebook) recvObj;
			addNotebook(nb);
		} else if (recvObj instanceof Note) {
			Note n = (Note) recvObj;
			addNote(n);
		} else {
			System.out.println("Unknown or unwanted object received.");
		}
	}

	private void authenticate(Authenticator auth) {
		this.username = auth.getUsername();
		// Check if the user is already logged in
		if (server.userIsLoggedIn(auth.getUsername())) {
			// The user is already connected, send message and disconnect
			auth.stripCredentials();
			auth.setHasExistingConnection(true);
			cnm.send(auth);
			disconnect();
			return;
		}

		// Compare username/password to entries in database
		ResultSet authResult = dbm.getPasswordQuery(auth.getUsername());

		try {
			String correctPwd;

			if (authResult.next()) {
				correctPwd = authResult.getString(DatabaseManager.USERS_TABLE_PASSWORD_COLUMN_NAME);
			} else {
				correctPwd = "";
			}

			String recvPwd = auth.getPassword();
			auth.stripCredentials();

			if (recvPwd.equals(correctPwd)) {
				System.out.println("User \'" + username + "\' has connected.");
				setUserId(auth.getUsername());
				isAuthorized = true;
				server.registerSession(username);
				auth.setAuthenticated(true);
				cnm.send(auth);
				sendContentPackage();
			} else {
				System.out.println("User \'" + username + "\' failed to authenticate.");
				auth.setAuthenticated(false);
				cnm.send(auth);
				disconnect();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { if (authResult != null) authResult.close(); } catch (Exception e) { e.printStackTrace(); }
		}
	}

	private void setUserId(String username) {
		ResultSet userIdResult = dbm.getUserIdQuery(username);

		try {
			if (userIdResult.next()) {
				userId = userIdResult.getInt(DatabaseManager.USER_ID_COLUMN_NAME);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { if (userIdResult != null) userIdResult.close(); } catch (Exception e) { e.printStackTrace(); }
		}
	}

	private void processRequest(Request req) {
		if (req == Request.USER_CONTENT) {
			// Send a notebooks package to the client
			sendContentPackage();
		}
	}

	private void processTransaction(Transaction t) {
		Integer entityId = t.getId();
		if (t.getEntity() == Entity.NOTEBOOK && t.getType() == TransactionType.DELETE) {
			deleteNotebook(entityId);
		} else if (t.getEntity() == Entity.NOTE && t.getType() == TransactionType.DELETE) {
			deleteNote(entityId);
		} else if (t.getEntity() == Entity.NOTEBOOK && t.getType() == TransactionType.UPDATE) {
			updateNotebook(t.getId(), t.getUpdateField(), t.getUpdate());
		} else if (t.getEntity() == Entity.NOTE && t.getType() == TransactionType.UPDATE) {
			if (t.getUpdateField() == UpdateType.CONTENT) {
				updateNoteContent(t.getId(), t.getContentUpdate());
			} else if (t.getUpdateField() == UpdateType.IMAGE_ATTACHMENTS) {
				updateNoteImgAttachMap(t.getId(), t.getContentUpdate());
			} else {
				updateNote(t.getId(), t.getUpdateField(), t.getUpdate());
			}
		}
	}

	private void sendContentPackage() {
		ContentPackage userContent = new ContentPackage();

		// Keep track of all notebookIds that are for this user in the database
		// We'll use this to cross search for notes later.
		ArrayList<Integer> nbIdList = new ArrayList<Integer>();

		// SQL query for retrieving all notebooks assigned to this user
		ResultSet notebooksResult = dbm.getUserNotebooksQuery(userId);

		try {
			// Iterate through notebooks assigned to this user in the database
			// Reconstruct each one into a Notebook object from its stored data
			while (notebooksResult.next()) {
				int notebookId = notebooksResult.getInt(DatabaseManager.NOTEBOOKS_TABLE_NOTEBOOK_ID_COLUMN_NAME);
				int parentId = notebooksResult.getInt(DatabaseManager.NOTEBOOKS_TABLE_PARENT_ID_COLUMN_NAME);
				// If value 'NULL' in database, JDBC conventions will return 0
				// Can check if true value was null using this function below
				boolean parentIdWasNull = notebooksResult.wasNull();
				String title = notebooksResult.getString(DatabaseManager.NOTEBOOKS_TABLE_TITLE_COLUMN_NAME);

				Notebook nb;
				if (parentIdWasNull) {
					nb = new Notebook(new Integer(notebookId), title);
				} else {
					nb = new Notebook(new Integer(notebookId), new Integer(parentId), title);
				}

				userContent.addNotebook(nb);

				nbIdList.add(new Integer(notebookId));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (notebooksResult != null)
					notebooksResult.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		// SQL query for retrieving all notes that match with found notebooks
		ResultSet notesResult;
		if (nbIdList.size() > 0) {
			notesResult = dbm.getUserNotesQuery(userId, nbIdList);
		} else {
			notesResult = null;
		}

		// TODO: can this be moved to database manager or something?
		try {
			if (notesResult == null)
				throw new NoUserNotesException("No notes for this user");
			// Iterate through notes assigned to user's notebooks
			while (notesResult.next()) {
				int noteId = notesResult.getInt(DatabaseManager.NOTES_TABLE_NOTE_ID_COLUMN_NAME);
				int notebookId = notesResult.getInt(DatabaseManager.NOTES_TABLE_NOTEBOOK_ID_COLUMN_NAME);
				String title = notesResult.getString(DatabaseManager.NOTES_TABLE_TITLE_COLUMN_NAME);
				byte[] contentBytes = notesResult.getBytes(DatabaseManager.NOTES_TABLE_CONTENT_COLUMN_NAME);

				// We need to cast the image attachments map bytes back into a HashMap<URL, String>
				ByteArrayInputStream bais = new ByteArrayInputStream(notesResult.getBytes(DatabaseManager.NOTES_TABLE_IMG_ATTACH_MAP_COLUMN_NAME));
				ObjectInputStream ois = new ObjectInputStream(bais);
				HashMap<URL, String> imgAttachMap = (HashMap<URL, String>)ois.readObject();

				Note nn = new Note(new Integer(noteId), new Integer(notebookId), title, contentBytes, imgAttachMap);
				userContent.addNote(nn);
			}
		} catch (NoUserNotesException e) {
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (notesResult != null)
					notesResult.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		ResultSet lastNbIdResult = dbm.getLastNbIdQuery(userId);
		ResultSet lastNoteIdResult = dbm.getLastNoteIdQuery(userId);

		try {
			while (lastNbIdResult.next()) {
				Integer lastNbId = lastNbIdResult.getInt(1);
				// A null value would mean that the user has no previous notebooks
				if (lastNbIdResult.wasNull()) {
					lastNbId = null;
				}
				userContent.setLastNbId(lastNbId);
			}
			while (lastNoteIdResult.next()) {
				Integer lastNoteId = lastNoteIdResult.getInt(1);
				// A null value would mean that the user has no previous notes
				if (lastNoteIdResult.wasNull()) {
					lastNoteId = null;
				}
				userContent.setLastNoteId(lastNoteId);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (lastNbIdResult != null) 
					lastNbIdResult.close();
				if (lastNoteIdResult != null)
					lastNoteIdResult.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		// All notebook and note content is packaged and ready to send to client
		cnm.send(userContent);
	}

	private void addNotebook(Notebook nb) {
		Integer nbId = nb.getId();
		Integer parentId = nb.getParentId();
		String title = nb.getTitle();

		dbm.insertNotebook(nbId, parentId, userId, title);
	}

	private void updateNotebook(Integer id, UpdateType updateField, Object newValue) {
		if (updateField == UpdateType.TITLE) {
			String title = (String)newValue;
			dbm.updateNotebookTitle(id, userId, title);
		} else if (updateField == UpdateType.MAP_NOTEBOOK_TO_PARENT_NOTEBOOK_ID) {
			Integer parentId = (Integer)newValue;
			dbm.updateNotebookParentId(id, userId, parentId);
		}
	}

	private void addNote(Note n) {
		Integer noteId = n.getId();
		Integer nbId = n.getNotebookId();
		String title = n.getTitle();
		Blob content = dbm.generateBlob(n.getContent());
		Blob imgAttachMap = dbm.generateBlob(n.getImageAttachmentsBytes());
			
		dbm.insertNote(noteId, nbId, userId, title, content, imgAttachMap);
	}

	private void updateNote(Integer id, UpdateType updateField, Object newValue) {
		if (updateField == UpdateType.TITLE) {
			String title = (String)newValue;
			dbm.updateNoteTitle(id, userId, title);
		} else if (updateField == UpdateType.MAP_NOTE_TO_NOTEBOOK_ID) {
			Integer notebookId = (Integer)newValue;
			dbm.updateNoteMappedNotebookId(id, userId, notebookId);
		}
	}

	private void updateNoteContent(Integer id, byte[] content) {
		dbm.updateNoteContent(id, userId, dbm.generateBlob(content));
	}

	private void updateNoteImgAttachMap(Integer id, byte[] imgAttachMapBytes) {
		dbm.updateNoteImgAttachMap(id, userId, dbm.generateBlob(imgAttachMapBytes));
	}

	private void deleteNotebook(Integer nbId) {
		// SQL query for retrieving all notes that match with this notebook
		ResultSet notesResult = dbm.getNoteIDsInNotebookQuery(userId, nbId);

		// Iterate through all notes and delete from the database
		try {
			while (notesResult.next()) {
				int noteId = notesResult.getInt(1);
				deleteNote(noteId);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (notesResult != null)
					notesResult.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// SQL query for retrieving all child notebooks of this notebook
		ResultSet childrenResult = dbm.getChildNbIdsQuery(userId, nbId);

		// Iterate through children notebooks and delete them as well
		try {
			while (childrenResult.next()) {
				int childId = childrenResult.getInt(1);
				deleteNotebook(childId);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (childrenResult != null)
					childrenResult.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// Finally, delete the notebook itself
		dbm.deleteNotebook(nbId, userId);
	}

	private void deleteNote(Integer id) {
		dbm.deleteNote(id, userId);
	}

	public int getUserId() {
		return userId;
	}

	public String getUsername() {
		return username;
	}

	public boolean isAuthorized() {
		return isAuthorized;
	}

	public void disconnect() {
		if (isAuthorized())
			server.removeSession(username);
		dbm.closeConnection();
		cnm.closeConnection();
		server.removeClient(this);
	}
}

class NoUserNotesException extends Exception {
	public NoUserNotesException(String message) {
		super(message);
	}
}