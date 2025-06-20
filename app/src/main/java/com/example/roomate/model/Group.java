package com.example.roomate.model;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.Exclude;

import java.util.Objects;

@IgnoreExtraProperties
public class Group {
    private String id;
    private String name;

    // ctor ריק נדרש ל-Firebase
    public Group() {}

    // ctor נוח
    public Group(String id, String name) {
        this.id = id;
        this.name = name;
    }

    // getters & setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // equals/hashCode כדי ש-DiffUtil יכול לזהות שינויים בתוכן
    @Exclude
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Group)) return false;
        Group group = (Group) o;
        // משווים גם id וגם name
        return Objects.equals(id, group.id) &&
                Objects.equals(name, group.name);
    }

    @Exclude
    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Exclude
    @Override
    public String toString() {
        return "Group{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
