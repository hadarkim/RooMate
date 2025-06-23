
package com.example.roomate.model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {
    private String id;         // מזהה המשתמש (Firebase UID)
    private String name;       // השם להציג
    private String email;      // אימייל
    private String avatarUrl;  // כתובת תמונת פרופיל
    private String groupId;    // מזהה הקבוצה שבה המשתמש חבר (יכול להיות null)

    // ctor ריק נדרש על־ידי Firebase
    public User() { }

    // ctor מלא נוח ליצירת אובייקט
    public User(String id,
                String name,
                String email,
                String avatarUrl,
                String groupId) {
        this.id        = id;
        this.name      = name;
        this.email     = email;
        this.avatarUrl = avatarUrl;
        this.groupId   = groupId;
    }

    // getters & setters
    public String getId()           { return id; }
    public void   setId(String id)  { this.id = id; }

    public String getName()             { return name; }
    public void   setName(String name)  { this.name = name; }

    public String getEmail()               { return email; }
    public void   setEmail(String email)   { this.email = email; }

    public String getAvatarUrl()                   { return avatarUrl; }
    public void   setAvatarUrl(String avatarUrl)   { this.avatarUrl = avatarUrl; }

    public String getGroupId()               { return groupId; }
    public void   setGroupId(String groupId) { this.groupId = groupId; }
}
