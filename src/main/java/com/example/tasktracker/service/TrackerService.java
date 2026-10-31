package com.example.tasktracker.service;
import com.example.tasktracker.model.*;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
@Service
public class TrackerService {
    private final Map<Long,ResourceMember> resources=new LinkedHashMap<>();
    private final Map<Long,WorkTask> tasks=new LinkedHashMap<>();
    private final AtomicLong resourceSeq=new AtomicLong(2), taskSeq=new AtomicLong(2);
    public TrackerService() {
        resources.put(1L,new ResourceMember(1,"Aarav Patil","Developer","aarav@example.com"));
        resources.put(2L,new ResourceMember(2,"Neha Sharma","QA Engineer","neha@example.com"));
        tasks.put(1L,new WorkTask(1,"Build login page","Create responsive login screen",1,LocalDate.now(),LocalDate.now().plusDays(4),"IN_PROGRESS"));
        tasks.put(2L,new WorkTask(2,"Test registration","Validate registration flow",2,LocalDate.now(),LocalDate.now().plusDays(2),"PENDING"));
    }
    public List<ResourceMember> resources(){return new ArrayList<>(resources.values());}
    public List<WorkTask> tasks(){return new ArrayList<>(tasks.values());}
    public ResourceMember resource(long id){return resources.get(id);}
    public WorkTask task(long id){return tasks.get(id);}
    public ResourceMember saveResource(ResourceMember x){if(x.getId()==0)x.setId(resourceSeq.incrementAndGet());resources.put(x.getId(),x);return x;}
    public void deleteResource(long id){resources.remove(id);tasks.values().removeIf(t->t.getResourceId()==id);}
    public WorkTask saveTask(WorkTask x){if(x.getId()==0)x.setId(taskSeq.incrementAndGet());tasks.put(x.getId(),x);return x;}
    public void deleteTask(long id){tasks.remove(id);}
}
