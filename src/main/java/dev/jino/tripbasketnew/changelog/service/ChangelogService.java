package dev.jino.tripbasketnew.changelog.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Service
@RequiredArgsConstructor
public class ChangelogService {

    private static final String CHANGELOG_RESOURCE_PATH = "classpath:changelog/changelog.md";
    private static final String DEFAULT_TITLE = "Trip Basket Changelog";

    private final ResourceLoader resourceLoader;
    private final Parser parser = Parser.builder().build();
    private final HtmlRenderer htmlRenderer = HtmlRenderer.builder().build();

    public ChangelogPage getChangelogPage() {
        String markdown = loadMarkdown();
        Node document = parser.parse(markdown);

        return new ChangelogPage(extractTitle(markdown), htmlRenderer.render(document));
    }

    private String loadMarkdown() {
        Resource resource = resourceLoader.getResource(CHANGELOG_RESOURCE_PATH);

        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Failed to load changelog", ex);
        }
    }

    private String extractTitle(String markdown) {
        return markdown.lines()
                .map(String::trim)
                .filter(line -> line.startsWith("# "))
                .map(line -> line.substring(2).trim())
                .filter(line -> !line.isBlank())
                .findFirst()
                .orElse(DEFAULT_TITLE);
    }

    public record ChangelogPage(String title, String contentHtml) {}
}
