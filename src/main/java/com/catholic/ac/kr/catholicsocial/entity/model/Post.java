//package com.catholic.ac.kr.catholicsocial.entity.model;
//
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
//import java.time.LocalDateTime;
//
//@Entity
//@AllArgsConstructor
//@NoArgsConstructor
//@Getter @Setter
//public class Post {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne (fetch = FetchType.EAGER)
//    @JoinColumn(name = "user_id")
//    private User user;
//
//    private String content;
//
//    private String imageUrl;
//
//    private LocalDateTime createdAt;
//}
