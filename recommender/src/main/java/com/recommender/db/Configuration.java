package com.recommender.db;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Configuration
{

	public enum CONFIGURATION_TYPE
	{
		ASSIGNEE_EMAIL_TEMPLATE,
		ASSIGNEE_EMAIL_SUBJECT,
		ASSIGNEE_EMAIL_CC,
		PREFERENCE_PRIORITIES
	}

	@Id
	@GeneratedValue
	private Integer id;
	private CONFIGURATION_TYPE configurationType;
	private String content;

	public Integer getId()
	{
		return id;
	}

	public void setId(Integer id)
	{
		this.id = id;
	}

	public CONFIGURATION_TYPE getConfigurationType()
	{
		return configurationType;
	}

	public void setConfigurationType(CONFIGURATION_TYPE configurationType)
	{
		this.configurationType = configurationType;
	}

	public String getContent()
	{
		return content;
	}

	public void setContent(String content)
	{
		this.content = content;
	}

}