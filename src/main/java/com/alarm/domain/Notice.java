package com.alarm.domain;

import javax.persistence.Entity;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Notice {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	private String receiver_id;
	private String sender_id;
	private int article_id;
	
	protected Notice() {}
	
	public Notice(final int id, final String receiver_id, final String sender_id,final int article_id)
	{
		this.receiver_id=receiver_id;
		this.sender_id=sender_id;
		this.article_id=article_id;
	}
	
	public int getId()
	{
		return id;
	}

	public String getReceiver_id()
	{
		return receiver_id;
	}
	public String getSender_id()
	{
		return sender_id;
	}
	
	public void setReceiver_id(String receiver_id)
	{
		this.receiver_id=receiver_id;
	}
	
	public void setSender_id(String sender_id)
	{
		this.sender_id=sender_id;
	}
	
	public int getArticle_id()
	{
		return article_id;
	}
	public void setArticle_id(int article_id)
	{
		this.article_id=article_id;
	}
	@Override
	public String toString()
	{
		return String.format("Notice [id = %d ,receiver_id = '%s', sender_id = '%s', article_id = %d] ", id, receiver_id, sender_id, article_id);
		
	}

}
