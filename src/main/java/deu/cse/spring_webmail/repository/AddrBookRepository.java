/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.repository;

import deu.cse.spring_webmail.model.AddrBook;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author yeong
 */
@Repository
public interface AddrBookRepository extends JpaRepository<AddrBook,Long> {

    public List<AddrBook> findAllByEmail(String email);
    boolean existsByEmailAndConcatEmail(String email, String concatEmail);
    
}
