package com.openatk.rockapp.models;

import java.util.Date;

public class RockData {
	
	public int id;
	public String remoteId;
	public boolean deleted;
	public Date deletedChanged;
	public double lat;
	public double lon;
	public Date posChanged;
	
	public boolean picked;
	public Date pickedChanged;
	
	public String comments;
	public Date commentsChanged;
	
	public String picture;
	public Date pictureChanged;
	public String pictureURL;
	public String pictureRemoteId;
	
	public RockData(){
		this.id = Rock.BLANK_ROCK_ID;
		this.remoteId = "";
		this.deleted = false;
		this.deletedChanged = null;
		this.lat = 0.0d;
		this.lon = 0.0d;
		this.posChanged = null;
		this.picked = false;
		this.pickedChanged = null;
		this.comments = "";
		this.commentsChanged = null;
		this.picture = null;
		this.pickedChanged = null;
		this.pictureURL = null;
		this.pictureRemoteId = null;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getRemoteId() {
		return remoteId;
	}

	public void setRemoteId(String remoteId) {
		this.remoteId = remoteId;
	}

	public boolean isDeleted() {
		return deleted;
	}
	public Date getDeletedChanged(){
		return this.deletedChanged;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	public void setDeletedChanged(Date deletedChanged) {
		this.deletedChanged = deletedChanged;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public Date getPosChanged() {
		return posChanged;
	}

	public void setPosChanged(Date posChanged) {
		this.posChanged = posChanged;
	}

	public boolean isPicked() {
		return picked;
	}

	public void setPicked(boolean picked) {
		this.picked = picked;
	}

	public Date getPickedChanged() {
		return pickedChanged;
	}

	public void setPickedChanged(Date pickedChanged) {
		this.pickedChanged = pickedChanged;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public Date getCommentsChanged() {
		return commentsChanged;
	}

	public void setCommentsChanged(Date commentsChanged) {
		this.commentsChanged = commentsChanged;
	}

	public String getPicture() {
		return picture;
	}

	public void setPicture(String picture) {
		this.picture = picture;
	}

	public Date getPictureChanged() {
		return pictureChanged;
	}

	public void setPictureChanged(Date pictureChanged) {
		this.pictureChanged = pictureChanged;
	}

	public String getPictureURL() {
		return pictureURL;
	}

	public void setPictureURL(String pictureURL) {
		this.pictureURL = pictureURL;
	}

	public String getPictureRemoteId() {
		return pictureRemoteId;
	}

	public void setPictureRemoteId(String pictureRemoteId) {
		this.pictureRemoteId = pictureRemoteId;
	}
}
