package com.example.tasktracker.model;
import java.time.LocalDate;
public class WorkTask {
    private long id; private String title; private String description; private long resourceId;
    private LocalDate startDate; private LocalDate dueDate; private String status;
    public WorkTask() {}
    public WorkTask(long id,String title,String description,long resourceId,LocalDate startDate,LocalDate dueDate,String status) {
        this.id=id;this.title=title;this.description=description;this.resourceId=resourceId;this.startDate=startDate;this.dueDate=dueDate;this.status=status;
    }
    public long getId(){return id;} public void setId(long v){id=v;}
    public String getTitle(){return title;} public void setTitle(String v){title=v;}
    public String getDescription(){return description;} public void setDescription(String v){description=v;}
    public long getResourceId(){return resourceId;} public void setResourceId(long v){resourceId=v;}
    public LocalDate getStartDate(){return startDate;} public void setStartDate(LocalDate v){startDate=v;}
    public LocalDate getDueDate(){return dueDate;} public void setDueDate(LocalDate v){dueDate=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
}
