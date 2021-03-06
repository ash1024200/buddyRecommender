package com.recommender.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.recommender.db.Configuration;
import com.recommender.db.Configuration.CONFIGURATION_TYPE;
import com.recommender.db.Employee;
import com.recommender.frontend.Email;
import com.recommender.repositories.ConfigurationRepository;

/**
 * Contains code dealing with configuration - email templates, preferences etc.
 * 
 * @author ashwinvinod
 *
 */
@Service
public class ConfigurationService
{
	private static final String CC = "cc";
	private static final String SUBJECT = "subject";
	private static final String TO_ADDRESS = "toAddress";
	private static final String EMAIL_CONTENT = "emailContent";
	private static final String JOINEE_NAME = "$joineeName";
	private static final String ASSIGNEE_NAME = "$assigneeName";

	@Autowired
	private ConfigurationRepository configurationRepository;
	@Autowired
	private EmployeeService employeeService;

	/**
	 * Saves the provided assignee email template details.
	 * 
	 * @param cc
	 *            Default CC email address
	 * @param subject
	 *            Default email subject
	 * @param mailContent
	 *            Mail content template
	 */
	public void saveAssigneeTemplate(String cc, String subject, String mailContent)
	{
		Configuration assigneeTemplate = configurationRepository
				.findOneByConfigurationType(CONFIGURATION_TYPE.ASSIGNEE_EMAIL_TEMPLATE);
		if (assigneeTemplate == null)
		{
			assigneeTemplate = new Configuration();
		}
		assigneeTemplate.setConfigurationType(CONFIGURATION_TYPE.ASSIGNEE_EMAIL_TEMPLATE);
		assigneeTemplate.setContent(mailContent);
		configurationRepository.save(assigneeTemplate);

		Configuration assigneeSubject = configurationRepository
				.findOneByConfigurationType(CONFIGURATION_TYPE.ASSIGNEE_EMAIL_SUBJECT);
		if (assigneeSubject == null)
		{
			assigneeSubject = new Configuration();
		}
		assigneeSubject.setConfigurationType(CONFIGURATION_TYPE.ASSIGNEE_EMAIL_SUBJECT);
		assigneeSubject.setContent(subject);
		configurationRepository.save(assigneeSubject);

		Configuration assigneeCC = configurationRepository.findOneByConfigurationType(CONFIGURATION_TYPE.ASSIGNEE_EMAIL_CC);
		if (assigneeCC == null)
		{
			assigneeCC = new Configuration();
		}
		assigneeCC.setConfigurationType(CONFIGURATION_TYPE.ASSIGNEE_EMAIL_CC);
		assigneeCC.setContent(cc);
		configurationRepository.save(assigneeCC);
	}

	/**
	 * Creates a json object containing information required to send an email to
	 * the assigned employee, including email content, to address, cc and
	 * subject.
	 * 
	 * @param employeeId
	 * @param joineeName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public JSONObject getAssigneeEmailData(String employeeId, String joineeName)
	{
		Employee assignee = employeeService.findEmployeeById(employeeId);

		Email email = getEmailDataFromTemplate();

		Map<String, String> placeholderName2Values = new HashMap<String, String>();
		placeholderName2Values.put(ASSIGNEE_NAME, assignee.getName());
		placeholderName2Values.put(JOINEE_NAME, joineeName);
		String emailContent = resolveTemplatePlaceholders(email.getMailContent(), placeholderName2Values);
		JSONObject emailData = new JSONObject();
		emailData.put(EMAIL_CONTENT, emailContent);
		emailData.put(TO_ADDRESS, assignee.getEmailAddress());
		emailData.put(SUBJECT, email.getSubject());
		emailData.put(CC, email.getCc());
		return emailData;
	}

	@SuppressWarnings("unchecked")
	public JSONObject getAssigneeEmailTemplate()
	{
		Email email = getEmailDataFromTemplate();

		JSONObject emailData = new JSONObject();
		emailData.put(EMAIL_CONTENT, email.getMailContent());
		emailData.put(SUBJECT, email.getSubject());
		emailData.put(CC, email.getCc());
		return emailData;
	}

	private Email getEmailDataFromTemplate()
	{
		String cc = "";
		String subject = "";
		String template = "";
		Configuration assigneeTemplate = configurationRepository
				.findOneByConfigurationType(CONFIGURATION_TYPE.ASSIGNEE_EMAIL_TEMPLATE);
		Configuration assigneeSubject = configurationRepository
				.findOneByConfigurationType(CONFIGURATION_TYPE.ASSIGNEE_EMAIL_SUBJECT);
		Configuration assigneeCC = configurationRepository.findOneByConfigurationType(CONFIGURATION_TYPE.ASSIGNEE_EMAIL_CC);

		if (assigneeTemplate != null && assigneeSubject != null && assigneeCC != null)
		{
			cc = assigneeCC.getContent();
			subject = assigneeSubject.getContent();
			template = assigneeTemplate.getContent();
		}
		if (StringUtils.isBlank(template))
		{
			template = "Hi " + ASSIGNEE_NAME + ", You have been selected to be a buddy for " + JOINEE_NAME + ". Thanks, HR";
		}
		Email email = new Email("", "", cc, subject, template);

		return email;
	}

	private String resolveTemplatePlaceholders(String template, Map<String, String> placeholderName2Values)
	{
		String result = new String(template);
		for (Entry<String, String> key2value : placeholderName2Values.entrySet())
		{
			result = result.replace(key2value.getKey(), key2value.getValue());
		}
		return result;
	}
}