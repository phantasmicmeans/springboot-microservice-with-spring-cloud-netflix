package com.alarm.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alarm.domain.Notice;
import com.alarm.repository.NoticeRepository;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Service("noticeService")
public class NoticeServiceImpl implements NoticeService{

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private List<Notice> result; 
	//private List<String> receiver_id_str = new ArrayList<>();
	
	@Autowired
	private NoticeRepository noticeRepository;
	
	@Override
	public List<Notice> findByReceiverId(String receiver_id)
	{
	    
		result = new ArrayList<>();
		
		Iterable<Notice>notiIter = noticeRepository.findAll();
		
		notiIter.forEach(result::add);
		List<Notice> matchingResult = result.stream()
											.filter(receiver-> receiver.getReceiver_id()
											.equals(receiver_id))
											.collect(Collectors.toList());
		
		
		
		/*
		result = new ArrayList<Notice>();
		receiver_id_str.add(receiver_id);
		Iterable<String> received_id = receiver_id_str.subList(0, 1);	
		Iterable<Notice> notiIter = noticeRepository.findAllById(received_id);
		Consumer<Notice> noticeList = (notice) -> {
			logger.info("DB Receiver id "+ notice.getReceiver_id());
			result.add(notice);
		};
		
		
		receiver_id_str.remove(receiver_id);
		return result;
		
		*/
		
		//notiIter.forEach(noticeList);
		
		
		return matchingResult;
	}
	
	@Override
    public List<Notice> findAllNotice()
    {
		List<Notice> AllNotice = new ArrayList<>();

		Iterable<Notice> allNoticeIter = noticeRepository.findAll();
		
		Consumer<Notice> noticeList = (notice) -> {
			AllNotice.add(notice);
		};
		
		allNoticeIter.forEach(noticeList);
		
		/*
		allNoticeIter.forEach(allMemberList::add); //이거랑 위랑 같은코드 
		*/
		return AllNotice;		
	}
    @Override
	public Notice saveNotice(Notice notice)
	{
    	Optional <Notice> maybeNotice = Optional.ofNullable(notice); //비어있는지 모름.
    	/*
    	logger.info("Re id :" +maybeNotice.get().getReceiver_id());
    	logger.info("Se id :" +maybeNotice.get().getSender_id());
    	logger.info("Ar id :" +maybeNotice.get().getArticle_id());
    	logger.info(maybeNotice.get().toString());
    	*/
		return noticeRepository.save(maybeNotice.get()); //비어있으면 던짐 
	}
	

	
}




















