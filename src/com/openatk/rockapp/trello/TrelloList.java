package com.openatk.rockapp.trello;

import java.util.Date;

public class TrelloList extends TrelloObject {
	String id;
	String localId;
	String boardId;
	Date boardId_changed = new Date(0);

	String name;
	Date name_changed = new Date(0);

	Boolean closed = false;
	Date closed_changed = new Date(0);
	
	Integer pos;
	Date pos_changed = new Date(0);

	
	public static final String COL_ID = "id";
	public static final String COL_LOCAL_ID = "local_id";
	
	public static final String COL_BOARD_ID = "board_id";
	public static final String COL_BOARD_ID_CHANGED = "board_id_changed";

	public static final String COL_NAME = "name";
	public static final String COL_NAME_CHANGED = "name_changed";

	public static final String COL_CLOSED = "closed";
	public static final String COL_CLOSED_CHANGED = "closed_changed";

	
	public static final String COL_POS = "pos";
	public static final String COL_POS_CHANGED = "pos_changed";

	
	public TrelloList(){
		
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getLocalId() {
		return localId;
	}

	public void setLocalId(String localId) {
		this.localId = localId;
	}

	public String getBoardId() {
		return boardId;
	}

	public void setBoardId(String boardId) {
		this.boardId = boardId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getClosed() {
		return closed;
	}

	public void setClosed(Boolean closed) {
		this.closed = closed;
	}

	public Integer getPos() {
		return pos;
	}

	public void setPos(Integer pos) {
		this.pos = pos;
	}

	public Date getBoardId_changed() {
		return boardId_changed;
	}

	public void setBoardId_changed(Date boardId_changed) {
		this.boardId_changed = boardId_changed;
	}

	public Date getName_changed() {
		return name_changed;
	}

	public void setName_changed(Date name_changed) {
		this.name_changed = name_changed;
	}

	public Date getPos_changed() {
		return pos_changed;
	}

	public void setPos_changed(Date pos_changed) {
		this.pos_changed = pos_changed;
	}

	public Date getClosed_changed() {
		return closed_changed;
	}

	public void setClosed_changed(Date closed_changed) {
		this.closed_changed = closed_changed;
	}
	
	
	
}
