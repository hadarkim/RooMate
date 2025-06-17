package com.example.roomate.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@IgnoreExtraProperties
public class Task implements Serializable {

    // ====== שדות לשמירה ב־DB ======
    private String id;
    private String title;
    private String description;
    private String room;
    private String assignedToUid;
    private long   dueDateMillis;   // נשמר ב-Firebase
    private String priority;        // REGULAR / URGENT
    private boolean done;

    // ====== ctor ריק (נדרש ל-Firebase) ======
    public Task() {}

    // ====== ctor מלא לנוחות ======
    public Task(String id,
                String title,
                String description,
                String room,
                String assignedToUid,
                Date dueDate,
                String priority,
                boolean done) {
        this.id              = id;
        this.title           = title;
        this.description     = description;
        this.room            = room;
        this.assignedToUid   = assignedToUid;
        setDueDate(dueDate);            // מאפס גם את dueDateMillis
        this.priority        = priority;
        this.done            = done;
    }

    // ====== getters & setters לשדות DB ======
    public String getId()               { return id; }
    public void   setId(String id)      { this.id = id; }

    public String getTitle()            { return title; }
    public void   setTitle(String t)    { this.title = t; }

    public String getDescription()              { return description; }
    public void   setDescription(String desc)   { this.description = desc; }

    public String getRoom()             { return room; }
    public void   setRoom(String room)  { this.room = room; }

    public String getAssignedToUid()            { return assignedToUid; }
    public void   setAssignedToUid(String uid)  { this.assignedToUid = uid; }

    public long getDueDateMillis()      { return dueDateMillis; }
    public void setDueDateMillis(long ms) { this.dueDateMillis = ms; }

    public String getPriority()         { return priority; }
    public void   setPriority(String p) { this.priority = p; }

    public boolean isDone()             { return done; }
    public void    setDone(boolean d)   { this.done = d; }

    // ====== שדות ושיטות משוערים (לא נשמרים ב-DB) ======
    /** לשירות UI: המרת millis ל-Date */
    @Exclude
    public Date getDueDate() {
        return new Date(dueDateMillis);
    }
    /** לשירות UI: המרת Date ל-millis */
    @Exclude
    public void setDueDate(Date dueDate) {
        this.dueDateMillis = dueDate == null ? 0L : dueDate.getTime();
    }

    /** עזר למיון – לא נשמר ב-DB */
    @Exclude
    public int getDoneAsInt() {
        return done ? 1 : 0;
    }

    // ====== equals & hashCode ל-DiffUtil ======
    @Exclude
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task)) return false;
        Task t = (Task) o;
        return done == t.done &&
                dueDateMillis == t.dueDateMillis &&
                Objects.equals(id, t.id) &&
                Objects.equals(title, t.title) &&
                Objects.equals(description, t.description) &&
                Objects.equals(room, t.room) &&
                Objects.equals(assignedToUid, t.assignedToUid) &&
                Objects.equals(priority, t.priority);
    }

    @Exclude
    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, room,
                assignedToUid, dueDateMillis, priority, done);
    }
}
