package com.shrija.performance.service;
import com.google.adk.agents.*;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.common.collect.ImmutableList;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import com.shrija.performance.agent.PerformanceAgent;
import com.shrija.performance.dto.PerformanceChatRequest;
import com.shrija.performance.exception.PerformanceAgentExecutionException;
import io.reactivex.rxjava3.core.Flowable;
import java.util.*;
import org.slf4j.*;
import org.springframework.stereotype.Service;
@Service
public class PerformanceConversationService {
  private static final Logger log=LoggerFactory.getLogger(PerformanceConversationService.class);
  private final InMemoryRunner runner; private final String appName;
  public PerformanceConversationService(PerformanceAgent agent){BaseAgent a=agent.agent();appName=a.name();runner=new InMemoryRunner(a);}
  public Result converse(PerformanceChatRequest request){
    String sessionId=request.sessionId()==null||request.sessionId().isBlank()?UUID.randomUUID().toString():request.sessionId();
    try{
      ensureSession(request.userId(),sessionId);
      String msg="Authenticated actor: "+request.userId()+"; role: "+request.role()+".\nUser request: "+request.message();
      Content content=Content.builder().role("user").parts(ImmutableList.of(Part.builder().text(msg).build())).build();
      Flowable<Event> events=runner.runAsync(request.userId(),sessionId,content,RunConfig.builder().build());
      return new Result(sessionId,collect(events));
    }catch(Exception ex){
      log.error("Performance Agent failed for actor={} session={}",request.userId(),sessionId,ex);
      throw new PerformanceAgentExecutionException("Performance Agent could not process the request right now.",ex);
    }
  }
  private void ensureSession(String userId,String sessionId){
    Session existing=runner.sessionService().getSession(appName,userId,sessionId,Optional.empty()).blockingGet();
    if(existing==null) runner.sessionService().createSession(appName,userId,null,sessionId).blockingGet();
  }
  private String collect(Flowable<Event> events){
    List<Event> collected=events.toList().blockingGet(); StringBuilder result=new StringBuilder();
    for(Event event:collected) result.append(event.stringifyContent());
    return result.toString().stripTrailing();
  }
  public record Result(String sessionId,String responseText){}
}
