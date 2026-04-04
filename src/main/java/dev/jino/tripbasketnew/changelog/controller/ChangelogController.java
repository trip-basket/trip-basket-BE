package dev.jino.tripbasketnew.changelog.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import dev.jino.tripbasketnew.changelog.service.ChangelogService;
import dev.jino.tripbasketnew.changelog.service.ChangelogService.ChangelogPage;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChangelogController {

    private final ChangelogService changelogService;

    @GetMapping("/changelog")
    public String getChangelog(Model model) {
        ChangelogPage page = changelogService.getChangelogPage();
        model.addAttribute("pageTitle", page.title());
        model.addAttribute("contentHtml", page.contentHtml());
        return "changelog";
    }
}
