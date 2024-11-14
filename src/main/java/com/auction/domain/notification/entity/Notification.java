package com.auction.domain.notification.entity;

import com.auction.common.entity.TimeStamped;
import com.auction.domain.notification.enums.NotificationType;
import com.auction.domain.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@Table(name = "notification")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends TimeStamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User receiver;

    @NotNull
    private String content;

    @Column(name = "related_url")
    private String relatedUrl;

    @NotNull
    @Column(name = "is_read")
    private boolean isRead;

    @NotNull
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private Notification(User user, String content, String relatedUrl, NotificationType type) {
        this.receiver = user;
        this.content = content;
        this.relatedUrl = relatedUrl;
        this.isRead = false;
        this.type = type;
    }
    public static Notification of(User user, String content, String relatedUrl, NotificationType type) {
        return new Notification(user, content, relatedUrl, type);
    }
}
