package com.example.roomate.model;

public class ShoppingItem {
    private String id;
    private String name;
    private boolean isBought;
    private String assignedToUserId;

    // ctor ריק – חייב ל-Firestore
    public ShoppingItem() {}

    public ShoppingItem(String id, String name, String assignedToUserId) {
        this.id = id;
        this.name = name;
        this.assignedToUserId = assignedToUserId;
        this.isBought = false;
    }

    // getters & setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isBought() { return isBought; }
    public void setBought(boolean bought) { isBought = bought; }

    public String getAssignedToUserId() { return assignedToUserId; }
    public void setAssignedToUserId(String assignedToUserId) {
        this.assignedToUserId = assignedToUserId;
    }
}
