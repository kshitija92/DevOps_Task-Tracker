package com.example.tasktracker.controller;
import com.example.tasktracker.model.*;
import com.example.tasktracker.service.TrackerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
@Controller
public class WebController {
    private final TrackerService service;
    public WebController(TrackerService service){this.service=service;}
    @GetMapping("/") public String home(Model m){
        m.addAttribute("appTitle","Task Tracker");m.addAttribute("resources",service.resources());m.addAttribute("tasks",service.tasks());
        m.addAttribute("total",service.tasks().size());m.addAttribute("pending",service.tasks().stream().filter(t->"PENDING".equals(t.getStatus())).count());
        m.addAttribute("progress",service.tasks().stream().filter(t->"IN_PROGRESS".equals(t.getStatus())).count());
        m.addAttribute("done",service.tasks().stream().filter(t->"COMPLETED".equals(t.getStatus())).count());return "index";
    }
    @PostMapping("/resources/save") public String saveResource(@RequestParam(defaultValue="0") long id,@RequestParam String name,@RequestParam String role,@RequestParam String email,RedirectAttributes ra){
        if(name.isBlank()||role.isBlank()){ra.addFlashAttribute("error","Name and role are required.");return "redirect:/";}
        service.saveResource(new ResourceMember(id,name.trim(),role.trim(),email.trim()));ra.addFlashAttribute("success","Resource saved.");return "redirect:/";
    }
    @PostMapping("/resources/delete/{id}") public String deleteResource(@PathVariable long id,RedirectAttributes ra){service.deleteResource(id);ra.addFlashAttribute("success","Resource and assigned tasks removed.");return "redirect:/";}
    @PostMapping("/tasks/save") public String saveTask(@RequestParam(defaultValue="0") long id,@RequestParam String title,@RequestParam(required=false) String description,@RequestParam long resourceId,@RequestParam LocalDate startDate,@RequestParam LocalDate dueDate,@RequestParam String status,RedirectAttributes ra){
        if(title.isBlank()||dueDate.isBefore(startDate)||service.resource(resourceId)==null){ra.addFlashAttribute("error","Enter a title, valid resource, and due date on or after start date.");return "redirect:/";}
        service.saveTask(new WorkTask(id,title.trim(),description==null?"":description,resourceId,startDate,dueDate,status));ra.addFlashAttribute("success","Task saved.");return "redirect:/";
    }
    @PostMapping("/tasks/delete/{id}") public String deleteTask(@PathVariable long id,RedirectAttributes ra){service.deleteTask(id);ra.addFlashAttribute("success","Task deleted.");return "redirect:/";}
}
