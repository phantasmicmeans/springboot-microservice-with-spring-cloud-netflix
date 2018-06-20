package com.alarm.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.alarm.domain.Notice;
import com.alarm.repository.NoticeRepository;
import com.google.common.collect.Lists;

@Service("noticeService")
public class NoticeServiceImpl implements NoticeService{

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	

	@Autowired
	private NoticeRepository noticeRepository;
	
	@Override
	public List<Notice> findAllNotice()
	{
		
		Optional<Iterable<Notice>> maybeNoticeIter = Optional.ofNullable(noticeRepository.findAll());
		
		return Lists.newArrayList(maybeNoticeIter.get());

		
	}
	
	@Override
	public List<Notice> findAllNoticeByReceiverId(String receiver_id)
	{
	    
		Optional<List<Notice>> maybeNotice = Optional.ofNullable(noticeRepository.findNoticeByReceiverId(receiver_id));
		
		return maybeNotice.get();

	}
	

	@Override
    public List<Notice> findLatestNoticeByReceiverId(String receiver_id)
    {
		Optional<List<Notice>> maybeLatestNotice = Optional.ofNullable(noticeRepository.findLatestNoticeByReceiverId(receiver_id, PageRequest.of(0, 10)));
		
		return maybeLatestNotice.get();

	
	}
	
	@Override
	public List<Notice> findPreviousNoticeByReceiverId(String receiver_id, int id)
	{
		
		Optional<List<Notice>> maybePreviousNotice = Optional.ofNullable(noticeRepository.findPreviousNoticeByReceiverId(receiver_id, id, PageRequest.of(0, 10)));
		
		return maybePreviousNotice.get();

	}
	
    @Override
	public Boolean saveNotice(Notice notice)
	{
    	try {
    		
    		Optional<Notice> maybeNotice = Optional.of(notice);
    		noticeRepository.save(maybeNotice.get());
    		logger.info("Saved");
    		return true;
    		
    	}catch(Exception e)
    	{
    		logger.info("Nothing to save");
    		return false;
    	}
    	

	}

	

	
}




















