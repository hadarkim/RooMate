package com.example.roomate.model;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.PropertyName;
import java.util.Objects;
@IgnoreExtraProperties
public class ShoppingItem {
    private String id;               // מזהה הפריט (מפתח ב-DB)
    private String name;             // שם הפריט
    private boolean isBought;        // סטטוס האם נרכש
    private String assignedToUid;    // UID של מי שהוקצה אליו (יכול להיות null)
    /** ctor ריק נדרש עבור Firebase Realtime Database */
    public ShoppingItem() {}
    /** ctor נוח ליצירה בצד הלקוח, עם isBought כברירת מחדל false */
    public ShoppingItem(String id, String name, String assignedToUid) {
        this.id = id;
        this.name = name;
        this.assignedToUid = assignedToUid;
        this.isBought = false;
    }
    /** ctor מלא כולל סטטוס isBought */
    public ShoppingItem(String id, String name, String assignedToUid, boolean isBought) {
        this.id = id;
        this.name = name;
        this.assignedToUid = assignedToUid;
        this.isBought = isBought;
    }
    // -------------------- getters & setters --------------------
    public String getId() {return id;}
    public void setId(String id) {this.id = id;}
    public String getName() {return name;}
    public void setName(String name) {this.name = name;}
    /** Firebase uses this getter for the JSON key "isBought" */
    @PropertyName("isBought")
    public boolean isBought() {
        return isBought;
    }
    /**     firebase משתמש ב-setter הזה בשביל מפתח ה-json "isBought" */
    @PropertyName("isBought")
    public void setIsBought(boolean isBought) {
        this.isBought = isBought;
    }
    /**
     * מתודה נוספת לשימוש פנימי
     * (ניתן להשאיר אותה, אבל Firebase כבר יקרא ל־setIsBought).
     */
    public void setBought(boolean bought) {this.isBought = bought;}
    public String getAssignedToUid() {return assignedToUid;}
    public void setAssignedToUid(String assignedToUid) {this.assignedToUid = assignedToUid;}
    // -------------------- equals & hashCode & toString --------------------
    @Exclude
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShoppingItem)) return false;
        ShoppingItem that = (ShoppingItem) o;
        return isBought == that.isBought &&
                Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(assignedToUid, that.assignedToUid);
    }
    @Exclude
    @Override
    public int hashCode() {
        return Objects.hash(id, name, assignedToUid, isBought);
    }
    @Exclude
    @Override
    public String toString() {
        return "ShoppingItem{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", isBought=" + isBought +
                ", assignedToUid='" + assignedToUid + '\'' +
                '}';
    }
}
