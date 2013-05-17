package edu.purdue.autogenics.rockapp.trello;

import edu.purdue.autogenics.libtrello.IBoard;

public class RockBoard implements IBoard {

	private String id;
	private String localId;
	private String name;
	private String desc;
	private Boolean closed;
	private Boolean changed;
	
	public RockBoard(String id, String localId, String name, String desc,
			Boolean closed, Boolean changed) {
		super();
		this.id = id;
		this.localId = localId;
		this.name = name;
		this.desc = desc;
		this.closed = closed;
		this.changed = changed;
	}

	@Override
	public String getTrelloId() {
		// TODO Auto-generated method stub
		return id;
	}

	@Override
	public Object getLocalId() {
		// TODO Auto-generated method stub
		return localId;
	}

	@Override
	public Boolean getClosed() {
		// TODO Auto-generated method stub
		return closed;
	}

	@Override
	public Boolean hasLocalChanges() {
		// TODO Auto-generated method stub
		return changed;
	}

	@Override
	public void setLocalChanges(Boolean changes) {
		this.changed = changes;	
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDesc() {
		return desc;
	}

}
