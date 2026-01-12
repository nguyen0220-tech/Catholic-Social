package com.catholic.ac.kr.catholicsocial.service;

import com.catholic.ac.kr.catholicsocial.entity.dto.NotificationDTO;
import com.catholic.ac.kr.catholicsocial.entity.dto.PageInfo;
import com.catholic.ac.kr.catholicsocial.entity.model.Notification;
import com.catholic.ac.kr.catholicsocial.entity.model.User;
import com.catholic.ac.kr.catholicsocial.mapper.NotificationMapper;
import com.catholic.ac.kr.catholicsocial.projection.NotificationProjection;
import com.catholic.ac.kr.catholicsocial.repository.NotificationRepository;
import com.catholic.ac.kr.catholicsocial.status.NotifyType;
import com.catholic.ac.kr.catholicsocial.wrapper.GraphqlResponse;
import com.catholic.ac.kr.catholicsocial.wrapper.ListResponse;
import graphql.GraphQLException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public int getCountNotifications(Long userId) {
        return notificationRepository.countByUserIdAndRead(userId, false);
    }

    public ListResponse<NotificationDTO> getAllNotifications(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<NotificationProjection> projectionPage = notificationRepository.findByNotificationsByUserId(userId, pageable);

        List<NotificationProjection> projectionList = projectionPage.getContent();

        List<NotificationDTO> rs = NotificationMapper.toNotificationDTO(projectionList);

        return new ListResponse<>(rs, new PageInfo(page, size, projectionPage.hasNext()));
    }

    public void createNotification(User user, User actor, Long entityId, NotifyType type) {
        Notification newNotification = new Notification();

        newNotification.setUser(user);
        newNotification.setActor(actor);
        newNotification.setType(type);

        if (type == NotifyType.SYSTEM)
            newNotification.setEntityId(null);
        else
            newNotification.setEntityId(entityId);

        notificationRepository.save(newNotification);
    }

    public GraphqlResponse<String> maskAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow( ()-> new GraphQLException("Notification not found"));

        notification.setRead(true);

        notificationRepository.save(notification);

        return GraphqlResponse.success("",null);
    }

    public GraphqlResponse<String> deleteNotification(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new GraphQLException("Notification not found"));

        notificationRepository.delete(notification);

        return GraphqlResponse.success("Deleted success", null);
    }

}
