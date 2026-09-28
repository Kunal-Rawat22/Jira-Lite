package com.jiralite.tickets.api.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TicketListRequest {
    private String searchKey;
    private List<String> status = new ArrayList<>();
    private List<UUID> assignee = new ArrayList<>();
    private List<UUID> reporter = new ArrayList<>();
    private List<UUID> product = new ArrayList<>();
    private List<UUID> user = new ArrayList<>();
    private Integer size;
    private Integer page;

    public String getSearchKey() {
        return searchKey;
    }

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }

    public List<String> getStatus() {
        return status;
    }

    public void setStatus(List<String> status) {
        this.status = status == null ? new ArrayList<>() : status;
    }

    public List<UUID> getAssignee() {
        return assignee;
    }

    public void setAssignee(List<UUID> assignee) {
        this.assignee = assignee == null ? new ArrayList<>() : assignee;
    }

    public List<UUID> getReporter() {
        return reporter;
    }

    public void setReporter(List<UUID> reporter) {
        this.reporter = reporter == null ? new ArrayList<>() : reporter;
    }

    public List<UUID> getProduct() {
        return product;
    }

    public void setProduct(List<UUID> product) {
        this.product = product == null ? new ArrayList<>() : product;
    }

    public List<UUID> getUser() {
        return user;
    }

    public void setUser(List<UUID> user) {
        this.user = user == null ? new ArrayList<>() : user;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }
}
