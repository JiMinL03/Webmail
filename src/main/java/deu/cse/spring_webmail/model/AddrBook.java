/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 *
 * @author yeong
 */
@Entity
@Table(name = "addrbook")
public class AddrBook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    
    @Column(nullable = false, length = 50)
    private String email;
    
    @Column(name = "concat_email", nullable = false, length = 50)
    private String concatEmail;
    
    @Column(nullable = false, length = 20)
    private String name;
    
    @Column(name = "phone_num", nullable = false, length = 20)
    private String phoneNum;
    
    public AddrBook() {
    }

    public AddrBook(String email, String concatEmail, String name, String phoneNum) {
        this.email = email;
        this.concatEmail = concatEmail;
        this.name = name;
        this.phoneNum = phoneNum;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getConcatEmail() {
        return concatEmail;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNum() {
        return phoneNum;
    }
    
}
