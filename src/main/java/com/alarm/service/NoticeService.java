package com.alarm.service;

import java.util.List;

import com.alarm.domain.Notice;

public interface NoticeService {

	List<Notice> findAllNotice();
	
	//3개 묶음 , Receiver_id로 찾는데, 최근 10개, 이전 10개씩 찾자.
	List<Notice> findAllNoticeByReceiverId(String receiver_id);
    List<Notice> findLatestNoticeByReceiverId(String receiver_id);	
    List<Notice> findPreviousNoticeByReceiverId(String receiver_id, int id);
    //
    
	Notice saveNotice(Notice notice);
	
	void Cacherefresh();
	

}
