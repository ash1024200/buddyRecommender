package com.recommender.services;

import org.springframework.beans.factory.annotation.Autowired;
import com.recommender.db.Joinee;
import com.recommender.repositories.JoineeRepository;

public class JoineeService
{

	@Autowired
	private JoineeRepository joineeRepository;
	
	public Joinee saveJoinee(Joinee joinee)
	{
		return joineeRepository.save(joinee);
	}
}
