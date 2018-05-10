package com.alarm.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.alarm.domain.Notice;
import com.alarm.repository.NoticeRepository;
import com.alarm.cache.*;

@Service("noticeService")
public class NoticeServiceImpl implements NoticeService{

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	
	public CacheManager cachemanager;
	
	private List<Notice> result; 
	//private List<String> receiver_id_str = new ArrayList<>();
	
	@Autowired
	private NoticeRepository noticeRepository;
	
	@Override
	//@CachePut(value="usercache")
	public List<Notice> findAllNotice()
	{
		List<Notice> AllNotice = new ArrayList<>();

		Iterable<Notice> allNoticeIter = noticeRepository.findAll();
		
		Consumer<Notice> noticeList = (notice) -> {
			AllNotice.add(notice);
		};
		
		allNoticeIter.forEach(noticeList);
		
		return AllNotice;
	}
	
	//@CachePut(value="usercache")
	@Override
	public List<Notice> findAllNoticeByReceiverId(String receiver_id)
	{
	    
		result = new ArrayList<>();
		
		Iterable<Notice>notiIter = noticeRepository.findAll();
		
		notiIter.forEach(result::add);
		List<Notice> matchingResult = result.stream()
											.filter(receiver-> receiver.getReceiver_id()
											.equals(receiver_id))
											.collect(Collectors.toList());
		
		
		
		//notiIter.forEach(noticeList);
		
		
		return matchingResult;
	}
	

	//@CachePut(value="usercache")
	@Override
	//@Cacheable(value="usercache")
    public List<Notice> findLatestNoticeByReceiverId(String receiver_id)
    {

	
		return noticeRepository.findLatestNoticeByReceiverId(receiver_id,PageRequest.of(0, 10));
	
		/*
		allNoticeIter.forEach(allMemberList::add) 이거랑 위랑 같은코드 
		처음에 누르면 
		1. 그 정보를 가져오고, 가져옴과 동시에 캐싱해 놓는다.
		2. 또 누르면 캐싱된 정보를 가져온다? But => 그 사이에 새로운게 입력되면? -> 이건 아닌듯
		3. 새로운게 있다면 다시 가져와야 하므로 계속 디비 접근하게 놔두고.
		4. 서비스가 끊긴다면 캐싱해논 데이터를 가져다 쓰게끔 만드는게 좋을듯
		
		*/
	}
	
	//@CachePut(value="usercache")
	@Override
	//@Cacheable(value="usercache")
	public List<Notice> findPreviousNoticeByReceiverId(String receiver_id, int id)
	{
		
		
		return noticeRepository.findPreviousNoticeByReceiverId(receiver_id, id,PageRequest.of(0, 10));
		/*
		List<Notice> AllNotice = new ArrayList<>();
		

		Iterable<Notice> allNoticeIter = noticeRepository.findAll();
		
		Consumer<Notice> noticeList = (notice) -> {
			AllNotice.add(notice);
		};
		
		allNoticeIter.forEach(noticeList);
		
		return AllNotice;
		*/
	}
	/*
	@Override
	@Cacheable(value="BBScache")
	public List<Notice> findAllNoticeByCache()
	{
		
		
	}*/
	
    @Override
    @CachePut(value="usercache")
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
    
    
    
    @Override
    @CacheEvict(cacheNames="usercache", allEntries=true)
    public void Cacherefresh()
    {
    	logger.info("BBSCache Clear");
    }
   
	

	
}




















