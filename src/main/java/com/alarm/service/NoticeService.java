package com.alarm.service;

import java.util.List;

import com.alarm.domain.Notice;

public interface NoticeService {

	List<Notice> findByReceiverId(String receiver_id);
    List<Notice> findAllNotice();	
	Notice saveNotice(Notice notice);

}
