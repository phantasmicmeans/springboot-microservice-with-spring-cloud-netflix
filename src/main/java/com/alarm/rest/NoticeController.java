package com.alarm.rest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

@RestController
public class NoticeController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private NoticeService noticeService;

	@RequestMapping(value = "/notice", method=RequestMethod.GET)
	public ResponseEntity<List<Notice>> listAllMembers(){
		
		final List<Notice> allMembers = noticeService.findAllNotice();
		
		if (allMembers.isEmpty()) {
			return new ResponseEntity<List<Notice>>(HttpStatus.NO_CONTENT);
		}
		
		return new ResponseEntity<List<Notice>>(allMembers, HttpStatus.OK);
	}	
	@RequestMapping(value="/notice/{receiver_id}", method = RequestMethod.GET)
	public ResponseEntity<List<Notice>> getSelectNotice(@PathVariable("receiver_id") final String receiver_id)
	{
		final List<Notice> selectedNotice = noticeService.findByReceiverId(receiver_id);
		
		if (selectedNotice.isEmpty())
		{
			
			logger.info("404 Not Found"); ;
			return new ResponseEntity<List<Notice>>(HttpStatus.NOT_FOUND);
		}
		
		return new ResponseEntity<List<Notice>>(selectedNotice, HttpStatus.OK);

	}
	
	@RequestMapping(value="/notice", method = RequestMethod.POST)
	public ResponseEntity<Void> createNotice(@RequestBody final Notice notice, final UriComponentsBuilder ucBuilder){
		
		//제약조건 없음 그냥 쏴주기만 하면 됨.
		//쏘는게 null인지만 체크하자. request body에 notice객체 보냄.
		
		final Notice savedNotice = noticeService.saveNotice(notice); 
		final HttpHeaders headers = new HttpHeaders();
		headers.setLocation(ucBuilder.path("/notice/{receiver_id}").buildAndExpand(savedNotice.getReceiver_id()).toUri());
	
		return new ResponseEntity<Void>(headers, HttpStatus.CREATED);
	
		
	}
	
}























