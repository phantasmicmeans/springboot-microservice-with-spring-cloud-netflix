package com.alarm.repository;


import org.springframework.data.repository.CrudRepository;

import com.alarm.domain.Notice;


public interface NoticeRepository extends CrudRepository<Notice, String>{
	
	
}
