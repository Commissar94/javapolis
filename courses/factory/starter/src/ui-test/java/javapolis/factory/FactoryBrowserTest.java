package javapolis.factory;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.RequestOptions;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/** Author/CI checks only. Enable with -Psolution,ui after installing Playwright Chromium. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FactoryBrowserTest {
    @LocalServerPort int port;
    Playwright playwright;
    Browser browser;
    Page page;
    final List<String> errors = new ArrayList<>();

    @BeforeEach void openFactory() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
        page = browser.newPage(new Browser.NewPageOptions().setViewportSize(1440, 1080));
        page.onPageError(errors::add);
        post(Map.of("action", "reset", "scenario", "contract"));
        page.navigate(url());
        assertThat(page.locator("#mode-label")).hasText("Эталон");
        assertThat(page.locator("[data-machine]")).hasCount(3);
    }

    @AfterEach void closeFactory() {
        try { assertTrue(errors.isEmpty(), errors.toString()); }
        finally { if (browser != null) browser.close(); if (playwright != null) playwright.close(); }
    }

    @Test void buildsConnectsAndDeliversTheContractThroughTheRealPage() throws Exception {
        build("PRESS", "7,3", 4);
        build("SMELTER", "4,5", 5);
        page.locator("[data-machine='2']").click();
        page.locator("[data-disconnect='2,3']").click();
        assertThat(page.locator("[data-disconnect='2,3']")).hasCount(0);
        connect(2, 4); connect(4, 3); connect(1, 5); connect(5, 4);
        assertThat(page.locator("#budget-count")).hasText("80");
        page.locator("#step").click();
        assertThat(page.locator("#tick-count")).hasText("001");
        // Fast-forward deterministic Java time, keeping building and delivery interactions in the UI.
        post(Map.of("action", "step", "amount", 99));
        assertThat(page.locator("#tick-count")).hasText("100");
        screenshot("desktop-before-delivery");
        page.locator("#submit").click();
        assertThat(page.locator("#contract-status")).hasText("Выполнен ✓");
        assertThat(page.locator("#submit")).isDisabled();
        assertThat(page.locator("#code-error")).isHidden();
        screenshot("desktop-complete");
    }

    @Test void mobileLayoutAndWorkshopRemainUsable() throws Exception {
        page.setViewportSize(390, 844);
        assertTrue((Boolean) page.evaluate("document.documentElement.scrollWidth <= innerWidth"),
                "Only the map may scroll horizontally, not the entire page");
        page.locator("#open-workshop-bottom").click();
        assertThat(page.locator("#workshop")).isVisible();
        assertThat(page.locator("#workshop .lesson-list article")).hasCount(5);
        screenshot("mobile-workshop");
        page.locator("[data-close='workshop']").click();
        screenshot("mobile-factory");
    }

    @Test void pauseResetAndConnectionRecoveryWork() {
        page.locator("#play").click();
        assertThat(page.locator("#play")).hasText("Ⅱ Пауза");
        page.locator("#play").click();
        assertThat(page.locator("#play")).hasText("▶ Запустить");
        page.locator("#new-shift").click();
        page.locator("[data-scenario='sandbox']").click();
        assertThat(page.locator("#scenario-label")).hasText("Песочница");
        assertThat(page.locator("#tick-count")).hasText("000");
        page.locator("[data-action='supply']").click();
        assertThat(page.locator("#ore-count")).hasText("3");
        page.route("**/api/factory", route -> route.abort());
        assertThat(page.locator("#offline")).isVisible();
        page.unroute("**/api/factory");
        assertThat(page.locator("#offline")).isHidden();
    }

    @Test void keyboardSelectionSurvivesPolling() {
        page.locator("[data-machine='1']").focus();
        page.locator("[data-machine='1']").press("Enter");
        assertThat(page.locator("#inspector h2")).hasText("Шахта");
        post(Map.of("action", "step", "amount", 1));
        assertThat(page.locator("#tick-count")).hasText("001");
        assertThat(page.locator("[data-machine='1']")).isFocused();
    }

    private void build(String type, String cell, int count) {
        page.locator("[data-build='" + type + "']").click();
        page.locator("[data-cell='" + cell + "']").click();
        assertThat(page.locator("[data-machine]")).hasCount(count);
    }

    private void connect(int from, int to) {
        page.locator("[data-machine='" + from + "']").click();
        page.locator("#connect-machine").click();
        page.locator("[data-machine='" + to + "']").click();
        assertThat(page.locator("[data-disconnect='" + from + "," + to + "']")).hasCount(1);
    }

    private void post(Map<String, Object> data) {
        APIResponse response = page.request().post(url() + "/api/factory", RequestOptions.create().setData(data));
        assertEquals(200, response.status(), response.text());
    }

    private String url() { return "http://127.0.0.1:" + port; }

    private void screenshot(String name) throws Exception {
        Path directory = Path.of(System.getProperty("factory.screenshots", "target/ui-screenshots"));
        Files.createDirectories(directory);
        page.screenshot(new Page.ScreenshotOptions().setFullPage(true).setPath(directory.resolve(name + ".png")));
    }
}
