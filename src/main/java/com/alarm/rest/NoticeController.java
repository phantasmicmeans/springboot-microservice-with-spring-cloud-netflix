package com.alarm.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.alarm.domain.Notice;
import com.alarm.service.NoticeService;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;

@RestController
//@Cacheable(value="usercache")
public class NoticeController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private NoticeService noticeService;
	private List<Notice> Temp;
	
	
	@HystrixCommand(fallbackMethod = "fallbacknotice",
			commandProperties = {
					@HystrixProperty(name="execution.isolation.strategy", value="SEMAPHORE")
			}
	)
	@RequestMapping(value = "/notice", method=RequestMethod.GET)
	public ResponseEntity<List<Notice>> getAllNotice(){
		
		long start = System.currentTimeMillis();
		final List<Notice> allMembers = noticeService.findAllNotice();
		
		if (allMembers.isEmpty()) {
			return new ResponseEntity<List<Notice>>(HttpStatus.NO_CONTENT);
		}
		
		long end=System.currentTimeMillis();
		
		logger.info("수행시간: " + (end-start));
		
		return new ResponseEntity<List<Notice>>(allMembers, HttpStatus.OK);
	}	

	//@Cacheable(value="usercache")
	public ResponseEntity<List<Notice>> fallbacknotice(){
		//cacheing data 사용 
		
		List<Notice> noti = new ArrayList<>();
		
		IntStream.range(1,10).forEachOrdered( n -> {
			
			Notice notice = new Notice(1,"TEST","TEST",3);
			noti.add(notice);
			
		});
		
		
		return new ResponseEntity<List<Notice>>(noti, HttpStatus.OK);
		/*
		long start = System.currentTimeMillis();
		final List<Notice> allnotice = noticeService.findAllNotice();
		
		if (allnotice.isEmpty()) {
			return new ResponseEntity<List<Notice>>(HttpStatus.NO_CONTENT);
		}
		
		long end=System.currentTimeMillis();
		
		logger.info("수행시간: " + (end-start));
		
		
		return new ResponseEntity<List<Notice>>(allnotice, HttpStatus.OK);*/
		
	}
	
	//receiver_id를 인자로 전체 notice 출
	@HystrixCommand(fallbackMethod = "fallbackReceiverNotice",
	commandProperties = {
			@HystrixProperty(name="execution.isolation.strategy", value="SEMAPHORE")
	}
)
	@RequestMapping(value="/notice/{receiver_id}", method = RequestMethod.GET)
	public ResponseEntity<List<Notice>> getAllNoticeByReceiverId(@PathVariable("receiver_id") final String receiver_id)
	{
		long start = System.currentTimeMillis();
		
		final List<Notice> selectedNotice = noticeService.findAllNoticeByReceiverId(receiver_id);
		Temp = noticeService.findAllNoticeByReceiverId(receiver_id);
		if (selectedNotice.isEmpty())
		{
			
			logger.info("404 Not Found"); ;
			return new ResponseEntity<List<Notice>>(HttpStatus.NOT_FOUND);
		}
		long end=System.currentTimeMillis();
		
		logger.info("수행시간: " + (end-start));
		return new ResponseEntity<List<Notice>>(selectedNotice, HttpStatus.OK);

	}
	
	//@Cacheable(value="usercache")
	public ResponseEntity<List<Notice>> fallbackReceiverNotice(@PathVariable("receiver_id") final String receiver_id)
	{
		/*
		List<Notice>temp= new ArrayList<>();
		
		Notice n = new Notice(6,"ddd","fdfdf",3);
		temp.add(n);
		
		if(Temp.isEmpty())
		{
			logger.info("sdfsdfsdfsdsfd");
		}
	
		return new ResponseEntity<List<Notice>>(Temp,HttpStatus.OK);
	}*/
		long start = System.currentTimeMillis();

		final List<Notice> selectedNotice = noticeService.findAllNoticeByReceiverId(receiver_id);
		
		if (selectedNotice.isEmpty())
		{
			
			logger.info("404 Not Found"); ;
			return new ResponseEntity<List<Notice>>(HttpStatus.NOT_FOUND);
		}
		long end=System.currentTimeMillis();
		
		logger.info("수행시간: " + (end-start));
		
		return new ResponseEntity<List<Notice>>(Temp, HttpStatus.OK);	
		}
		

	//receiver_id를 인자로 최근 10개 notice 출

	@HystrixCommand(fallbackMethod = "fallbackLatestReceiverNotice",
	commandProperties = {
			@HystrixProperty(name="execution.isolation.strategy", value="SEMAPHORE")
	}
)
	@RequestMapping(value="/notice/latest/{receiver_id}",method = RequestMethod.GET)
	public ResponseEntity<List<Notice>> getLastNoticeByReceiverId(@PathVariable("receiver_id") final String receiver_id)
	{
		long start = System.currentTimeMillis();

		final List<Notice> LastNotice = noticeService.findLatestNoticeByReceiverId(receiver_id);
		
		if(LastNotice.isEmpty())
		{
			logger.info("GetLastNoriceByReceiverId, 404 Not Found");
			return new ResponseEntity<List<Notice>>(HttpStatus.NOT_FOUND);
			
		}
		long end=System.currentTimeMillis();
		
		logger.info("수행시간: " + (end-start));
		return new ResponseEntity<List<Notice>>(LastNotice , HttpStatus.OK);
	}
	
	//@Cacheable(value="usercache")
	public ResponseEntity<List<Notice>> fallbackLatestReceiverNotice(@PathVariable("receiver_id") final String receiver_id)
	{
		long start = System.currentTimeMillis();

		final List<Notice> LastNotice = noticeService.findLatestNoticeByReceiverId(receiver_id);
		
		if(LastNotice.isEmpty())
		{
			logger.info("GetLastNoriceByReceiverId, 404 Not Found");
			return new ResponseEntity<List<Notice>>(HttpStatus.NOT_FOUND);
			
		}
		long end=System.currentTimeMillis();
		
		logger.info("수행시간: " + (end-start));
		return new ResponseEntity<List<Notice>>(LastNotice , HttpStatus.OK);
	}
	
	
	//receiver_id와 현재 index를 인자로 이 10개 notice 출력
	@HystrixCommand(fallbackMethod = "fallbackPreviousReceiverNotice",
	commandProperties = {
			@HystrixProperty(name="execution.isolation.strategy", value="SEMAPHORE")
	}
)
	@RequestMapping(value="/notice/previous/{receiver_id}/{id}",method = RequestMethod.GET)
	public ResponseEntity<List<Notice>> getPreviousNoticeByReceiverId(@PathVariable("receiver_id") final String receiver_id, @PathVariable("id") final int id)
	{
		long start = System.currentTimeMillis();

		final List<Notice> PreviousNotice = noticeService.findPreviousNoticeByReceiverId(receiver_id,id);
		
		if(PreviousNotice.isEmpty())
		{
			logger.info("GetPreviousNoticeByReceiverId, 404 Not Found");
			return new ResponseEntity<List<Notice>>(HttpStatus.NOT_FOUND);
			
		}
		long end=System.currentTimeMillis();
		
		logger.info("수행시간: " + (end-start));
		return new ResponseEntity<List<Notice>>(PreviousNotice , HttpStatus.OK);
	}
	
	@Cacheable(value="usercache")
	public ResponseEntity<List<Notice>> fallbackPreviousReceiverNotice(@PathVariable("receiver_id") final String receiver_id, @PathVariable("id") final int id)
	{
		long start = System.currentTimeMillis();

		final List<Notice> PreviousNotice = noticeService.findPreviousNoticeByReceiverId(receiver_id,id);
		
		if(PreviousNotice.isEmpty())
		{
			logger.info("GetPreviousNoticeByReceiverId, 404 Not Found");
			return new ResponseEntity<List<Notice>>(HttpStatus.NOT_FOUND);
			
		}
		long end=System.currentTimeMillis();
		
		logger.info("수행시간: " + (end-start));
		return new ResponseEntity<List<Notice>>(PreviousNotice , HttpStatus.OK);
	}
	
	//얘는 Notice 객체 그대로 저장 
	@RequestMapping(value="/notice", method = RequestMethod.POST)
	public ResponseEntity<Void> createNotice(@RequestBody final Notice notice, final UriComponentsBuilder ucBuilder){
		
		
		final Notice savedNotice = noticeService.saveNotice(notice); 
		final HttpHeaders headers = new HttpHeaders();
		headers.setLocation(ucBuilder.path("/notice/{receiver_id}").buildAndExpand(savedNotice.getReceiver_id()).toUri());
	
		return new ResponseEntity<Void>(headers, HttpStatus.CREATED);
	
		
	}
	
}























