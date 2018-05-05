package com.alarm.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cache.CacheManager;

public class CacheManagerCheck implements CommandLineRunner {
	
	private static final Logger logger = LoggerFactory.getLogger(CacheManagerCheck.class);
	
	private final CacheManager cacheManager ; //singletone cache , 최초 한번 호출될 때에만 CacheMange의 초기화 작업이 수행되므, 이후 동일한 CacheManager인스턴스를 리턴하게 됨.

	public CacheManagerCheck(CacheManager cacheManager)
	{
		this.cacheManager=cacheManager;
	}
	
	@Override
	public void run(String ...strings) throws Exception
	{
		logger.info("\n\n" + "===========================================\n"
				+ "Using cacheManager :" + this.cacheManager.getClass().getName() + "\n"
				+ "=========================================\n\n");
		
	}
	
	public void putCache()
	{
		this.cacheManager.getCache("BBScache");
		
		
	}
	
}
