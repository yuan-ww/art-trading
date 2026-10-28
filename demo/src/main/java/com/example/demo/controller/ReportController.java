package com.example.demo.controller;
import com.example.demo.model.Report;
import com.example.demo.repository.ReportRepository;
import com.example.demo.repository.ArtworksRepository;
import com.example.demo.service.AccessControlService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
@RestController @RequestMapping("/api/reports")
public class ReportController {
    private final ReportRepository reports; private final ArtworksRepository artworks; private final AccessControlService access;
    public ReportController(ReportRepository reports, ArtworksRepository artworks, AccessControlService access) { this.reports=reports; this.artworks=artworks; this.access=access; }
    @PostMapping public Report create(@RequestParam Integer reporterId, @RequestParam Integer artworkId, @RequestParam String reason) {
        access.requireSelfOrAdmin(reporterId);
        if (reason == null || reason.isBlank() || reason.length() > 500 || !artworks.existsById(artworkId)) throw new IllegalArgumentException("举报信息无效");
        Report report = new Report(); report.setReporterId(reporterId); report.setArtworkId(artworkId); report.setReason(reason.trim()); return reports.save(report);
    }
    @GetMapping public Page<Report> list(@RequestParam(required=false) String status, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) {
        access.requireAdmin(); PageRequest pageable = PageRequest.of(Math.max(0,page), Math.min(100,Math.max(1,size)), org.springframework.data.domain.Sort.by("createdAt").descending());
        return status == null || status.isBlank() ? reports.findAll(pageable) : reports.findByStatus(status.trim(), pageable);
    }
    @PutMapping("/{id}/resolve") public Report resolve(@PathVariable Long id) {
        access.requireAdmin(); Report report=reports.findById(id).orElseThrow(() -> new IllegalArgumentException("举报不存在"));
        report.setStatus("Resolved"); report.setResolvedAt(LocalDateTime.now()); return reports.save(report);
    }
}
