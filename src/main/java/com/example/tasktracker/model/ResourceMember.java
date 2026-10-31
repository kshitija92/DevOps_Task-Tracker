package com.example.tasktracker.model;
public class ResourceMember {
    private long id; private String name; private String role; private String email;
    public ResourceMember() {}
    public ResourceMember(long id, String name, String role, String email) { this.id=id; this.name=name; this.role=role; this.email=email; }
    public long getId(){return id;} public void setId(long v){id=v;}
    public String getName(){return name;} public void setName(String v){name=v;}
    public String getRole(){return role;} public void setRole(String v){role=v;}
    public String getEmail(){return email;} public void setEmail(String v){email=v;}
}
