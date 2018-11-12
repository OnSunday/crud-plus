package com.jfeat.crud.plus;

/**
 * Created by vincenthuang on 18/09/2017.
 */
public class DemoType {
    private Long id;
    private String name;
    private Long pid;

    public DemoType() {
    }

    public DemoType(Long id, String name, Long pid) {
        this.id = id;
        this.name = name;
        this.pid = pid;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getPid() {
        return pid;
    }

    public void setPid(Long pid) {
        this.pid = pid;
    }
}
