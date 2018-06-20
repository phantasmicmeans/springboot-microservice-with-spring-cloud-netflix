package com.alarm.rest;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.bind.annotation.CrossOrigin;
import com.alarm.domain.Notice;
import com.alarm.service.NoticeService;


@RestController
@CrossOrigin(origins="*")
public class NoticeController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
    public static List<Notice> Temp;

	@Autowired
	private NoticeService noticeService;

	@RequestMapping(value = "/notice", method=RequestMethod.GET)
	public ResponseEntity<List<Notice>> getAllNotice(){
    
        try{

		    Optional<List<Notice>> maybeAllStory = Optional.ofNullable(noticeService.findAllNotice());
		
		    return new ResponseEntity<List<Notice>>(maybeAllStory.get(), HttpStatus.OK);

        }catch(Exception e)
        {
 			return new ResponseEntity<List<Notice>>(HttpStatus.NOT_FOUND);
        }
	
	}

	@RequestMapping(value="/notice/{receiver_id}", method = RequestMethod.GET)
	public ResponseEntity<List<Notice>> getAllNoticeByReceiverId(@PathVariable("receiver_id") final String receiver_id)
	{

		try {
			
			Optional<List<Notice>> maybeSelectedNotice = Optional.of(noticeService.findAllNoticeByReceiverId(receiver_id));
		
			return new ResponseEntity<List<Notice>>(maybeSelectedNotice.get(), HttpStatus.OK);
			
		}catch(Exception e)
		{
			return new ResponseEntity<List<Notice>>(HttpStatus.NOT_FOUND);
		}
	

	}
	
	@RequestMapping(value="/notice/latest/{receiver_id}",method = RequestMethod.GET)
	public ResponseEntity<List<Notice>> getLastNoticeByReceiverId(@PathVariable("receiver_id") final String receiver_id)
	{

		try {
			
			Optional<List<Notice>>maybeLastsNotice = Optional.of(noticeService.findLatestNoticeByReceiverId(receiver_id));
			
			return new ResponseEntity<List<Notice>>(maybeLastsNotice.get() , HttpStatus.OK);
			
		}catch(Exception e)
		{
			return new ResponseEntity<List<Notice>>(HttpStatus.NOT_FOUND);

		}
	
	}
    
    @RequestMapping(value="/notice/previous/{receiver_id}/{id}",method = RequestMethod.GET)
	public ResponseEntity<List<Notice>> getPreviousNoticeByReceiverId(@PathVariable("receiver_id") final String receiver_id, @PathVariable("id") final int id)
	{
		
		try {		
			
			Optional<List<Notice>> maybePreviousNotice = Optional.of(noticeService.findPreviousNoticeByReceiverId(receiver_id, id));
			
			return new ResponseEntity<List<Notice>>(maybePreviousNotice.get(), HttpStatus.OK);
			
		}catch(Exception e)
		{
			return new ResponseEntity<List<Notice>>(HttpStatus.NOT_FOUND);
		}
	
	}
	
	
	@RequestMapping(value="/notice", method = RequestMethod.POST)
	public ResponseEntity<Void> createNotice(@RequestBody final Notice notice, final UriComponentsBuilder ucBuilder){
		
		if(!noticeService.saveNotice(notice)) { return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST); }
		
		return new ResponseEntity<Void>(HttpStatus.CREATED);

	}
	
}























