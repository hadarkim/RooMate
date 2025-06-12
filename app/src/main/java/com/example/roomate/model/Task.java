package com.example.roomate.model;

import com.google.firebase.database.Exclude;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * מודל מטלה לבית משותף.
 *  ▸ id            – מנגנון ייחודי (UUID / Firestore document id)
 *  ▸ title         – כותרת המטלה
 *  ▸ description   – פירוט (אופציונלי)
 *  ▸ room          – אזור בבית (מטבח, סלון, שירותים…)
 *  ▸ assignedToUid – UID של הדייר האחראי
 *  ▸ dueDate       – תאריך יעד
 *  ▸ priority      – "רגילה" / "דחופה"
 *  ▸ done          – ביצוע (true/false)
 */
public class Task implements Serializable {

    // ====== שדות ======
    private String id;
    private String title;
    private String description;
    private String room;
    private String assignedToUid;
    private Date   dueDate;
    private String priority;   // REGULAR / URGENT
    private boolean done;

    // ====== ctor ריק (נדרש ל-Firestore) ======
    public Task() {}

    // ctor מלא – נוח לשימוש באפליקציה
    public Task(String id,
                String title,
                String description,
                String room,
                String assignedToUid,
                Date dueDate,
                String priority,
                boolean done) {

        this.id = id;
        this.title = title;
        this.description = description;
        this.room = room;
        this.assignedToUid = assignedToUid;
        this.dueDate = dueDate;
        this.priority = priority;
        this.done = done;
    }

    // ====== getters & setters ======
    public String getId()            { return id; }
    public void   setId(String id)   { this.id = id; }

    public String getTitle()         { return title; }
    public void   setTitle(String t) { this.title = t; }

    public String getDescription()           { return description; }
    public void   setDescription(String d)   { this.description = d; }

    public String getRoom()          { return room; }
    public void   setRoom(String r)  { this.room = r; }

    public String getAssignedToUid()             { return assignedToUid; }
    public void   setAssignedToUid(String uid)   { this.assignedToUid = uid; }

    public Date getDueDate()         { return dueDate; }
    public void setDueDate(Date d)   { this.dueDate = d; }

    public String getPriority()         { return priority; }
    public void   setPriority(String p) { this.priority = p; }

    public boolean isDone()           { return done; }
    public void    setDone(boolean d) { this.done = d; }

    // ====== עזר: השדה done כ-int (נוח למיון) ======
    @Exclude           // Firestore לא ישמור שדה מחושב
    public int getDoneAsInt() { return done ? 1 : 0; }

    // ====== equals & hashCode (דרוש ל-DiffUtil) ======
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task)) return false;
        Task t = (Task) o;
        return done == t.done &&
                Objects.equals(id, t.id) &&
                Objects.equals(title, t.title) &&
                Objects.equals(description, t.description) &&
                Objects.equals(room, t.room) &&
                Objects.equals(assignedToUid, t.assignedToUid) &&
                Objects.equals(dueDate, t.dueDate) &&
                Objects.equals(priority, t.priority);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, room,
                assignedToUid, dueDate, priority, done);
    }
}
