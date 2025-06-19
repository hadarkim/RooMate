package com.example.roomate.model;

public class User {
    private String id;  // מזהה המשתמש (same as Firebase UID)
    private String name;  // השם להציג
    private String email;  // אימייל
    private String avatarUrl;  // כתובת תמונת פרופיל (URL)
    private String groupId;   // מזהה הקבוצה/הבית שהמשתמש משתייך אליו

    // ③ ctor ריק – חובה עבור Firebase Firestore
    public User() { }

    // ④ ctor מלא נוח ליצירת אובייקט בצד הלקוח
    public User(String id,
                String name,
                String email,
                String avatarUrl,
                String groupId) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.groupId = groupId;
    }

    // ⑤ getters & setters לכל שדה
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
}
