package com.alarm.service;

import java.util.List;

import com.alarm.domain.Notice;

public interface NoticeService {

	List<Notice> findAllNotice();
	List<Notice> findAllNoticeByReceiverId(String receiver_id);
    List<Notice> findLatestNoticeByReceiverId(String receiver_id);	
    List<Notice> findPreviousNoticeByReceiverId(String receiver_id, int id);
	Boolean saveNotice(Notice notice);
	
}
