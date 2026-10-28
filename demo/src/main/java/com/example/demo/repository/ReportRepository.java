package com.example.demo.repository;
import com.example.demo.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
public interface ReportRepository extends JpaRepository<Report, Long> { Page<Report> findByStatus(String status, Pageable pageable); }
