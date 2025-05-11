package com.notification.common;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * 
 * @author Abhishek
 */
@Aspect
public class AuditService
{
	
	Logger logger = LogManager.getLogger(AuditService.class);
	
	@Pointcut("execution(* com..*.controllers.*(..))")
	public void anyControllerMethods()
	{
	}
	
	@Pointcut("anyControllerMethods() && @annotation(org.springframework.web.bind.annotation.RequestMapping)")
	public void ControllerPointCut()
	{
	}
	
	@Around("execution(* com..*.common.db.service..*(..)) || ControllerPointCut()")
	public Object doAudit(ProceedingJoinPoint pjp) throws Throwable
	{
		String msg = "Begin " + pjp.toShortString();
		long start = System.currentTimeMillis();
		try
		{
			// IMP: Must call proceed on the join point and return the result
			Object result = pjp.proceed();
			return result;
		}
		finally
		{
			long end = System.currentTimeMillis();
			long timeTakenMillis = end - start;
			msg = "[Time] " + timeTakenMillis + "ms [Target] " + pjp.getTarget() + " [Origin] " + pjp.toShortString();
			logger.debug(msg);
		}
	}
}
