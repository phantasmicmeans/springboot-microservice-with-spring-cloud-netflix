package com.alarm.repository;


import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.alarm.domain.Notice;


public interface NoticeRepository extends CrudRepository<Notice, String>{
	
	
	@Query("SELECT n FROM Notice n WHERE receiver_id=:receiver_id ORDER BY id DESC")
	List<Notice> findNoticeByReceiverId(@Param("receiver_id") String receiver_id);
	
	@Query("SELECT n FROM Notice n WHERE receiver_id=:receiver_id ORDER BY id DESC")
	List<Notice> findLatestNoticeByReceiverId(@Param("receiver_id") String receiver_id, Pageable pageable);
	
	@Query("SELECT n FROM Notice n WHERE n.receiver_id=:receiver_id AND n.id < :id ORDER BY n.id DESC")
	List<Notice> findPreviousNoticeByReceiverId(@Param("receiver_id")String receiver_id, @Param("id") int id, Pageable pageable);
	
	
}
