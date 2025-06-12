#!/usr/bin/env bash
set -e

# שינוי לפי שם החבילה שלך
BASE_PKG_PATH="app/src/main/java/com/yourcompany/roomate/model"

# יוצרים את התיקייה
mkdir -p "$BASE_PKG_PATH"

# Task.java
cat > "$BASE_PKG_PATH/Task.java" << 'EOF'
package com.yourcompany.roomate.model;

public class Task {
    private String id;
    private String title;
    private String roomId;
    private String assignedToUserId;
    private String dueDate;
    private String priority;
    private boolean done;

    public Task() { }

    public Task(String id,
                String title,
                String roomId,
                String assignedToUserId,
                String dueDate,
                String priority,
                boolean done) {
        this.id = id;
        this.title = title;
        this.roomId = roomId;
        this.assignedToUserId = assignedToUserId;
        this.dueDate = dueDate;
        this.priority = priority;
        this.done = done;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getAssignedToUserId() { return assignedToUserId; }
    public void setAssignedToUserId(String assignedToUserId) { this.assignedToUserId = assignedToUserId; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public boolean isDone() { return done; }
    public void setDone(boolean done) { this.done = done; }
}
EOF

# User.java
cat > "$BASE_PKG_PATH/User.java" << 'EOF'
package com.yourcompany.roomate.model;

public class User {
    private String id;
    private String name;
    private String email;
    private String avatarUrl;
    private String groupId;

    public User() { }

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
EOF

# ShoppingItem.java
cat > "$BASE_PKG_PATH/ShoppingItem.java" << 'EOF'
package com.yourcompany.roomate.model;

public class ShoppingItem {
    private String id;
    private String name;
    private boolean bought;
    private String assignedToUserId;

    public ShoppingItem() { }

    public ShoppingItem(String id,
                        String name,
                        boolean bought,
                        String assignedToUserId) {
        this.id = id;
        this.name = name;
        this.bought = bought;
        this.assignedToUserId = assignedToUserId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isBought() { return bought; }
    public void setBought(boolean bought) { this.bought = bought; }

    public String getAssignedToUserId() { return assignedToUserId; }
    public void setAssignedToUserId(String assignedToUserId) { this.assignedToUserId = assignedToUserId; }
}
EOF

# Room.java
cat > "$BASE_PKG_PATH/Room.java" << 'EOF'
package com.yourcompany.roomate.model;

public class Room {
    private String id;
    private String name;

    public Room() { }

    public Room(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
EOF

# NotificationData.java
cat > "$BASE_PKG_PATH/NotificationData.java" << 'EOF'
package com.yourcompany.roomate.model;

public class NotificationData {
    private String id;
    private String message;
    private String timestamp;
    private String recipientUserId;
    private boolean read;

    public NotificationData() { }

    public NotificationData(String id,
                            String message,
                            String timestamp,
                            String recipientUserId,
                            boolean read) {
        this.id = id;
        this.message = message;
        this.timestamp = timestamp;
        this.recipientUserId = recipientUserId;
        this.read = read;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getRecipientUserId() { return recipientUserId; }
    public void setRecipientUserId(String recipientUserId) { this.recipientUserId = recipientUserId; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
}
EOF

echo "Model classes generated in $BASE_PKG_PATH"
